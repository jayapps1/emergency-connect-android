package com.gec.emergency.feature.reports.silent

import com.gec.emergency.domain.model.GeoLocation
import com.gec.emergency.domain.model.IncidentCategory
import com.gec.emergency.domain.model.IncidentReport

data class SilentReportUiState(
    val isLoadingCategories: Boolean = false,
    val isFetchingLocation: Boolean = false,
    val categories: List<IncidentCategory> = emptyList(),
    val selectedCategory: IncidentCategory? = null,
    val currentLocation: GeoLocation? = null,
    val locationAccuracyMeters: Double? = null,
    val description: String = "",
    val isSubmitting: Boolean = false,
    val isLocationAvailable: Boolean = false,
    val submissionSucceeded: Boolean = false,
    val submittedReport: IncidentReport? = null,
    val errorMessage: String? = null,
)
