package com.gec.emergency.domain.model

data class Station(
    val id: String,
    val agencyId: String,
    val name: String,
    val code: String,
    val stationLevel: String? = "DISTRICT_STATION",
    val region: String,
    val district: String,
    val regionId: String? = null,
    val districtId: String? = null,
    val locality: String? = null,
    val latitude: Double,
    val longitude: Double,
    val locationAccuracyMeters: Double? = null,
    val isActive: Boolean = true,
    val isVerified: Boolean = true,
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90 degrees" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180 degrees" }
    }
}
