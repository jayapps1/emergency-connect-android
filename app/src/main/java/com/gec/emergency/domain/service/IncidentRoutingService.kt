package com.gec.emergency.domain.service

import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.IncidentCategory

interface IncidentRoutingService {
    fun routeCategory(category: IncidentCategory): Set<AgencyType>
}
