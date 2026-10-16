package com.gec.emergency.feature.reports.details

import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.IncidentReport
import com.gec.emergency.domain.model.ReportStatusHistory

data class ReportDetailsUiState(
    val isLoading: Boolean = false,
    val report: IncidentReport? = null,
    val targetAgencyTypes: Set<AgencyType> = emptySet(),
    val statusHistory: List<ReportStatusHistory> = emptyList(),
    val errorMessage: String? = null,
)
