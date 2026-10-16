package com.gec.emergency.data.remote.dto

data class ReportAssignmentDto(
    val id: String,
    val reportId: String,
    val agencyType: String,
    val agencyId: String? = null,
    val stationId: String? = null,
    val status: String,
    val assignedAtMillis: Long?,
)
