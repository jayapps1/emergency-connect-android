package com.gec.emergency.data.repository

import com.gec.emergency.data.remote.dto.IncidentCategoryResponseDto
import com.gec.emergency.data.remote.dto.toDomainModel
import com.gec.emergency.data.remote.dto.toRequestDto
import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.IncidentReport
import com.gec.emergency.domain.model.IncidentSeverity
import com.gec.emergency.domain.model.ReportMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteIncidentReportRepositoryTest {

    @Test
    fun categoryResponseDto_toDomainModel_mapsCorrectly() {
        val dto = IncidentCategoryResponseDto(
            id = "cat_fire",
            code = "FIRE",
            name = "Fire & Explosion",
            description = "Fires",
            defaultSeverity = "HIGH",
            supportsSilent = true,
            supportsGeneral = true,
            supportsCall = true,
            defaultAgencyTypes = setOf("FIRE", "AMBULANCE"),
        )

        val domain = dto.toDomainModel()

        assertEquals("cat_fire", domain.id)
        assertEquals("Fire & Explosion", domain.name)
        assertEquals(IncidentSeverity.HIGH, domain.severity)
        assertTrue(ReportMode.SILENT in domain.reportModeSupported)
        assertTrue(AgencyType.FIRE in domain.defaultAgencyTypes)
        assertTrue(AgencyType.AMBULANCE in domain.defaultAgencyTypes)
    }

    @Test
    fun reportRequestDto_containsRequiredFields() {
        val report = IncidentReport(
            id = "rpt_01",
            categoryId = "cat_robbery",
            mode = ReportMode.SILENT,
            description = "Silent report notes",
        )

        val dto = report.toRequestDto(clientRequestId = "req_123")

        assertEquals("cat_robbery", dto.categoryId)
        assertEquals("SILENT", dto.reportMode)
        assertEquals("Silent report notes", dto.description)
        assertEquals("req_123", dto.clientGeneratedRequestId)
    }
}
