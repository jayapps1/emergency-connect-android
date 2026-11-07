package com.gec.emergency.domain.repository

import com.gec.emergency.data.remote.dto.LiveTrackingRequestDto
import com.gec.emergency.domain.model.GeoLocation
import com.gec.emergency.domain.model.NearbyStation
import com.gec.emergency.domain.model.NearbyStationQuery
import com.gec.emergency.domain.model.SafetyInformation
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface StationRepository {
    fun getStationsByAgency(agencyId: String): Flow<List<Station>>
    suspend fun getStationById(stationId: String): Station?
    fun getContactsForStation(stationId: String): Flow<List<StationContact>>
    fun getNearbyStations(query: NearbyStationQuery): Flow<List<NearbyStation>>
    fun getPublicSafetyInformation(): Flow<List<SafetyInformation>> = flowOf(emptyList())
    suspend fun getUnreadNotificationCount(): Long = 0L
    suspend fun markNotificationAsRead(notificationId: String) {}
    suspend fun createEmergencyCallAttempt(
        agencyId: String,
        stationId: String?,
        stationContactId: String?,
        destinationType: String?,
        location: GeoLocation?,
    ): String? = null
    suspend fun markCallLaunched(callAttemptId: String): Boolean = false
    suspend fun getPendingLiveTrackingRequests(reportId: String): List<LiveTrackingRequestDto> = emptyList()
    suspend fun respondToLiveTrackingRequest(requestId: String, action: String): Boolean = false
    suspend fun createLocationSession(reportId: String): String? = null
}
