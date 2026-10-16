package com.gec.emergency.domain.service

import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.IncidentCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultIncidentRoutingService @Inject constructor() : IncidentRoutingService {
    override fun routeCategory(category: IncidentCategory): Set<AgencyType> {
        return category.defaultAgencyTypes
    }
}
