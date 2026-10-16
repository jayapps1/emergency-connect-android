package com.gec.emergency.domain.repository

import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.OfflineEmergencyContact
import com.gec.emergency.domain.model.OfflineRegionalHeadquartersContact
import com.gec.emergency.domain.model.RegionRef
import kotlinx.coroutines.flow.Flow

interface OfflineEmergencyDirectoryRepository {
    fun getEmergencyContacts(): Flow<List<OfflineEmergencyContact>>
    fun getPrimaryNationalEmergencyContact(): Flow<OfflineEmergencyContact?>
    fun getContactsByAgencyType(type: AgencyType): Flow<List<OfflineEmergencyContact>>
    fun getRegionalHeadquartersContacts(regionName: String? = null): Flow<List<OfflineRegionalHeadquartersContact>>
    fun getRegions(): Flow<List<RegionRef>>
    suspend fun syncOfflineDirectory(): Boolean
}
