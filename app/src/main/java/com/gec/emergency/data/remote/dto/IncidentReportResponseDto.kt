package com.gec.emergency.data.remote.dto

data class IncidentReportResponseDto(
    val id: String,
    val publicReference: String,
    val trackingToken: String? = null,
    val status: String,
    val mode: String,
    val severity: String,
    val createdAt: String? = null,
    val targetAgencyTypes: Set<String> = emptySet(),
)
