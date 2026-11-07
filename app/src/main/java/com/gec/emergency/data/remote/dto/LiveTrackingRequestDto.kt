package com.gec.emergency.data.remote.dto

data class LiveTrackingRequestDto(
    val id: String,
    val reportId: String,
    val requestedByStaffId: String? = null,
    val requestedByAgencyId: String? = null,
    val reason: String? = null,
    val status: String,
    val requestedAt: String? = null,
    val expiresAt: String? = null,
    val respondedAt: String? = null,
)

data class CreateLocationSessionRequestDto(
    val reportId: String? = null,
    val callAttemptId: String? = null,
    val trackingPolicy: String? = "LIVE_OPTIONAL",
    val consentGrantedAt: String? = null,
)

data class LocationSessionResponseDto(
    val sessionId: String,
    val reportId: String? = null,
    val callAttemptId: String? = null,
    val status: String,
    val trackingPolicy: String? = null,
    val consentGrantedAt: String? = null,
    val startedAt: String? = null,
    val lastLocationAt: String? = null,
    val endedAt: String? = null,
)

data class LocationPointRequestDto(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Double? = null,
    val capturedAt: String? = null,
)
