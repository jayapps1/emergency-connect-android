package com.gec.emergency.data.remote.dto

data class StationResponseDto(
    val id: String,
    val agencyId: String,
    val name: String,
    val code: String,
    val stationLevel: String? = null,
    val region: String,
    val district: String,
    val regionId: String? = null,
    val districtId: String? = null,
    val locality: String? = null,
    val latitude: Double,
    val longitude: Double,
    val locationAccuracyMeters: Double? = null,
    val active: Boolean = true,
    val verified: Boolean = true,
)
