package com.gec.emergency.domain.model

data class IncidentCategory(
    val id: String,
    val name: String,
    val description: String? = null,
    val severity: IncidentSeverity,
    val reportModeSupported: Set<ReportMode>,
    val defaultAgencyTypes: Set<AgencyType> = emptySet(),
    val supportsCall: Boolean = true,
    val isActive: Boolean = true,
) {
    init {
        require(id.isNotBlank()) { "Category ID must not be blank" }
        require(name.isNotBlank()) { "Category name must not be blank" }
    }
}
