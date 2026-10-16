package com.gec.emergency.domain.model

/**
 * Represents an emergency station with calculated distance relative to a query location.
 */
data class NearbyStation(
    val station: Station,
    val distanceKm: Double,
) {
    init {
        require(distanceKm >= 0.0) { "Distance must not be negative" }
    }
}
