package com.gec.emergency.feature.reports.general

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.core.location.LocationProvider
import com.gec.emergency.core.location.LocationResult
import com.gec.emergency.domain.model.IncidentCategory
import com.gec.emergency.domain.model.IncidentReport
import com.gec.emergency.domain.model.ReportLocation
import com.gec.emergency.domain.model.ReportMode
import com.gec.emergency.domain.model.ReportSubmissionResult
import com.gec.emergency.domain.model.VehicleInfo
import com.gec.emergency.domain.usecase.GetGeneralIncidentCategoriesUseCase
import com.gec.emergency.domain.usecase.SubmitIncidentReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GeneralReportViewModel @Inject constructor(
    private val getGeneralIncidentCategoriesUseCase: GetGeneralIncidentCategoriesUseCase,
    private val locationProvider: LocationProvider,
    private val submitIncidentReportUseCase: SubmitIncidentReportUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GeneralReportUiState())
    val uiState: StateFlow<GeneralReportUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        refreshLocation()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingCategories = true)
            try {
                val categories = getGeneralIncidentCategoriesUseCase()
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
        val isReckless = category.code == "RECKLESS_DRIVING"
        if (!isReckless) {
            _uiState.value = _uiState.value.copy(
                selectedCategory = category,
                vehicleRegistrationNumber = "",
                vehicleType = "",
                vehicleColour = "",
                directionOfTravel = "",
            )
        } else {
            _uiState.value = _uiState.value.copy(selectedCategory = category)
        }
    }

    fun updateDescription(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun updateVehicleRegistration(value: String) {
        val formatted = value.take(20).uppercase()
        _uiState.value = _uiState.value.copy(vehicleRegistrationNumber = formatted)
    }

    fun updateVehicleType(value: String) {
        _uiState.value = _uiState.value.copy(vehicleType = value)
    }

    fun updateVehicleColour(value: String) {
        _uiState.value = _uiState.value.copy(vehicleColour = value)
    }

    fun updateDirectionOfTravel(value: String) {
        _uiState.value = _uiState.value.copy(directionOfTravel = value)
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

    fun submitReport() {
        val category = _uiState.value.selectedCategory ?: run {
            _uiState.value = _uiState.value.copy(errorMessage = "Please select an incident category.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)

            var loc = _uiState.value.currentLocation
            val locAgeMs = System.currentTimeMillis() - (loc?.timestampMillis ?: 0L)
            if (loc == null || locAgeMs > 5 * 60 * 1000L) {
                val freshResult = locationProvider.getCurrentLocation()
                if (freshResult is LocationResult.Success) {
                    loc = freshResult.location
                    _uiState.value = _uiState.value.copy(
                        currentLocation = loc,
                        locationAccuracyMeters = loc.accuracyMeters,
                        isLocationAvailable = true,
                    )
                }
            }

            val location = loc?.let {
                ReportLocation(geoLocation = it)
            }

            val isReckless = category.code == "RECKLESS_DRIVING"
            val hasVehicleInfo = _uiState.value.vehicleRegistrationNumber.isNotBlank() ||
                    _uiState.value.vehicleType.isNotBlank() ||
                    _uiState.value.vehicleColour.isNotBlank() ||
                    _uiState.value.directionOfTravel.isNotBlank()

            val vehicleInfo = if (isReckless && hasVehicleInfo) {
                VehicleInfo(
                    registrationNumber = _uiState.value.vehicleRegistrationNumber.trim().ifBlank { null },
                    vehicleType = _uiState.value.vehicleType.trim().ifBlank { null },
                    colour = _uiState.value.vehicleColour.trim().ifBlank { null },
                    directionOfTravel = _uiState.value.directionOfTravel.trim().ifBlank { null },
                )
            } else null

            val report = IncidentReport(
                id = UUID.randomUUID().toString(),
                categoryId = category.id,
                mode = ReportMode.GENERAL,
                description = _uiState.value.description.ifBlank { null },
                location = location,
                severity = category.severity,
                vehicleInfo = vehicleInfo,
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
                is ReportSubmissionResult.Failed -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = result.message,
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = "Submission failed.",
                    )
                }
            }
        }
    }
}
