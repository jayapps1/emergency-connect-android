package com.gec.emergency.domain.repository

import com.gec.emergency.domain.model.IncidentCategory
import com.gec.emergency.domain.model.IncidentReport
import com.gec.emergency.domain.model.ReportStatus
import com.gec.emergency.domain.model.ReportStatusHistory
import com.gec.emergency.domain.model.ReportSubmissionResult
import kotlinx.coroutines.flow.Flow

interface IncidentReportRepository {
    suspend fun getCategories(): List<IncidentCategory>
    suspend fun getCategoryById(id: String): IncidentCategory?
    suspend fun submitReport(report: IncidentReport): ReportSubmissionResult
    suspend fun getReportById(id: String): IncidentReport?
    fun observeReportStatus(id: String): Flow<ReportStatus?>
    suspend fun getStatusHistory(reportId: String): List<ReportStatusHistory>
}
