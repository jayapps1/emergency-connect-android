package com.gec.emergency.domain

import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.IncidentCategory
import com.gec.emergency.domain.model.IncidentReport
import com.gec.emergency.domain.model.IncidentSeverity
import com.gec.emergency.domain.model.ReportMode
import com.gec.emergency.domain.model.ReportStatus
import com.gec.emergency.domain.model.ReportStatusHistory
import com.gec.emergency.domain.model.ReportSubmissionResult
import com.gec.emergency.domain.repository.IncidentReportRepository
import com.gec.emergency.domain.usecase.GetGeneralIncidentCategoriesUseCase
import com.gec.emergency.domain.usecase.GetSilentIncidentCategoriesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.InputStream

class IncidentCategoryRulesTest {

    private val fakeRepository = object : IncidentReportRepository {
        private val categoriesList = listOf(
            IncidentCategory(
                id = "cat_silent_only",
                name = "Silent Only",
                severity = IncidentSeverity.HIGH,
                reportModeSupported = setOf(ReportMode.SILENT),
                defaultAgencyTypes = setOf(AgencyType.POLICE),
            ),
            IncidentCategory(
                id = "cat_general_only",
                name = "General Only",
                severity = IncidentSeverity.LOW,
                reportModeSupported = setOf(ReportMode.GENERAL),
                defaultAgencyTypes = setOf(AgencyType.FIRE),
            ),
            IncidentCategory(
                id = "cat_both",
                name = "Both Supported",
                severity = IncidentSeverity.CRITICAL,
                reportModeSupported = setOf(ReportMode.SILENT, ReportMode.GENERAL),
                defaultAgencyTypes = setOf(AgencyType.POLICE, AgencyType.AMBULANCE),
            ),
        )

        override suspend fun getCategories(mode: String?): List<IncidentCategory> {
            return if (mode == "SILENT") {
                categoriesList.filter { ReportMode.SILENT in it.reportModeSupported }
            } else if (mode == "GENERAL") {
                categoriesList.filter { ReportMode.GENERAL in it.reportModeSupported }
            } else {
                categoriesList
            }
        }

        override suspend fun getCategoryById(id: String): IncidentCategory? = getCategories(null).find { it.id == id }
        override suspend fun submitReport(report: IncidentReport): ReportSubmissionResult = ReportSubmissionResult.Success(report)
        override suspend fun uploadReportAttachment(
            reportId: String,
            trackingToken: String,
            mediaType: String,
            fileName: String,
            mimeType: String,
            inputStream: InputStream,
            contentLength: Long,
        ): Boolean = true
        override suspend fun getReportById(id: String): IncidentReport? = null
        override fun observeReportStatus(id: String): Flow<ReportStatus?> = flowOf(null)
        override suspend fun getStatusHistory(reportId: String): List<ReportStatusHistory> = emptyList()
    }

    private val getSilentCategoriesUseCase = GetSilentIncidentCategoriesUseCase(fakeRepository)
    private val getGeneralCategoriesUseCase = GetGeneralIncidentCategoriesUseCase(fakeRepository)

    @Test
    fun getSilentCategories_filtersSilentSupportedOnly() = runBlocking {
        val result = getSilentCategoriesUseCase()
        assertEquals(2, result.size)
        assertTrue(result.all { ReportMode.SILENT in it.reportModeSupported })
    }

    @Test
    fun getGeneralCategories_filtersGeneralSupportedOnly() = runBlocking {
        val result = getGeneralCategoriesUseCase()
        assertEquals(2, result.size)
        assertTrue(result.all { ReportMode.GENERAL in it.reportModeSupported })
    }
}
