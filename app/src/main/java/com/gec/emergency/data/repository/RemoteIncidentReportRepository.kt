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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote implementation of {@link IncidentReportRepository} interacting with GEC backend endpoints.
 *
 * <p>Identifier Mapping Note:
 * {@code clientGeneratedRequestId} provides client-side submission idempotency. Upon successful submission,
 * the domain model {@code id} is updated to the authoritative backend {@code publicReference} string (e.g. RPT-20261019-ABC123)
 * so report detail lookups and navigation use the public reference key.
 */
@Singleton
class RemoteIncidentReportRepository @Inject constructor(
    private val api: EmergencyDirectoryApi,
    private val sessionProvider: AnonymousSessionProvider,
    private val trackingStore: ReportTrackingStore,
    private val fallbackDataSource: DevelopmentIncidentDataSource,
) : IncidentReportRepository {

    override suspend fun getCategories(mode: String?): List<IncidentCategory> {
        return try {
            val response = api.getIncidentCategories(mode)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { it.toDomainModel() }
                    .filter { it.isActive && (mode == null || (mode == "SILENT" && it.reportModeSupported.contains(com.gec.emergency.domain.model.ReportMode.SILENT))) }
                    .sortedWith(compareBy({ it.displayOrder }, { it.name }))
            } else {
                fallbackDataSource.getCategories().filter {
                    it.isActive && (mode == null || (mode == "SILENT" && it.reportModeSupported.contains(com.gec.emergency.domain.model.ReportMode.SILENT)))
                }.sortedWith(compareBy({ it.displayOrder }, { it.name }))
            }
        } catch (e: Exception) {
            fallbackDataSource.getCategories().filter {
                it.isActive && (mode == null || (mode == "SILENT" && it.reportModeSupported.contains(com.gec.emergency.domain.model.ReportMode.SILENT)))
            }.sortedWith(compareBy({ it.displayOrder }, { it.name }))
        }
    }

    override suspend fun getCategoryById(id: String): IncidentCategory? {
        return getCategories(null).find { it.id == id }
    }

    override suspend fun submitReport(report: IncidentReport): ReportSubmissionResult {
        return try {
            val sessionId = sessionProvider.getOrCreateSessionId()
            val preparedReport = report.copy(anonymousSessionId = sessionId)
            val requestDto = preparedReport.toRequestDto(clientRequestId = preparedReport.id)

            val installationId = trackingStore.getOrCreateInstallationId()
            val authToken = trackingStore.getPublicAuthToken()
            val bearerToken = if (!authToken.isNullOrBlank()) "Bearer $authToken" else null

            val response = api.submitReport(
                installationId = installationId,
                bearerToken = bearerToken,
                request = requestDto,
            )

            if (response.isSuccessful && response.body() != null) {
                val respDto = response.body()!!
                val authoritativeRef = respDto.publicReference.ifBlank { preparedReport.id }

                val submittedReport = preparedReport.copy(
                    id = authoritativeRef,
                    publicReference = authoritativeRef,
                    trackingToken = respDto.trackingToken,
                    status = runCatching { ReportStatus.valueOf(respDto.status) }.getOrDefault(ReportStatus.SUBMITTED),
                )

                if (!respDto.trackingToken.isNullOrBlank() && !authoritativeRef.isBlank()) {
                    trackingStore.saveTrackingToken(authoritativeRef, respDto.trackingToken)
                }

                ReportSubmissionResult.Success(submittedReport)
            } else {
                ReportSubmissionResult.Failed("Failed to submit report. Server status: ${response.code()}")
            }
        } catch (e: Exception) {
            ReportSubmissionResult.Failed(e.localizedMessage ?: "Network error. Please check your connection.")
        }
    }

    override suspend fun uploadReportAttachment(
        reportId: String,
        trackingToken: String,
        mediaType: String,
        fileName: String,
        mimeType: String,
        inputStream: InputStream,
        contentLength: Long,
    ): Boolean {
        return try {
            val bytes = inputStream.readBytes()
            val fileRequestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", fileName, fileRequestBody)
            val mediaTypeRequestBody = mediaType.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.uploadReportAttachment(reportId, trackingToken, mediaTypeRequestBody, filePart)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getReportById(id: String): IncidentReport? {
        val trackingToken = trackingStore.getTrackingToken(id)?.ifBlank { null }
        return try {
            val response = api.trackReport(id, trackingToken)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val parsedCreatedAt = runCatching {
                    if (body.createdAt != null) {
                        java.time.OffsetDateTime.parse(body.createdAt).toInstant().toEpochMilli()
                    } else System.currentTimeMillis()
                }.getOrDefault(System.currentTimeMillis())

                IncidentReport(
                    id = body.publicReference,
                    publicReference = body.publicReference,
                    categoryId = "",
                    mode = com.gec.emergency.domain.model.ReportMode.GENERAL,
                    status = runCatching { ReportStatus.valueOf(body.status) }.getOrDefault(ReportStatus.SUBMITTED),
                    createdAtMillis = parsedCreatedAt,
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
        val trackingToken = trackingStore.getTrackingToken(reportId)?.ifBlank { null }
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
