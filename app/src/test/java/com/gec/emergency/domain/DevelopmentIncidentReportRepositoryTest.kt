package com.gec.emergency.domain

import com.gec.emergency.data.local.DevelopmentIncidentDataSource
import com.gec.emergency.data.repository.DevelopmentIncidentReportRepository
import com.gec.emergency.domain.model.IncidentReport
import com.gec.emergency.domain.model.ReportMode
import com.gec.emergency.domain.model.ReportStatus
import com.gec.emergency.domain.model.ReportSubmissionResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DevelopmentIncidentReportRepositoryTest {

    private val dataSource = DevelopmentIncidentDataSource()
    private val repository = DevelopmentIncidentReportRepository(dataSource)

    @Test
    fun submitReport_generatesReferencePatternAndSubmittedStatus() = runBlocking {
        val report = IncidentReport(
            id = "rpt_test_01",
            categoryId = "cat_fire",
            mode = ReportMode.SILENT,
        )

        val result = repository.submitReport(report)

        assertTrue(result is ReportSubmissionResult.Success)
        val submitted = (result as ReportSubmissionResult.Success).report

        assertNotNull(submitted.publicReference)
        assertTrue(submitted.publicReference!!.startsWith("RPT-"))
        assertEquals(ReportStatus.SUBMITTED, submitted.status)

        val fetched = repository.getReportById("rpt_test_01")
        assertNotNull(fetched)
        assertEquals(submitted.publicReference, fetched?.publicReference)
    }

    @Test
    fun submitReport_recordsInitialStatusHistorySubmitted() = runBlocking {
        val report = IncidentReport(
            id = "rpt_test_02",
            categoryId = "cat_robbery",
            mode = ReportMode.SILENT,
        )

        repository.submitReport(report)

        val history = repository.getStatusHistory("rpt_test_02")
        assertEquals(1, history.size)
        assertEquals(ReportStatus.SUBMITTED, history[0].status)
    }
}
