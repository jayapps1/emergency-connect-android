package com.gec.emergency.feature.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.core.location.LocationProvider
import com.gec.emergency.core.location.LocationResult
import com.gec.emergency.domain.model.GeoLocation
import com.gec.emergency.domain.model.NearbyStation
import com.gec.emergency.domain.model.NearbyStationQuery
import com.gec.emergency.domain.usecase.GetActiveAgenciesUseCase
import com.gec.emergency.domain.usecase.GetNearbyStationsUseCase
import com.gec.emergency.domain.usecase.GetPreferredStationContactUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(
    private val locationProvider: LocationProvider,
    private val getNearbyStationsUseCase: GetNearbyStationsUseCase,
    private val getActiveAgenciesUseCase: GetActiveAgenciesUseCase,
    private val getPreferredStationContactUseCase: GetPreferredStationContactUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NearbyUiState())
    val uiState: StateFlow<NearbyUiState> = _uiState.asStateFlow()

    init {
        loadActiveAgencies()
        fetchNearbyStations()
    }

    private fun loadActiveAgencies() {
        viewModelScope.launch {
            getActiveAgenciesUseCase()
                .catch { /* ignore error */ }
                .collect { agencies ->
                    _uiState.value = _uiState.value.copy(agencies = agencies)
                }
        }
    }

    fun onPermissionGranted() {
        _uiState.value = _uiState.value.copy(
            permissionState = LocationPermissionState.Granted,
        )
        refreshLocation()
    }

    fun onPermissionDenied(permanentlyDenied: Boolean = false) {
        val state = if (permanentlyDenied) {
            LocationPermissionState.PermanentlyDenied
        } else {
            LocationPermissionState.Denied
        }
        _uiState.value = _uiState.value.copy(
            permissionState = state,
            isLoadingLocation = false,
            locationAvailable = false,
            isLocationDisabled = false,
        )
        fetchNearbyStations()
    }

    fun setSelectedAgency(agencyId: String?) {
        _uiState.value = _uiState.value.copy(selectedAgencyId = agencyId)
        fetchNearbyStations()
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingLocation = true,
                errorMessage = null,
                isLocationDisabled = false,
            )

            when (val result = locationProvider.getCurrentLocation()) {
                is LocationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingLocation = false,
                        currentLocation = result.location,
                        locationAccuracyMeters = result.location.accuracyMeters,
                        locationAvailable = true,
                        isLocationDisabled = false,
                        errorMessage = null,
                    )
                    fetchNearbyStations()
                }
                LocationResult.PermissionDenied -> {
                    onPermissionDenied(permanentlyDenied = false)
                }
                LocationResult.LocationDisabled -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingLocation = false,
                        locationAvailable = false,
                        isLocationDisabled = true,
                        errorMessage = null,
                    )
                    fetchNearbyStations()
                }
                LocationResult.Unavailable -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingLocation = false,
                        locationAvailable = false,
                        isLocationDisabled = false,
                        errorMessage = null,
                    )
                    fetchNearbyStations()
                }
                is LocationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingLocation = false,
                        locationAvailable = false,
                        isLocationDisabled = false,
                        errorMessage = result.message,
                    )
                    fetchNearbyStations()
                }
            }
        }
    }

    fun fetchNearbyStations() {
        val location = _uiState.value.currentLocation ?: GeoLocation(0.0, 0.0)
        viewModelScope.launch {
            val query = NearbyStationQuery(
                location = location,
                agencyId = _uiState.value.selectedAgencyId,
                radiusKm = 100000.0, // No radius limit
            )
            getNearbyStationsUseCase(query)
                .onStart {
                    _uiState.value = _uiState.value.copy(errorMessage = null)
                }
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.localizedMessage ?: "Failed to load stations.",
                        nearbyStations = emptyList(),
                    )
                }
                .collect { stations ->
                    _uiState.value = _uiState.value.copy(
                        nearbyStations = stations,
                        errorMessage = null,
                    )
                    loadPreferredContacts(stations)
                }
        }
    }

    private fun loadPreferredContacts(stations: List<NearbyStation>) {
        stations.forEach { nearbyStation ->
            val station = nearbyStation.station
            viewModelScope.launch {
                getPreferredStationContactUseCase(station.id)
                    .catch { /* ignore error */ }
                    .collect { contact ->
                        val map = _uiState.value.preferredContacts.toMutableMap()
                        if (contact != null) {
                            map[station.id] = contact
                        } else {
                            map.remove(station.id)
                        }
                        _uiState.value = _uiState.value.copy(preferredContacts = map)
                    }
            }
        }
    }
}
