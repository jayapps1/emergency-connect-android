package com.gec.emergency.feature.nearby

import android.content.Context
import com.gec.emergency.R

object DistanceFormatter {

    fun formatDistance(context: Context, distanceKm: Double): String {
        return if (distanceKm < 1.0) {
            val meters = (distanceKm * 1000).toInt()
            context.getString(R.string.distance_meters_away, meters)
        } else {
            context.getString(R.string.distance_km_away, distanceKm)
        }
    }

    fun formatAccuracy(context: Context, accuracyMeters: Double?): String? {
        if (accuracyMeters == null) return null
        val accuracyInt = accuracyMeters.toInt()
        return when {
            accuracyMeters <= 20.0 -> context.getString(R.string.accuracy_good, accuracyInt)
            accuracyMeters <= 100.0 -> context.getString(R.string.accuracy_moderate, accuracyInt)
            else -> context.getString(R.string.accuracy_low, accuracyInt)
        }
    }
}
