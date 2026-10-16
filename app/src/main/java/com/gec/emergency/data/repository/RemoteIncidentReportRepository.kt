package com.gec.emergency.data.repository

import com.gec.emergency.core.common.AnonymousSessionProvider
import com.gec.emergency.data.local.DevelopmentIncidentDataSource
import com.gec.emergency.data.local.ReportTrackingStore
import com.gec.emergency.data.mapper.toDomainModel
import com.gec.emergency.data.remote.EmergencyDirectoryApi
import com.gec.emergency.data.remote.dto.toDomainModel
import com.gec.emergency.data.remote.dto.toRequestDto
import com.gec.emergency.domain.model.IncidentCategory
import com.gec.emergency.domain.model.IncidentReport
import com.gec.emergency.domain.model.ReportStatus
import com.gec.emergency.domain.model.ReportStatusHistory
import com.gec.emergency.domain.model.ReportSubmissionResult
import com.gec.emergency.domain.repository.IncidentReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteIncidentReportRepository @Inject constructor(
    private val api: EmergencyDirectoryApi,
    private val sessionProvider: AnonymousSessionProvider,
    private val trackingStore: ReportTrackingStore,
    private val fallbackDataSource: DevelopmentIncidentDataSource,
) : IncidentReportRepository {

    override suspend fun getCategories(): List<IncidentCategory> {
        return try {
            val response = api.getIncidentCategories(null)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { it.toDomainModel() }
            } else {
                fallbackDataSource.getCategories()
            }
        } catch (e: Exception) {
            fallbackDataSource.getCategories()
        }
    }

    override suspend fun getCategoryById(id: String): IncidentCategory? {
        return getCategories().find { it.id == id }
    }

    override suspend fun submitReport(report: IncidentReport): ReportSubmissionResult {
        return try {
            val sessionId = sessionProvider.getOrCreateSessionId()
            val preparedReport = report.copy(anonymousSessionId = sessionId)
            val requestDto = preparedReport.toRequestDto(clientRequestId = preparedReport.id)

            val response = api.submitReport(requestDto)
            if (response.isSuccessful && response.body() != null) {
                val respDto = response.body()!!
                val submittedReport = preparedReport.copy(
                    publicReference = respDto.publicReference,
                    status = runCatching { ReportStatus.valueOf(respDto.status) }.getOrDefault(ReportStatus.SUBMITTED),
                )

                if (!respDto.trackingToken.isNullOrBlank() && !respDto.publicReference.isBlank()) {
                    trackingStore.saveTrackingToken(respDto.publicReference, respDto.trackingToken)
                }

                ReportSubmissionResult.Success(submittedReport)
            } else {
                ReportSubmissionResult.Failed("Failed to submit report. Server status: ${response.code()}")
            }
        } catch (e: Exception) {
            ReportSubmissionResult.Failed(e.localizedMessage ?: "Network error. Please check your connection.")
        }
    }

    override suspend fun getReportById(id: String): IncidentReport? {
        val trackingToken = trackingStore.getTrackingToken(id) ?: return null
        return try {
            val response = api.trackReport(id, trackingToken)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                IncidentReport(
                    id = id,
                    publicReference = body.publicReference,
                    categoryId = "",
                    mode = com.gec.emergency.domain.model.ReportMode.GENERAL,
                    status = runCatching { ReportStatus.valueOf(body.status) }.getOrDefault(ReportStatus.SUBMITTED),
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun observeReportStatus(id: String): Flow<ReportStatus?> = flow {
        val report = getReportById(id)
        emit(report?.status)
    }

    override suspend fun getStatusHistory(reportId: String): List<ReportStatusHistory> {
        val trackingToken = trackingStore.getTrackingToken(reportId) ?: return emptyList()
        return try {
            val response = api.trackReport(reportId, trackingToken)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.statusHistory.mapIndexed { index, histDto ->
                    val parsedStatus = runCatching { ReportStatus.valueOf(histDto.status) }.getOrDefault(ReportStatus.SUBMITTED)
                    ReportStatusHistory(
                        id = "hist_$index",
                        reportId = reportId,
                        status = parsedStatus,
                        note = histDto.note,
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
