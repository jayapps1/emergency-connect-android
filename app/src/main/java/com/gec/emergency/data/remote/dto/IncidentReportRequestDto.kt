package com.gec.emergency.data.remote.dto

data class LocationDto(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Double? = null,
    val landmark: String? = null,
    val locality: String? = null,
    val district: String? = null,
    val region: String? = null,
)

data class VehicleInfoDto(
    val registrationNumber: String? = null,
    val vehicleType: String? = null,
    val colour: String? = null,
    val directionOfTravel: String? = null,
)

data class IncidentReportRequestDto(
    val categoryId: String,
    val reportMode: String,
    val description: String? = null,
    val anonymousSessionId: String? = null,
    val clientGeneratedRequestId: String? = null,
    val location: LocationDto? = null,
    val vehicleInfo: VehicleInfoDto? = null,
)
