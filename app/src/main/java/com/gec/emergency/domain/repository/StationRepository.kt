package com.gec.emergency.domain.repository

import com.gec.emergency.domain.model.NearbyStation
import com.gec.emergency.domain.model.NearbyStationQuery
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact
import kotlinx.coroutines.flow.Flow

interface StationRepository {
    fun getStationsByAgency(agencyId: String): Flow<List<Station>>
    suspend fun getStationById(stationId: String): Station?
    fun getContactsForStation(stationId: String): Flow<List<StationContact>>
    fun getNearbyStations(query: NearbyStationQuery): Flow<List<NearbyStation>>
}
