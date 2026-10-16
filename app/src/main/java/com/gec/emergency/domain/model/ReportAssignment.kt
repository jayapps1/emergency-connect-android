package com.gec.emergency.domain.model

data class ReportAssignment(
    val id: String,
    val reportId: String,
    val agencyType: AgencyType,
    val agencyId: String? = null,
    val stationId: String? = null,
    val status: ReportStatus = ReportStatus.ASSIGNED,
    val assignedAtMillis: Long = System.currentTimeMillis(),
)
