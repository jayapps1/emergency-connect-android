package com.gec.emergency.data.repository

import com.gec.emergency.data.mapper.toDomainModel
import com.gec.emergency.data.remote.EmergencyDirectoryApi
import com.gec.emergency.domain.model.GeoLocation
import com.gec.emergency.domain.model.NearbyStation
import com.gec.emergency.domain.model.NearbyStationQuery
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact
import com.gec.emergency.domain.repository.StationRepository
import com.gec.emergency.domain.service.DistanceCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StationRepositoryImpl @Inject constructor(
    private val api: EmergencyDirectoryApi,
    private val distanceCalculator: DistanceCalculator,
) : StationRepository {

    override fun getStationsByAgency(agencyId: String): Flow<List<Station>> = flow {
        val response = api.getStationsByAgency(agencyId)
        if (response.isSuccessful && response.body() != null) {
            val stations = response.body()!!.map { it.toDomainModel() }.filter { it.isActive && it.isVerified }
            emit(stations)
        } else {
            throw IllegalStateException("Unable to load stations. Server status: ${response.code()}")
        }
    }

    override suspend fun getStationById(stationId: String): Station? {
        val response = api.getStationDetails(stationId)
        return if (response.isSuccessful && response.body() != null) {
            response.body()!!.station.toDomainModel()
        } else {
            null
        }
    }

    override fun getContactsForStation(stationId: String): Flow<List<StationContact>> = flow {
        val response = api.getStationContacts(stationId)
        if (response.isSuccessful && response.body() != null) {
            val contacts = response.body()!!.map { it.toDomainModel() }.filter { it.isPublic && it.isActive && it.isVerified }
            emit(contacts)
        } else {
            throw IllegalStateException("Unable to load station contacts.")
        }
    }

    override fun getNearbyStations(query: NearbyStationQuery): Flow<List<NearbyStation>> = flow {
        val allStations = if (query.agencyId.isNullOrEmpty()) {
            val agenciesResp = api.getAgencies()
            val stationsList = mutableListOf<Station>()
            if (agenciesResp.isSuccessful && agenciesResp.body() != null) {
                agenciesResp.body()!!.forEach { agencyDto ->
                    val resp = api.getStationsByAgency(agencyDto.id)
                    if (resp.isSuccessful && resp.body() != null) {
                        stationsList.addAll(resp.body()!!.map { it.toDomainModel() })
                    }
                }
            }
            stationsList
        } else {
            val resp = api.getStationsByAgency(query.agencyId)
            if (resp.isSuccessful && resp.body() != null) {
                resp.body()!!.map { it.toDomainModel() }
            } else {
                emptyList()
            }
        }

        val hasValidLocation = query.location.latitude != 0.0 || query.location.longitude != 0.0

        val stations = allStations
            .filter { it.isActive && it.isVerified }
            .map { station ->
                val distance = if (hasValidLocation) {
                    distanceCalculator.distanceKm(query.location, GeoLocation(station.latitude, station.longitude))
                } else {
                    0.0
                }
                NearbyStation(station = station, distanceKm = distance)
            }

        val sortedStations = if (hasValidLocation) {
            stations.sortedBy { it.distanceKm }
        } else {
            stations.sortedWith(compareBy({ it.station.region }, { it.station.district }, { it.station.name }))
        }

        emit(sortedStations.take(query.limit))
    }
}
