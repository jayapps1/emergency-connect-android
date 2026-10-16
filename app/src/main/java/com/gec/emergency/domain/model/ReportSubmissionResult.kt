package com.gec.emergency.domain.model

sealed interface ReportSubmissionResult {
    data class Success(val report: IncidentReport) : ReportSubmissionResult
    data class ValidationError(val message: String) : ReportSubmissionResult
    data object LocationRequired : ReportSubmissionResult
    data class Failed(val message: String) : ReportSubmissionResult
}
