package com.gec.emergency.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.gec.emergency.domain.model.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult as FusedLocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "AndroidLocationProvider"

@Singleton
class AndroidLocationProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
) : LocationProvider {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): LocationResult {
        if (!hasLocationPermission()) {
            Log.d(TAG, "Location permission not granted")
            return LocationResult.PermissionDenied
        }

        if (!isLocationEnabled()) {
            Log.d(TAG, "Location services disabled on device")
            return LocationResult.LocationDisabled
        }

        return try {
            Log.d(TAG, "Starting multi-stage location retrieval")

            // Stage 1: High Accuracy Current Location
            var location = getFreshLocation(Priority.PRIORITY_HIGH_ACCURACY, timeoutMs = 8000L)
            if (location != null) {
                Log.d(TAG, "Stage 1 (HIGH_ACCURACY) obtained location: $location")
                return LocationResult.Success(location.toGeoLocation())
            }

            // Stage 2: Balanced Power Accuracy Current Location
            location = getFreshLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, timeoutMs = 5000L)
            if (location != null) {
                Log.d(TAG, "Stage 2 (BALANCED_POWER) obtained location: $location")
                return LocationResult.Success(location.toGeoLocation())
            }

            // Stage 3: Fused Location Client Last Known Location
            location = getLastKnownFusedLocation()
            if (location != null) {
                Log.d(TAG, "Stage 3 (fused lastLocation) obtained location: $location")
                return LocationResult.Success(location.toGeoLocation())
            }

            // Stage 4: Single Location Update Callback
            location = requestSingleLocationUpdate(timeoutMs = 5000L)
            if (location != null) {
                Log.d(TAG, "Stage 4 (single update callback) obtained location: $location")
                return LocationResult.Success(location.toGeoLocation())
            }

            // Stage 5: Direct System LocationManager Fallback (GPS / Network / Passive)
            location = getDirectSystemLocation()
            if (location != null) {
                Log.d(TAG, "Stage 5 (system LocationManager) obtained location: $location")
                return LocationResult.Success(location.toGeoLocation())
            }

            Log.w(TAG, "All location retrieval stages returned null")
            LocationResult.Unavailable
        } catch (_: SecurityException) {
            Log.e(TAG, "SecurityException while accessing location")
            LocationResult.PermissionDenied
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error during location retrieval", e)
            LocationResult.Error(e.localizedMessage)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getFreshLocation(priority: Int, timeoutMs: Long): Location? {
        return withTimeoutOrNull(timeoutMs) {
            try {
                val cancellationTokenSource = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(
                    priority,
                    cancellationTokenSource.token,
                ).awaitTask()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                null
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLastKnownFusedLocation(): Location? {
        return try {
            fusedLocationClient.lastLocation.awaitTask()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            null
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleLocationUpdate(timeoutMs: Long): Location? {
        return withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { continuation ->
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                    .setMaxUpdates(1)
                    .build()

                val callback = object : LocationCallback() {
                    override fun onLocationResult(result: FusedLocationResult) {
                        fusedLocationClient.removeLocationUpdates(this)
                        val loc = result.lastLocation
                        if (continuation.isActive) {
                            continuation.resume(loc)
                        }
                    }
                }

                try {
                    fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
                    continuation.invokeOnCancellation {
                        fusedLocationClient.removeLocationUpdates(callback)
                    }
                } catch (_: Exception) {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDirectSystemLocation(): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        var bestLocation: Location? = null
        for (provider in providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || loc.time > bestLocation.time) {
                        bestLocation = loc
                    }
                }
            } catch (_: SecurityException) {
                // Ignore security exception for specific provider
            }
        }
        return bestLocation
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}

private fun Location.toGeoLocation(): GeoLocation {
    return GeoLocation(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = if (hasAccuracy()) accuracy.toDouble() else null,
        timestampMillis = time,
    )
}

private suspend fun <T> Task<T>.awaitTask(): T? = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        if (continuation.isActive) {
            continuation.resume(result)
        }
    }
    addOnFailureListener { exception ->
        if (continuation.isActive) {
            continuation.resumeWithException(exception)
        }
    }
    addOnCanceledListener {
        if (continuation.isActive) {
            continuation.cancel()
        }
    }
}
