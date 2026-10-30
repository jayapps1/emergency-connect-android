package com.gec.emergency.data.remote.dto

data class CreateEmergencyCallAttemptRequestDto(
    val agencyId: String,
    val stationId: String? = null,
    val stationContactId: String? = null,
    val destinationType: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Double? = null,
    val clientGeneratedRequestId: String? = null,
)

data class EmergencyCallAttemptResponseDto(
    val id: String,
    val publicReference: String,
    val agencyId: String,
    val stationId: String? = null,
    val destinationType: String? = null,
    val destinationPhone: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Double? = null,
    val status: String,
    val initiatedAt: String? = null,
    val callLaunchedAt: String? = null,
    val linkedReportId: String? = null,
    val createdAt: String? = null,
)
