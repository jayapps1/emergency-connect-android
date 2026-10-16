package com.gec.emergency.domain.model

data class IncidentRoutingResult(
    val reportId: String,
    val targetAgencyTypes: Set<AgencyType>,
    val assignments: List<ReportAssignment> = emptyList(),
)
