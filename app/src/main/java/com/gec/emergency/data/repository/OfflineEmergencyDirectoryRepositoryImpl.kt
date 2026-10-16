package com.gec.emergency.data.repository

import com.gec.emergency.data.local.OfflineEmergencyDirectoryDataSource
import com.gec.emergency.data.local.OfflineEmergencyDirectoryStore
import com.gec.emergency.data.mapper.toDomainModel
import com.gec.emergency.data.remote.EmergencyDirectoryApi
import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.OfflineEmergencyContact
import com.gec.emergency.domain.model.OfflineRegionalHeadquartersContact
import com.gec.emergency.domain.model.RegionRef
import com.gec.emergency.domain.repository.OfflineEmergencyDirectoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineEmergencyDirectoryRepositoryImpl @Inject constructor(
    private val dataSource: OfflineEmergencyDirectoryDataSource,
    private val api: EmergencyDirectoryApi,
    private val store: OfflineEmergencyDirectoryStore,
) : OfflineEmergencyDirectoryRepository {

    private val defaultCanonicalRegions = listOf(
        RegionRef("reg_ahafo", "AH", "Ahafo"),
        RegionRef("reg_ashanti", "AS", "Ashanti"),
        RegionRef("reg_bono", "BO", "Bono"),
        RegionRef("reg_bono_east", "BE", "Bono East"),
        RegionRef("reg_central", "CR", "Central"),
        RegionRef("reg_eastern", "ER", "Eastern"),
        RegionRef("reg_greater_accra", "GAR", "Greater Accra"),
        RegionRef("reg_north_east", "NE", "North East"),
        RegionRef("reg_northern", "NR", "Northern"),
        RegionRef("reg_oti", "OR", "Oti"),
        RegionRef("reg_savannah", "SR", "Savannah"),
        RegionRef("reg_upper_east", "UE", "Upper East"),
        RegionRef("reg_upper_west", "UW", "Upper West"),
        RegionRef("reg_volta", "VR", "Volta"),
        RegionRef("reg_western", "WR", "Western"),
        RegionRef("reg_western_north", "WN", "Western North"),
    )

    override fun getEmergencyContacts(): Flow<List<OfflineEmergencyContact>> = flow {
        emit(dataSource.getContacts().filter { it.isActive }.sortedBy { it.priority })
    }

    override fun getPrimaryNationalEmergencyContact(): Flow<OfflineEmergencyContact?> = flow {
        val contacts = dataSource.getContacts().filter { it.isActive && it.isNational }
        emit(contacts.minByOrNull { it.priority })
    }

    override fun getContactsByAgencyType(type: AgencyType): Flow<List<OfflineEmergencyContact>> = flow {
        emit(dataSource.getContacts().filter { it.isActive && it.agencyType == type }.sortedBy { it.priority })
    }

    override fun getRegionalHeadquartersContacts(regionName: String?): Flow<List<OfflineRegionalHeadquartersContact>> = flow {
        val cached = store.getHeadquartersContacts()
        val filtered = if (regionName.isNullOrBlank()) {
            cached
        } else {
            cached.filter { it.regionName.equals(regionName.trim(), ignoreCase = true) || it.regionName.contains(regionName.trim(), ignoreCase = true) }
        }
        emit(filtered.sortedWith(compareBy({ it.agencyName }, { it.priority })))
    }

    override fun getRegions(): Flow<List<RegionRef>> = flow {
        val cached = store.getRegions()
        if (cached.isNotEmpty()) {
            emit(cached.sortedBy { it.name })
        } else {
            emit(defaultCanonicalRegions)
        }
    }

    override suspend fun syncOfflineDirectory(): Boolean {
        return try {
            val agenciesResp = api.getAgencies()
            val agenciesMap = if (agenciesResp.isSuccessful && agenciesResp.body() != null) {
                agenciesResp.body()!!.map { it.toDomainModel() }.associateBy { it.id }
            } else {
                emptyMap()
            }

            val hqResp = api.getPublicHeadquarters()
            if (hqResp.isSuccessful && hqResp.body() != null) {
                val hqList = hqResp.body()!!
                val rhqContacts = mutableListOf<OfflineRegionalHeadquartersContact>()

                for (details in hqList) {
                    val station = details.station.toDomainModel()
                    val contacts = details.contacts.map { it.toDomainModel() }.filter { it.isPublic && it.isActive && it.isVerified }
                    val agency = agenciesMap[station.agencyId]

                    val agencyCode = agency?.code ?: "EMERGENCY"
                    val agencyName = agency?.name ?: "Emergency Service"
                    val agencyType = agency?.type ?: AgencyType.OTHER

                    for (c in contacts) {
                        rhqContacts.add(
                            OfflineRegionalHeadquartersContact(
                                id = c.id,
                                agencyId = station.agencyId,
                                agencyCode = agencyCode,
                                agencyName = agencyName,
                                agencyType = agencyType,
                                regionId = station.regionId,
                                regionName = station.region,
                                stationId = station.id,
                                stationName = station.name,
                                stationLevel = station.stationLevel ?: "RHQ",
                                phoneNumber = c.phoneNumber,
                                contactType = c.type.name,
                                priority = c.priority,
                                isAvailable24Hour = c.is24Hour,
                                lastSuccessfulSyncAt = System.currentTimeMillis(),
                            )
                        )
                    }
                }

                store.saveHeadquartersContacts(rhqContacts)
            }

            val regionsResp = api.getPublicRegions()
            if (regionsResp.isSuccessful && regionsResp.body() != null) {
                val fetchedRegions = regionsResp.body()!!.map {
                    RegionRef(id = it.id, code = it.code, name = it.name)
                }
                store.saveRegions(fetchedRegions)
            }

            true
        } catch (_: Exception) {
            // Keep existing cache on network error
            false
        }
    }
}
