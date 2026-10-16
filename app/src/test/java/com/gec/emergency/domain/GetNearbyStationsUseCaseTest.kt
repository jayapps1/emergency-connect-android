package com.gec.emergency.domain

import com.gec.emergency.domain.model.GeoLocation
import com.gec.emergency.domain.model.NearbyStation
import com.gec.emergency.domain.model.NearbyStationQuery
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact
import com.gec.emergency.domain.repository.StationRepository
import com.gec.emergency.domain.usecase.GetNearbyStationsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetNearbyStationsUseCaseTest {

    private val userLocation = GeoLocation(latitude = 5.5500, longitude = -0.2000)

    private val fakeRepository = object : StationRepository {
        override fun getStationsByAgency(agencyId: String): Flow<List<Station>> = flowOf(emptyList())
        override suspend fun getStationById(stationId: String): Station? = null
        override fun getContactsForStation(stationId: String): Flow<List<StationContact>> = flowOf(emptyList())

        override fun getNearbyStations(query: NearbyStationQuery): Flow<List<NearbyStation>> {
            val s1 = Station(
                id = "st_near",
                agencyId = "gnfs",
                name = "Near Station",
                code = "S1",
                region = "Greater Accra",
                district = "Accra",
                latitude = 5.5510,
                longitude = -0.2000,
                isActive = true,
                isVerified = true,
            )
            val s2 = Station(
                id = "st_far",
                agencyId = "gnfs",
                name = "Far Station",
                code = "S2",
                region = "Greater Accra",
                district = "Accra",
                latitude = 5.8000,
                longitude = -0.2000,
                isActive = true,
                isVerified = true,
            )

            val results = listOf(
                NearbyStation(station = s1, distanceKm = 0.11),
                NearbyStation(station = s2, distanceKm = 27.8),
            )

            val filtered = results
                .filter { if (query.agencyId != null) it.station.agencyId == query.agencyId else true }
                .filter { it.distanceKm <= query.radiusKm }
                .sortedBy { it.distanceKm }
                .take(query.limit)

            return flowOf(filtered)
        }
    }

    private val useCase = GetNearbyStationsUseCase(fakeRepository)

    @Test
    fun invoke_radiusFilter_returnsOnlyStationsWithinRadius() = runBlocking {
        val query = NearbyStationQuery(
            location = userLocation,
            radiusKm = 10.0,
        )

        val result = useCase(query).first()

        assertEquals(1, result.size)
        assertEquals("st_near", result[0].station.id)
    }

    @Test
    fun invoke_ordering_returnsNearestFirst() = runBlocking {
        val query = NearbyStationQuery(
            location = userLocation,
            radiusKm = 50.0,
        )

        val result = useCase(query).first()

        assertEquals(2, result.size)
        assertEquals("st_near", result[0].station.id)
        assertEquals("st_far", result[1].station.id)
    }
}
