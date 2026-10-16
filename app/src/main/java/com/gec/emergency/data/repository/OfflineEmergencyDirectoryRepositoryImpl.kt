package com.gec.emergency.data.repository

import com.gec.emergency.data.local.OfflineEmergencyDirectoryDataSource
import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.OfflineEmergencyContact
import com.gec.emergency.domain.repository.OfflineEmergencyDirectoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineEmergencyDirectoryRepositoryImpl @Inject constructor(
    private val dataSource: OfflineEmergencyDirectoryDataSource,
) : OfflineEmergencyDirectoryRepository {

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
}
