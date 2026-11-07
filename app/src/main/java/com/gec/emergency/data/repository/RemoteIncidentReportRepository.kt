package com.gec.emergency.data.repository

import com.gec.emergency.core.common.AnonymousSessionProvider
import com.gec.emergency.data.local.ReportTrackingStore
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
import org.json.JSONObject
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Incident categories used for public emergency submission must originate
 * from the backend because their UUID identifies the authoritative category
 * record used for validation and agency routing. Development fixtures must
 * never silently replace failed operational API responses.
 *
 * Preserve the backend error reason during development because HTTP 404 may
 * represent either an unmapped endpoint or a domain-level resource lookup
 * failure such as an inactive incident category.
 */
@Singleton
class RemoteIncidentReportRepository @Inject constructor(
    private val api: EmergencyDirectoryApi,
    private val sessionProvider: AnonymousSessionProvider,
    private val trackingStore: ReportTrackingStore,
) : IncidentReportRepository {

    // Loads active incident categories from the backend API for the requested reporting mode.
    override suspend fun getCategories(mode: String?): List<IncidentCategory> {
        val matchesMode: (IncidentCategory) -> Boolean = { category ->
            category.isActive && (
                mode == null ||
                (mode.equals("SILENT", ignoreCase = true) && category.reportModeSupported.contains(com.gec.emergency.domain.model.ReportMode.SILENT)) ||
                (mode.equals("GENERAL", ignoreCase = true) && category.reportModeSupported.contains(com.gec.emergency.domain.model.ReportMode.GENERAL))
            )
        }

        val response = api.getIncidentCategories(mode)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.map { it.toDomainModel() }
                .filter(matchesMode)
                .sortedWith(compareBy({ it.displayOrder }, { it.name }))
        } else {
            throw IllegalStateException("Unable to load incident categories. Server status: ${response.code()}")
        }
    }

    override suspend fun getCategoryById(id: String): IncidentCategory? {
        return getCategories(null).find { it.id == id }
    }

    // Prepares the report payload with installation ID, anonymous session ID, and client request UUID before POST submission.
    override suspend fun submitReport(report: IncidentReport): ReportSubmissionResult {
        return try {
            val sessionId = sessionProvider.getOrCreateSessionId()
            val clientRequestId = try {
                UUID.fromString(report.id).toString()
            } catch (_: Exception) {
                UUID.randomUUID().toString()
            }

            val preparedReport = report.copy(
                id = clientRequestId,
                anonymousSessionId = sessionId,
            )
            val requestDto = preparedReport.toRequestDto(clientRequestId = clientRequestId)

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
                val authoritativeRef = respDto.publicReference.ifBlank { clientRequestId }

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
                val rawError = response.errorBody()?.string()
                val parsedMsg = try {
                    if (!rawError.isNullOrBlank()) {
                        JSONObject(rawError).optString("message").ifBlank { null }
                    } else null
                } catch (_: Exception) {
                    null
                }

                val safeMsg = parsedMsg ?: when (response.code()) {
                    400 -> "Report validation error. Please check your submission fields."
                    404 -> "Selected category or reporting service is no longer active."
                    else -> "Failed to submit report. Server status: ${response.code()}"
                }

                ReportSubmissionResult.Failed(safeMsg)
            }
        } catch (e: Exception) {
            ReportSubmissionResult.Failed(e.localizedMessage ?: "Network error. Please check your connection.")
        }
    }

    // Uploads optional media attachments for an incident report without failing the core report.
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

    // Tracks a public report using its public reference key and tracking token.
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
