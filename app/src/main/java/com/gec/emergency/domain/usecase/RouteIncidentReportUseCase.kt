package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.IncidentCategory
import com.gec.emergency.domain.model.IncidentReport
import com.gec.emergency.domain.model.IncidentRoutingResult
import com.gec.emergency.domain.model.ReportAssignment
import com.gec.emergency.domain.service.IncidentRoutingService
import java.util.UUID
import javax.inject.Inject

class RouteIncidentReportUseCase @Inject constructor(
    private val routingService: IncidentRoutingService,
) {
    operator fun invoke(
        report: IncidentReport,
        category: IncidentCategory,
    ): IncidentRoutingResult {
        val targetAgencyTypes = routingService.routeCategory(category)
        val assignments = targetAgencyTypes.map { agencyType ->
            ReportAssignment(
                id = "assign_${UUID.randomUUID()}",
                reportId = report.id,
                agencyType = agencyType,
            )
        }

        return IncidentRoutingResult(
            reportId = report.id,
            targetAgencyTypes = targetAgencyTypes,
            assignments = assignments,
        )
    }
}
