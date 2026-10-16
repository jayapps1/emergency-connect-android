package com.gec.emergency.domain

import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.NearbyStation
import com.gec.emergency.domain.model.NearbyStationQuery
import com.gec.emergency.domain.model.OfflineEmergencyContact
import com.gec.emergency.domain.model.OfflineRegionalHeadquartersContact
import com.gec.emergency.domain.model.RegionRef
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact
import com.gec.emergency.domain.model.StationContactType
import com.gec.emergency.domain.repository.OfflineEmergencyDirectoryRepository
import com.gec.emergency.domain.repository.StationRepository
import com.gec.emergency.domain.usecase.GetEmergencyCallFallbackUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetEmergencyCallFallbackUseCaseTest {

    private val sampleStation = Station(
        id = "st_komenda_fire",
        agencyId = "ag_gnfs",
        name = "Komenda Fire Station",
        code = "KFS01",
        region = "Central",
        district = "Komenda-Edina-Eguafo-Abirem",
        latitude = 5.093842,
        longitude = -1.240317,
    )

    @Test
    fun testA_stationContactAvailable_returnsStationContact() = runBlocking {
        val fakeStationRepo = object : StationRepository {
            override fun getStationsByAgency(agencyId: String) = flowOf(emptyList<Station>())
            override suspend fun getStationById(stationId: String) = sampleStation
            override fun getContactsForStation(stationId: String): Flow<List<StationContact>> {
                return flowOf(
                    listOf(
                        StationContact(
                            id = "c1",
                            stationId = stationId,
                            phoneNumber = "0332190001",
                            type = StationContactType.WATCHROOM,
                            priority = 1,
                        )
                    )
                )
            }
            override fun getNearbyStations(query: NearbyStationQuery) = flowOf(emptyList<NearbyStation>())
        }

        val fakeOfflineRepo = createFakeOfflineRepo(rhqAvailable = true, shortCodeAvailable = true)
        val useCase = GetEmergencyCallFallbackUseCase(fakeStationRepo, fakeOfflineRepo)

        val target = useCase(sampleStation, AgencyType.FIRE)

        assertEquals("0332190001", target.phoneNumber)
        assertEquals("Local Station Direct", target.tierDescription)
    }

    @Test
    fun testB_stationContactUnavailable_rhqAvailable_returnsRhqContact() = runBlocking {
        val fakeStationRepo = object : StationRepository {
            override fun getStationsByAgency(agencyId: String) = flowOf(emptyList<Station>())
            override suspend fun getStationById(stationId: String) = sampleStation
            override fun getContactsForStation(stationId: String) = flowOf(emptyList<StationContact>())
            override fun getNearbyStations(query: NearbyStationQuery) = flowOf(emptyList<NearbyStation>())
        }

        val fakeOfflineRepo = createFakeOfflineRepo(rhqAvailable = true, shortCodeAvailable = true)
        val useCase = GetEmergencyCallFallbackUseCase(fakeStationRepo, fakeOfflineRepo)

        val target = useCase(sampleStation, AgencyType.FIRE)

        assertEquals("0332001122", target.phoneNumber)
        assertEquals("Regional Headquarters Fallback", target.tierDescription)
    }

    @Test
    fun testC_stationAndRhqUnavailable_returnsAgencyShortCode() = runBlocking {
        val fakeStationRepo = object : StationRepository {
            override fun getStationsByAgency(agencyId: String) = flowOf(emptyList<Station>())
            override suspend fun getStationById(stationId: String) = sampleStation
            override fun getContactsForStation(stationId: String) = flowOf(emptyList<StationContact>())
            override fun getNearbyStations(query: NearbyStationQuery) = flowOf(emptyList<NearbyStation>())
        }

        val fakeOfflineRepo = createFakeOfflineRepo(rhqAvailable = false, shortCodeAvailable = true)
        val useCase = GetEmergencyCallFallbackUseCase(fakeStationRepo, fakeOfflineRepo)

        val target = useCase(sampleStation, AgencyType.FIRE)

        assertEquals("192", target.phoneNumber)
        assertEquals("Agency Emergency Short Code", target.tierDescription)
    }

    @Test
    fun testD_allUnavailable_returns112NationalEmergency() = runBlocking {
        val fakeStationRepo = object : StationRepository {
            override fun getStationsByAgency(agencyId: String) = flowOf(emptyList<Station>())
            override suspend fun getStationById(stationId: String) = sampleStation
            override fun getContactsForStation(stationId: String) = flowOf(emptyList<StationContact>())
            override fun getNearbyStations(query: NearbyStationQuery) = flowOf(emptyList<NearbyStation>())
        }

        val fakeOfflineRepo = createFakeOfflineRepo(rhqAvailable = false, shortCodeAvailable = false)
        val useCase = GetEmergencyCallFallbackUseCase(fakeStationRepo, fakeOfflineRepo)

        val target = useCase(sampleStation, AgencyType.FIRE)

        assertEquals("112", target.phoneNumber)
        assertEquals("National Emergency Fallback (112)", target.tierDescription)
    }

    private fun createFakeOfflineRepo(rhqAvailable: Boolean, shortCodeAvailable: Boolean): OfflineEmergencyDirectoryRepository {
        return object : OfflineEmergencyDirectoryRepository {
            override fun getEmergencyContacts() = flowOf(emptyList<OfflineEmergencyContact>())
            override fun getPrimaryNationalEmergencyContact() = flowOf(null)
            override fun getContactsByAgencyType(type: AgencyType): Flow<List<OfflineEmergencyContact>> {
                return if (shortCodeAvailable && type == AgencyType.FIRE) {
                    flowOf(
                        listOf(
                            OfflineEmergencyContact(
                                id = "gnfs_192",
                                agencyType = AgencyType.FIRE,
                                label = "Fire Service Short Code",
                                phoneNumber = "192",
                                priority = 1,
                            )
                        )
                    )
                } else {
                    flowOf(emptyList())
                }
            }

            override fun getRegionalHeadquartersContacts(regionName: String?): Flow<List<OfflineRegionalHeadquartersContact>> {
                return if (rhqAvailable) {
                    flowOf(
                        listOf(
                            OfflineRegionalHeadquartersContact(
                                id = "rhq_c1",
                                agencyId = "ag_gnfs",
                                agencyCode = "GNFS",
                                agencyName = "Ghana National Fire Service",
                                agencyType = AgencyType.FIRE,
                                regionName = "Central",
                                stationId = "st_rhq_central_fire",
                                stationName = "GNFS Central Regional Headquarters",
                                phoneNumber = "0332001122",
                                priority = 1,
                            )
                        )
                    )
                } else {
                    flowOf(emptyList())
                }
            }

            override fun getRegions(): Flow<List<RegionRef>> = flowOf(emptyList())
            override suspend fun syncOfflineDirectory(): Boolean = true
        }
    }
}
