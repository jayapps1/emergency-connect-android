package com.gec.emergency.domain

import com.gec.emergency.domain.model.NearbyStation
import com.gec.emergency.domain.model.NearbyStationQuery
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact
import com.gec.emergency.domain.model.StationContactType
import com.gec.emergency.domain.repository.StationRepository
import com.gec.emergency.domain.usecase.GetPublicStationContactsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetPublicStationContactsUseCaseTest {

    private val fakeRepository = object : StationRepository {
        override fun getStationsByAgency(agencyId: String): Flow<List<Station>> = flowOf(emptyList())
        override suspend fun getStationById(stationId: String): Station? = null
        override fun getNearbyStations(query: NearbyStationQuery): Flow<List<NearbyStation>> = flowOf(emptyList())

        override fun getContactsForStation(stationId: String): Flow<List<StationContact>> {
            return flowOf(
                listOf(
                    StationContact(
                        id = "sc_01",
                        stationId = stationId,
                        phoneNumber = "+233302000002",
                        type = StationContactType.CONTROL_ROOM,
                        priority = 2,
                        isPublic = true,
                        isActive = true,
                        isVerified = true,
                    ),
                    StationContact(
                        id = "sc_02",
                        stationId = stationId,
                        phoneNumber = "+233302000001",
                        type = StationContactType.WATCHROOM,
                        priority = 1,
                        isPublic = true,
                        isActive = true,
                        isVerified = true,
                    ),
                    StationContact(
                        id = "sc_private",
                        stationId = stationId,
                        phoneNumber = "+233302000003",
                        type = StationContactType.ALTERNATIVE,
                        priority = 3,
                        isPublic = false,
                        isActive = true,
                        isVerified = true,
                    ),
                    StationContact(
                        id = "sc_inactive",
                        stationId = stationId,
                        phoneNumber = "+233302000004",
                        type = StationContactType.ADMINISTRATIVE,
                        priority = 4,
                        isPublic = true,
                        isActive = false,
                        isVerified = true,
                    ),
                    StationContact(
                        id = "sc_unverified",
                        stationId = stationId,
                        phoneNumber = "+233302000005",
                        type = StationContactType.OTHER,
                        priority = 5,
                        isPublic = true,
                        isActive = true,
                        isVerified = false,
                    ),
                )
            )
        }
    }

    private val useCase = GetPublicStationContactsUseCase(fakeRepository)

    @Test
    fun invoke_filtersPublicActiveVerifiedAndSortsByPriority() = runBlocking {
        val result = useCase("st_01").first()

        assertEquals(2, result.size)
        assertEquals("sc_02", result[0].id) // priority 1
        assertEquals("sc_01", result[1].id) // priority 2
    }
}
