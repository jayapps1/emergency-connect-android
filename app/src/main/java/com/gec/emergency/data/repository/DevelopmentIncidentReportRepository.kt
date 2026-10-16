package com.gec.emergency.data.repository

import com.gec.emergency.data.local.DevelopmentIncidentDataSource
import com.gec.emergency.domain.model.IncidentCategory
import com.gec.emergency.domain.model.IncidentReport
import com.gec.emergency.domain.model.ReportStatus
import com.gec.emergency.domain.model.ReportStatusHistory
import com.gec.emergency.domain.model.ReportSubmissionResult
import com.gec.emergency.domain.repository.IncidentReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevelopmentIncidentReportRepository @Inject constructor(
    private val dataSource: DevelopmentIncidentDataSource,
) : IncidentReportRepository {

    private val reportsMap = MutableStateFlow<Map<String, IncidentReport>>(emptyMap())
    private val statusHistoryMap = MutableStateFlow<Map<String, List<ReportStatusHistory>>>(emptyMap())

    override suspend fun getCategories(): List<IncidentCategory> {
        return dataSource.getCategories().filter { it.isActive }
    }

    override suspend fun getCategoryById(id: String): IncidentCategory? {
        return dataSource.getCategoryById(id)
    }

    override suspend fun submitReport(report: IncidentReport): ReportSubmissionResult {
        if (report.categoryId.isBlank()) {
            return ReportSubmissionResult.ValidationError("Category is required.")
        }

        val refDate = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(report.createdAtMillis))
        val randomSuffix = UUID.randomUUID().toString().take(6).uppercase(Locale.US)
        val publicRef = "RPT-$refDate-$randomSuffix"

        val finalizedReport = report.copy(
            publicReference = publicRef,
            status = ReportStatus.SUBMITTED,
        )

        val initialHistory = ReportStatusHistory(
            id = "hist_${UUID.randomUUID()}",
            reportId = finalizedReport.id,
            status = ReportStatus.SUBMITTED,
            timestampMillis = finalizedReport.createdAtMillis,
            note = "Report submitted via development simulation.",
        )

        val updatedMap = reportsMap.value.toMutableMap()
        updatedMap[finalizedReport.id] = finalizedReport
        reportsMap.value = updatedMap

        val updatedHistory = statusHistoryMap.value.toMutableMap()
        updatedHistory[finalizedReport.id] = listOf(initialHistory)
        statusHistoryMap.value = updatedHistory

        return ReportSubmissionResult.Success(finalizedReport)
    }

    override suspend fun getReportById(id: String): IncidentReport? {
        return reportsMap.value[id]
    }

    override fun observeReportStatus(id: String): Flow<ReportStatus?> {
        return reportsMap.map { map -> map[id]?.status }
    }

    override suspend fun getStatusHistory(reportId: String): List<ReportStatusHistory> {
        return statusHistoryMap.value[reportId] ?: emptyList()
    }
}
