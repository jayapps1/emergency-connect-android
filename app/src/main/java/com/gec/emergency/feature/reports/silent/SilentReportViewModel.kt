package com.gec.emergency.feature.reports.silent

import android.content.Context
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
import com.gec.emergency.domain.repository.IncidentReportRepository
import com.gec.emergency.domain.usecase.GetSilentIncidentCategoriesUseCase
import com.gec.emergency.domain.usecase.SubmitIncidentReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel managing silent incident reporting for high-risk situations (e.g. robbery, assault, domestic distress).
 *
 * <p>Safety Constraint:
 * Silent reporting MUST NOT automatically play audio, speak via text-to-speech, vibrate, or initiate
 * an outgoing telephone call because doing so could reveal the victim's position and endanger the user
 * in hostage, robbery, or hostile distress situations.
 */
@HiltViewModel
class SilentReportViewModel @Inject constructor(
    private val getSilentIncidentCategoriesUseCase: GetSilentIncidentCategoriesUseCase,
    private val locationProvider: LocationProvider,
    private val submitIncidentReportUseCase: SubmitIncidentReportUseCase,
    private val reportRepository: IncidentReportRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SilentReportUiState())
    val uiState: StateFlow<SilentReportUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        refreshLocation()
    }

    /**
     * Loads incident categories configured specifically for silent report mode.
     */
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

    fun updateVehicleRegistrationNumber(value: String) {
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

    fun addAttachment(attachment: SelectedMediaAttachment) {
        val currentList = _uiState.value.attachments
        if (currentList.size >= 4) {
            _uiState.value = _uiState.value.copy(errorMessage = "Maximum limit of 4 attachments reached.")
            return
        }

        if (attachment.mediaType == "IMAGE" && attachment.sizeBytes > 10 * 1024 * 1024) {
            _uiState.value = _uiState.value.copy(errorMessage = "Selected image exceeds maximum 10 MB size limit.")
            return
        }

        if (attachment.mediaType == "VIDEO" && attachment.sizeBytes > 50 * 1024 * 1024) {
            _uiState.value = _uiState.value.copy(errorMessage = "Selected video exceeds maximum 50 MB size limit.")
            return
        }

        _uiState.value = _uiState.value.copy(
            attachments = currentList + attachment,
            errorMessage = null,
        )
    }

    fun removeAttachment(attachment: SelectedMediaAttachment) {
        val updated = _uiState.value.attachments.filter { it.uri != attachment.uri }
        _uiState.value = _uiState.value.copy(attachments = updated)
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

    /**
     * Submits a silent incident report and uploads optional photo/video evidence.
     */
    fun submitSilentReport(context: Context) {
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

            val isRecklessDriving = category.code == "RECKLESS_DRIVING"
            val hasVehicleData = _uiState.value.vehicleRegistrationNumber.isNotBlank() ||
                    _uiState.value.vehicleType.isNotBlank() ||
                    _uiState.value.vehicleColour.isNotBlank() ||
                    _uiState.value.directionOfTravel.isNotBlank()

            val vehicleInfo = if (isRecklessDriving && hasVehicleData) {
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
                mode = ReportMode.SILENT,
                description = _uiState.value.description.ifBlank { null },
                location = location,
                severity = category.severity,
                vehicleInfo = vehicleInfo,
            )

            when (val result = submitIncidentReportUseCase(report)) {
                is ReportSubmissionResult.Success -> {
                    val submitted = result.report
                    val attachmentsToUpload = _uiState.value.attachments

                    if (attachmentsToUpload.isNotEmpty() && submitted.publicReference != null && submitted.trackingToken != null) {
                        _uiState.value = _uiState.value.copy(isUploadingMedia = true)
                        var failedUploads = 0

                        for (att in attachmentsToUpload) {
                            try {
                                val inputStream = context.contentResolver.openInputStream(att.uri)
                                if (inputStream != null) {
                                    val uploaded = reportRepository.uploadReportAttachment(
                                        reportId = submitted.id,
                                        trackingToken = submitted.trackingToken,
                                        mediaType = att.mediaType,
                                        fileName = att.fileName,
                                        mimeType = att.mimeType,
                                        inputStream = inputStream,
                                        contentLength = att.sizeBytes,
                                    )
                                    inputStream.close()
                                    if (!uploaded) failedUploads++
                                } else {
                                    failedUploads++
                                }
                            } catch (_: Exception) {
                                failedUploads++
                            }
                        }

                        _uiState.value = _uiState.value.copy(isUploadingMedia = false)

                        if (failedUploads > 0) {
                            _uiState.value = _uiState.value.copy(
                                isSubmitting = false,
                                submissionSucceeded = true,
                                submittedReport = submitted,
                                errorMessage = "Report submitted. $failedUploads attachment(s) could not be uploaded.",
                            )
                            return@launch
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submissionSucceeded = true,
                        submittedReport = submitted,
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
