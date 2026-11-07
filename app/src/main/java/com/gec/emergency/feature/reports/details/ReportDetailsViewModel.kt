package com.gec.emergency.feature.reports.details

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.core.location.IncidentLocationSharingService
import com.gec.emergency.data.local.ReportTrackingStore
import com.gec.emergency.data.remote.EmergencyDirectoryApi
import com.gec.emergency.data.remote.dto.SubmitResponseReviewRequestDto
import com.gec.emergency.data.remote.dto.TrackedReportResponseDto
import com.gec.emergency.domain.repository.IncidentReportRepository
import com.gec.emergency.domain.repository.StationRepository
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
    private val stationRepository: StationRepository,
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

            val pendingRequests = stationRepository.getPendingLiveTrackingRequests(reportId)
            val pending = pendingRequests.firstOrNull()

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
                    pendingLiveRequest = pending,
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Report detail not found.",
                )
            }
        }
    }

    fun respondToLiveRequest(context: Context, action: String) {
        val req = _uiState.value.pendingLiveRequest ?: return
        val reportId = activeReportId ?: return

        viewModelScope.launch {
            val success = stationRepository.respondToLiveTrackingRequest(req.id, action)
            if (success && "ACCEPT".equals(action, ignoreCase = true)) {
                val sessionId = stationRepository.createLocationSession(reportId)
                if (!sessionId.isNullOrBlank()) {
                    IncidentLocationSharingService.startService(context, sessionId)
                }
            }
            _uiState.value = _uiState.value.copy(pendingLiveRequest = null)
        }
    }

    fun openRatingDialog(assignment: TrackedReportResponseDto.AgencyAssignmentDetailDto) {
        _uiState.value = _uiState.value.copy(
            isRatingDialogOpen = true,
            ratingTargetAssignment = assignment,
            errorMessage = null,
        )
    }

    fun dismissRatingDialog() {
        _uiState.value = _uiState.value.copy(
            isRatingDialogOpen = false,
            ratingTargetAssignment = null,
            errorMessage = null,
        )
    }

    fun submitResponseReview(
        rating: Int,
        responseSpeed: Int?,
        professionalism: Int?,
        helpfulness: Int?,
        comment: String
    ) {
        val assignment = _uiState.value.ratingTargetAssignment
        if (assignment == null) {
            Log.d("GEC_REVIEW_FLOW", "STEP3_ABORT ratingTargetAssignment is null")
            _uiState.value = _uiState.value.copy(errorMessage = "Unable to identify review assignment. Please try again.")
            return
        }

        val publicRef = _uiState.value.trackedDetail?.publicReference ?: activeReportId
        if (publicRef.isNullOrBlank()) {
            Log.d("GEC_REVIEW_FLOW", "STEP3_ABORT publicRef is null or blank")
            _uiState.value = _uiState.value.copy(errorMessage = "Unable to identify report. Please try again.")
            return
        }

        Log.d("GEC_REVIEW_FLOW", "STEP3 viewmodel_submit_started rating=$rating assignmentId=${assignment.assignmentId} publicRef=$publicRef")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingReview = true, errorMessage = null)
            try {
                val token = trackingStore.getPublicAuthToken()
                val bearer = if (!token.isNullOrBlank()) "Bearer $token" else null

                val requestDto = SubmitResponseReviewRequestDto(
                    rating = rating,
                    responseSpeedRating = responseSpeed,
                    professionalismRating = professionalism,
                    helpfulnessRating = helpfulness,
                    comment = comment.ifBlank { null },
                )

                Log.d("GEC_REVIEW_FLOW", "STEP5 api_request_started reportId=$publicRef assignmentId=${assignment.assignmentId}")

                val response = api.submitResponseReview(
                    bearerToken = bearer,
                    reportId = publicRef,
                    assignmentId = assignment.assignmentId,
                    request = requestDto,
                )

                Log.d("GEC_REVIEW_FLOW", "STEP6 api_response_status=${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    Log.d("GEC_REVIEW_FLOW", "STEP7 review_saved_success")
                    _uiState.value = _uiState.value.copy(
                        isSubmittingReview = false,
                        isRatingDialogOpen = false,
                        ratingTargetAssignment = null,
                        trackedDetail = response.body(),
                    )
                    loadReport(publicRef)
                } else {
                    val errCode = response.code()
                    Log.d("GEC_REVIEW_FLOW", "STEP6_FAIL code=$errCode")
                    _uiState.value = _uiState.value.copy(
                        isSubmittingReview = false,
                        errorMessage = when (errCode) {
                            401 -> "Your session has expired. Please sign in again."
                            403 -> "You cannot review this response."
                            409 -> "This response is not yet eligible for review."
                            else -> "Please check your rating and try again."
                        },
                    )
                }
            } catch (e: Exception) {
                Log.d("GEC_REVIEW_FLOW", "STEP5_EXCEPTION error=${e.localizedMessage}")
                _uiState.value = _uiState.value.copy(
                    isSubmittingReview = false,
                    errorMessage = e.localizedMessage ?: "Failed to submit review.",
                )
            }
        }
    }
}
