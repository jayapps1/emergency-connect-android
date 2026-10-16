package com.gec.emergency.domain.model

data class IncidentReport(
    val id: String,
    val publicReference: String? = null,
    val categoryId: String,
    val mode: ReportMode,
    val description: String? = null,
    val location: ReportLocation? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val severity: IncidentSeverity = IncidentSeverity.MEDIUM,
    val status: ReportStatus = ReportStatus.SUBMITTED,
    val vehicleInfo: VehicleInfo? = null,
    val anonymousSessionId: String? = null,
) {
    init {
        require(id.isNotBlank()) { "Report ID must not be blank" }
        require(categoryId.isNotBlank()) { "Category ID must not be blank" }
    }
}
