package com.gec.emergency.feature.reports.silent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.core.location.LocationProvider
import com.gec.emergency.core.location.LocationResult
import com.gec.emergency.domain.model.IncidentCategory
import com.gec.emergency.domain.model.IncidentReport
import com.gec.emergency.domain.model.ReportLocation
import com.gec.emergency.domain.model.ReportMode
import com.gec.emergency.domain.model.ReportSubmissionResult
import com.gec.emergency.domain.usecase.GetSilentIncidentCategoriesUseCase
import com.gec.emergency.domain.usecase.SubmitIncidentReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SilentReportViewModel @Inject constructor(
    private val getSilentIncidentCategoriesUseCase: GetSilentIncidentCategoriesUseCase,
    private val locationProvider: LocationProvider,
    private val submitIncidentReportUseCase: SubmitIncidentReportUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SilentReportUiState())
    val uiState: StateFlow<SilentReportUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        refreshLocation()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingCategories = true)
            try {
                val categories = getSilentIncidentCategoriesUseCase()
                _uiState.value = _uiState.value.copy(
                    isLoadingCategories = false,
                    categories = categories,
                    selectedCategory = categories.firstOrNull(),
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingCategories = false,
                    errorMessage = e.localizedMessage ?: "Failed to load categories",
                )
            }
        }
    }

    fun selectCategory(category: IncidentCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun updateDescription(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFetchingLocation = true)
            when (val result = locationProvider.getCurrentLocation()) {
                is LocationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isFetchingLocation = false,
                        currentLocation = result.location,
                        locationAccuracyMeters = result.location.accuracyMeters,
                        isLocationAvailable = true,
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isFetchingLocation = false,
                        isLocationAvailable = false,
                    )
                }
            }
        }
    }

    fun submitSilentReport() {
        val category = _uiState.value.selectedCategory ?: run {
            _uiState.value = _uiState.value.copy(errorMessage = "Please select an incident category.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)

            val location = _uiState.value.currentLocation?.let { GeoLocation ->
                ReportLocation(geoLocation = GeoLocation)
            }

            val report = IncidentReport(
                id = "rpt_${UUID.randomUUID()}",
                categoryId = category.id,
                mode = ReportMode.SILENT,
                description = _uiState.value.description.ifBlank { null },
                location = location,
                severity = category.severity,
            )

            when (val result = submitIncidentReportUseCase(report)) {
                is ReportSubmissionResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submissionSucceeded = true,
                        submittedReport = result.report,
                    )
                }
                is ReportSubmissionResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = result.message,
                    )
                }
                ReportSubmissionResult.LocationRequired -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = "Location is required for silent reports.",
                    )
                }
                is ReportSubmissionResult.Failed -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = result.message,
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
