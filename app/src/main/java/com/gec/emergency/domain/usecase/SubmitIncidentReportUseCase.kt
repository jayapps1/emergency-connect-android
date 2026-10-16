package com.gec.emergency.domain.usecase

import com.gec.emergency.core.common.AnonymousSessionProvider
import com.gec.emergency.domain.model.IncidentReport
import com.gec.emergency.domain.model.ReportSubmissionResult
import com.gec.emergency.domain.repository.IncidentReportRepository
import javax.inject.Inject

class SubmitIncidentReportUseCase @Inject constructor(
    private val repository: IncidentReportRepository,
    private val sessionProvider: AnonymousSessionProvider,
) {
    suspend operator fun invoke(report: IncidentReport): ReportSubmissionResult {
        if (report.categoryId.isBlank()) {
            return ReportSubmissionResult.ValidationError("Category is required.")
        }

        val sessionId = sessionProvider.getOrCreateSessionId()
        val preparedReport = report.copy(anonymousSessionId = sessionId)

        return repository.submitReport(preparedReport)
    }
}
