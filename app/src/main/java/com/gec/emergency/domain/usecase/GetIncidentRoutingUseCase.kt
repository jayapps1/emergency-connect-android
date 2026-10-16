package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.IncidentCategory
import com.gec.emergency.domain.service.IncidentRoutingService
import javax.inject.Inject

class GetIncidentRoutingUseCase @Inject constructor(
    private val routingService: IncidentRoutingService,
) {
    operator fun invoke(category: IncidentCategory): Set<AgencyType> {
        return routingService.routeCategory(category)
    }
}
