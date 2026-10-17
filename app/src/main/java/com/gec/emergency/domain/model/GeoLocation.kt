package com.gec.emergency.domain.model

/**
 * Pure Kotlin representation of geographic coordinates.
 * Free of Android framework dependencies.
 */
data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Double? = null,
    val timestampMillis: Long? = null,
    val isMock: Boolean = false,
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90 degrees" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180 degrees" }
        accuracyMeters?.let {
            require(it >= 0.0) { "Location accuracy must not be negative" }
        }
    }

    /**
     * Checks if coordinates fall within the broad operational bounding box of Ghana
     * (Latitude 4.5 to 11.5, Longitude -3.5 to 1.5).
     */
    fun isPlausibleForGhana(): Boolean {
        return latitude in 4.5..11.5 && longitude in -3.5..1.5
    }
}
