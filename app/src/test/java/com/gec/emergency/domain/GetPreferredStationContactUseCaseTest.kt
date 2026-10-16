package com.gec.emergency.domain

import com.gec.emergency.domain.model.NearbyStation
import com.gec.emergency.domain.model.NearbyStationQuery
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact
import com.gec.emergency.domain.model.StationContactType
import com.gec.emergency.domain.repository.StationRepository
import com.gec.emergency.domain.usecase.GetPreferredStationContactUseCase
import com.gec.emergency.domain.usecase.GetPublicStationContactsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GetPreferredStationContactUseCaseTest {

    private val fakeRepository = object : StationRepository {
        override fun getStationsByAgency(agencyId: String): Flow<List<Station>> = flowOf(emptyList())
        override suspend fun getStationById(stationId: String): Station? = null
        override fun getNearbyStations(query: NearbyStationQuery): Flow<List<NearbyStation>> = flowOf(emptyList())

        override fun getContactsForStation(stationId: String): Flow<List<StationContact>> {
            return flowOf(
                listOf(
                    StationContact(
                        id = "sc_secondary",
                        stationId = stationId,
                        phoneNumber = "+233302000002",
                        type = StationContactType.CONTROL_ROOM,
                        priority = 2,
                        isPublic = true,
                        isActive = true,
                        isVerified = true,
                    ),
                    StationContact(
                        id = "sc_primary",
                        stationId = stationId,
                        phoneNumber = "+233302000001",
                        type = StationContactType.WATCHROOM,
                        priority = 1,
                        isPublic = true,
                        isActive = true,
                        isVerified = true,
                    ),
                )
            )
        }
    }

    private val getPublicStationContactsUseCase = GetPublicStationContactsUseCase(fakeRepository)
    private val useCase = GetPreferredStationContactUseCase(getPublicStationContactsUseCase)

    @Test
    fun invoke_returnsPrimaryTopPriorityContact() = runBlocking {
        val preferredContact = useCase("st_01").first()

        assertNotNull(preferredContact)
        assertEquals("sc_primary", preferredContact?.id)
        assertEquals(1, preferredContact?.priority)
    }
}
