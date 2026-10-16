package com.gec.emergency.domain.repository

import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.OfflineEmergencyContact
import kotlinx.coroutines.flow.Flow

interface OfflineEmergencyDirectoryRepository {
    fun getEmergencyContacts(): Flow<List<OfflineEmergencyContact>>
    fun getPrimaryNationalEmergencyContact(): Flow<OfflineEmergencyContact?>
    fun getContactsByAgencyType(type: AgencyType): Flow<List<OfflineEmergencyContact>>
}
