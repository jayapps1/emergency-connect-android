package com.gec.emergency.data.remote.dto

data class StationResponseDto(
    val id: String,
    val agencyId: String,
    val name: String,
    val code: String,
    val region: String,
    val district: String,
    val locality: String? = null,
    val latitude: Double,
    val longitude: Double,
    val locationAccuracyMeters: Double? = null,
    val active: Boolean = true,
    val verified: Boolean = true,
)
