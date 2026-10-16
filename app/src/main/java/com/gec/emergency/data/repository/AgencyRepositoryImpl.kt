package com.gec.emergency.data.repository

import com.gec.emergency.data.mapper.toDomainModel
import com.gec.emergency.data.remote.EmergencyDirectoryApi
import com.gec.emergency.domain.model.Agency
import com.gec.emergency.domain.repository.AgencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgencyRepositoryImpl @Inject constructor(
    private val api: EmergencyDirectoryApi,
) : AgencyRepository {

    override fun getActiveAgencies(): Flow<List<Agency>> = flow {
        val response = api.getAgencies()
        if (response.isSuccessful && response.body() != null) {
            val agencies = response.body()!!.map { it.toDomainModel() }.filter { it.isActive && it.isVerified }
            emit(agencies)
        } else {
            throw IllegalStateException("Unable to load emergency services. Server status: ${response.code()}")
        }
    }

    override suspend fun getAgencyById(id: String): Agency? {
        val response = api.getAgency(id)
        return if (response.isSuccessful && response.body() != null) {
            response.body()!!.toDomainModel()
        } else {
            null
        }
    }
}
