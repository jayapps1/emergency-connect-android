package com.gec.emergency.domain.repository

import com.gec.emergency.domain.model.Agency
import kotlinx.coroutines.flow.Flow

interface AgencyRepository {
    fun getActiveAgencies(): Flow<List<Agency>>
    suspend fun getAgencyById(id: String): Agency?
}
