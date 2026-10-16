package com.gec.emergency.feature.reports.general

import com.gec.emergency.domain.model.GeoLocation
import com.gec.emergency.domain.model.IncidentCategory
import com.gec.emergency.domain.model.IncidentReport

data class GeneralReportUiState(
    val isLoadingCategories: Boolean = false,
    val isFetchingLocation: Boolean = false,
    val categories: List<IncidentCategory> = emptyList(),
    val selectedCategory: IncidentCategory? = null,
    val currentLocation: GeoLocation? = null,
    val locationAccuracyMeters: Double? = null,
    val description: String = "",
    val vehicleRegistrationNumber: String = "",
    val vehicleType: String = "",
    val vehicleColour: String = "",
    val directionOfTravel: String = "",
    val isSubmitting: Boolean = false,
    val isLocationAvailable: Boolean = false,
    val submissionSucceeded: Boolean = false,
    val submittedReport: IncidentReport? = null,
    val errorMessage: String? = null,
)
