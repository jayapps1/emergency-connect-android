package com.gec.emergency.data.repository

import com.gec.emergency.data.local.ReportTrackingStore
import com.gec.emergency.data.mapper.toDomainModel
import com.gec.emergency.data.remote.EmergencyDirectoryApi
import com.gec.emergency.data.remote.dto.CreateEmergencyCallAttemptRequestDto
import com.gec.emergency.data.remote.dto.CreateLocationSessionRequestDto
import com.gec.emergency.data.remote.dto.LiveTrackingRequestDto
import com.gec.emergency.domain.model.GeoLocation
import com.gec.emergency.domain.model.NearbyStation
import com.gec.emergency.domain.model.NearbyStationQuery
import com.gec.emergency.domain.model.SafetyInformation
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact
import com.gec.emergency.domain.repository.StationRepository
import com.gec.emergency.domain.service.DistanceCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A successful empty station response and a failed station request have
 * different operational meanings. Network/API failures must never be
 * converted into an empty directory because doing so falsely tells citizens
 * that emergency stations do not exist.
 */
@Singleton
class StationRepositoryImpl @Inject constructor(
    private val api: EmergencyDirectoryApi,
    private val distanceCalculator: DistanceCalculator,
    private val trackingStore: ReportTrackingStore,
) : StationRepository {

    // Resolves agency UUID and loads published active and verified stations for the specified agency.
    override fun getStationsByAgency(agencyId: String): Flow<List<Station>> = flow {
        val targetUuid = resolveAgencyUuid(agencyId)
        val response = api.getStationsByAgency(targetUuid)
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

    // Retrieves published public contact numbers for an active verified station.
    override fun getContactsForStation(stationId: String): Flow<List<StationContact>> = flow {
        val response = api.getStationContacts(stationId)
        if (response.isSuccessful && response.body() != null) {
            val contacts = response.body()!!.map { it.toDomainModel() }.filter { it.isPublic && it.isActive && it.isVerified }
            emit(contacts)
        } else {
            throw IllegalStateException("Unable to load station contacts.")
        }
    }

    // Combines verified stations from selected agencies, calculates their geographic distance from the citizen, and sorts them nearest-first.
    override fun getNearbyStations(query: NearbyStationQuery): Flow<List<NearbyStation>> = flow {
        val allStations = if (query.agencyId.isNullOrEmpty() || query.agencyId.equals("ALL", ignoreCase = true)) {
            val resp = api.getAllPublicStations()
            if (resp.isSuccessful && resp.body() != null) {
                resp.body()!!.map { it.toDomainModel() }
            } else {
                throw IllegalStateException("Unable to load public emergency stations. Server status: ${resp.code()}")
            }
        } else {
            val targetUuid = resolveAgencyUuid(query.agencyId)
            val resp = api.getStationsByAgency(targetUuid)
            if (resp.isSuccessful && resp.body() != null) {
                resp.body()!!.map { it.toDomainModel() }
            } else {
                throw IllegalStateException("Unable to load stations for selected agency. Server status: ${resp.code()}")
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

        emit(if (query.limit > 0) sortedStations.take(query.limit) else sortedStations)
    }

    override fun getPublicSafetyInformation(): Flow<List<SafetyInformation>> = flow {
        val response = api.getPublicSafetyInformation()
        if (response.isSuccessful && response.body() != null) {
            val list = response.body()!!.map { dto ->
                SafetyInformation(
                    id = dto.id,
                    sourceType = dto.sourceType,
                    agencyId = dto.agencyId,
                    agencyName = dto.agencyName,
                    agencyCode = dto.agencyCode,
                    title = dto.title,
                    content = dto.content,
                    category = dto.category,
                    severity = dto.severity,
                    publishedAt = dto.publishedAt,
                )
            }
            emit(list)
        } else {
            throw IllegalStateException("Unable to load public safety information. Server status: ${response.code()}")
        }
    }

    override suspend fun getUnreadNotificationCount(): Long {
        return try {
            val response = api.getUnreadNotificationCount()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!["unreadCount"] ?: 0L
            } else {
                0L
            }
        } catch (_: Exception) {
            0L
        }
    }

    override suspend fun markNotificationAsRead(notificationId: String) {
        try {
            api.markNotificationAsRead(notificationId)
        } catch (_: Exception) {}
    }

    // Records the citizen's emergency call attempt and initiation location before cellular handoff.
    override suspend fun createEmergencyCallAttempt(
        agencyId: String,
        stationId: String?,
        stationContactId: String?,
        destinationType: String?,
        location: GeoLocation?,
    ): String? {
        return try {
            val resolvedAgencyUuid = resolveAgencyUuid(agencyId)
            val installationId = trackingStore.getOrCreateInstallationId()
            val authToken = trackingStore.getPublicAuthToken()
            val bearerToken = if (!authToken.isNullOrBlank()) "Bearer $authToken" else null

            val req = CreateEmergencyCallAttemptRequestDto(
                agencyId = resolvedAgencyUuid,
                stationId = stationId,
                stationContactId = stationContactId,
                destinationType = destinationType ?: "STATION_CONTACT",
                latitude = location?.latitude,
                longitude = location?.longitude,
                accuracyMeters = location?.accuracyMeters,
                clientGeneratedRequestId = UUID.randomUUID().toString(),
            )

            val resp = api.createEmergencyCallAttempt(installationId, bearerToken, req)
            if (resp.isSuccessful && resp.body() != null) {
                resp.body()!!.id
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun markCallLaunched(callAttemptId: String): Boolean {
        return try {
            val resp = api.markCallLaunched(callAttemptId)
            resp.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun getPendingLiveTrackingRequests(reportId: String): List<LiveTrackingRequestDto> {
        return try {
            val resp = api.getRequestsForReport(reportId)
            if (resp.isSuccessful && resp.body() != null) {
                resp.body()!!.filter { it.status == "PENDING" }
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun respondToLiveTrackingRequest(requestId: String, action: String): Boolean {
        return try {
            val resp = api.respondToLiveTrackingRequest(requestId, action)
            resp.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun createLocationSession(reportId: String): String? {
        return try {
            val installationId = trackingStore.getOrCreateInstallationId()
            val req = CreateLocationSessionRequestDto(
                reportId = reportId,
                trackingPolicy = "LIVE_OPTIONAL",
            )
            val resp = api.createLocationSession(installationId, req)
            if (resp.isSuccessful && resp.body() != null) {
                resp.body()!!.sessionId
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun resolveAgencyUuid(agencyIdentifier: String): String {
        return try {
            UUID.fromString(agencyIdentifier).toString()
        } catch (_: Exception) {
            val agenciesResp = api.getAgencies()
            if (agenciesResp.isSuccessful && agenciesResp.body() != null) {
                val match = agenciesResp.body()!!.firstOrNull { agency ->
                    agency.id.equals(agencyIdentifier, ignoreCase = true) ||
                    agency.code.equals(agencyIdentifier, ignoreCase = true) ||
                    agency.type.equals(agencyIdentifier, ignoreCase = true) ||
                    (agencyIdentifier.startsWith("GNFS") && agency.type.equals("FIRE", ignoreCase = true)) ||
                    (agencyIdentifier.startsWith("GPS") && agency.type.equals("POLICE", ignoreCase = true)) ||
                    (agencyIdentifier.startsWith("NAS") && agency.type.equals("AMBULANCE", ignoreCase = true))
                }
                match?.id ?: agencyIdentifier
            } else {
                agencyIdentifier
            }
        }
    }
}
