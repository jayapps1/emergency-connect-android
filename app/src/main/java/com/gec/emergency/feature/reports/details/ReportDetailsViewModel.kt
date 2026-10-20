package com.gec.emergency.feature.reports.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.data.local.ReportTrackingStore
import com.gec.emergency.data.remote.EmergencyDirectoryApi
import com.gec.emergency.data.remote.dto.TrackedReportResponseDto
import com.gec.emergency.domain.repository.IncidentReportRepository
import com.gec.emergency.domain.usecase.GetReportByIdUseCase
import com.gec.emergency.domain.usecase.RouteIncidentReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Review submission uses the authoritative report and assignment IDs
 * returned by the backend; agency IDs or local list positions must
 * never be substituted for assignmentId.
 */
@HiltViewModel
class ReportDetailsViewModel @Inject constructor(
    private val getReportByIdUseCase: GetReportByIdUseCase,
    private val incidentReportRepository: IncidentReportRepository,
    private val routeIncidentReportUseCase: RouteIncidentReportUseCase,
    private val api: EmergencyDirectoryApi,
    private val trackingStore: ReportTrackingStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportDetailsUiState())
    val uiState: StateFlow<ReportDetailsUiState> = _uiState.asStateFlow()

    private var activeReportId: String? = null

    fun loadReport(reportId: String) {
        activeReportId = reportId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val report = getReportByIdUseCase(reportId)
            val trackingToken = trackingStore.getTrackingToken(reportId)?.ifBlank { null }

            var trackedDto: TrackedReportResponseDto? = null
            try {
                val resp = api.trackReport(reportId, trackingToken)
                if (resp.isSuccessful && resp.body() != null) {
                    trackedDto = resp.body()
                }
            } catch (_: Exception) {}

            if (report != null || trackedDto != null) {
                val category = report?.let { incidentReportRepository.getCategoryById(it.categoryId) }
                val targetAgencies = if (report != null && category != null) {
                    routeIncidentReportUseCase(report, category).targetAgencyTypes
                } else emptySet()

                val history = incidentReportRepository.getStatusHistory(reportId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    report = report,
                    targetAgencyTypes = targetAgencies,
                    statusHistory = history,
                    trackedDetail = trackedDto,
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Report detail not found.",
                )
            }
        }
    }

    fun openRatingDialog(assignment: TrackedReportResponseDto.AgencyAssignmentDetailDto) {
        _uiState.value = _uiState.value.copy(
            isRatingDialogOpen = true,
            ratingTargetAssignment = assignment,
        )
    }

    fun dismissRatingDialog() {
        _uiState.value = _uiState.value.copy(
            isRatingDialogOpen = false,
            ratingTargetAssignment = null,
        )
    }

    fun submitResponseReview(
        rating: Int,
        responseSpeed: Int?,
        professionalism: Int?,
        helpfulness: Int?,
        comment: String
    ) {
        val assignment = _uiState.value.ratingTargetAssignment ?: return
        val publicRef = _uiState.value.trackedDetail?.publicReference ?: activeReportId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingReview = true, errorMessage = null)
            try {
                val token = trackingStore.getPublicAuthToken()
                val bearer = if (!token.isNullOrBlank()) "Bearer $token" else null

                val body = mutableMapOf<String, Any?>(
                    "rating" to rating,
                    "responseSpeedRating" to responseSpeed,
                    "professionalismRating" to professionalism,
                    "helpfulnessRating" to helpfulness,
                    "comment" to comment.ifBlank { null },
                )

                val response = api.submitResponseReview(
                    bearerToken = bearer,
                    reportId = publicRef,
                    assignmentId = assignment.assignmentId,
                    body = body,
                )

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = _uiState.value.copy(
                        isSubmittingReview = false,
                        isRatingDialogOpen = false,
                        ratingTargetAssignment = null,
                        trackedDetail = response.body(),
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmittingReview = false,
                        errorMessage = when (response.code()) {
                            401 -> "Your session has expired. Please sign in again."
                            403 -> "You cannot review this response."
                            409 -> "This response is not yet eligible for review."
                            else -> "Please check your rating and try again."
                        },
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmittingReview = false,
                    errorMessage = e.localizedMessage ?: "Failed to submit review.",
                )
            }
        }
    }
}
