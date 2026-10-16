package com.gec.emergency.domain

import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.IncidentCategory
import com.gec.emergency.domain.model.IncidentReport
import com.gec.emergency.domain.model.IncidentSeverity
import com.gec.emergency.domain.model.ReportMode
import com.gec.emergency.domain.service.DefaultIncidentRoutingService
import com.gec.emergency.domain.usecase.RouteIncidentReportUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteIncidentReportUseCaseTest {

    private val routingService = DefaultIncidentRoutingService()
    private val useCase = RouteIncidentReportUseCase(routingService)

    @Test
    fun invoke_multiAgencyCategory_createsAssignmentsForEachAgencyType() {
        val category = IncidentCategory(
            id = "cat_accident",
            name = "Road Accident",
            severity = IncidentSeverity.HIGH,
            reportModeSupported = setOf(ReportMode.SILENT),
            defaultAgencyTypes = setOf(AgencyType.POLICE, AgencyType.AMBULANCE, AgencyType.FIRE),
        )

        val report = IncidentReport(
            id = "rpt_acc_01",
            categoryId = category.id,
            mode = ReportMode.SILENT,
        )

        val result = useCase(report, category)

        assertEquals(3, result.targetAgencyTypes.size)
        assertEquals(3, result.assignments.size)
        assertEquals(setOf(AgencyType.POLICE, AgencyType.AMBULANCE, AgencyType.FIRE), result.targetAgencyTypes)
    }
}
