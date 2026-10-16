package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.OfflineEmergencyContact
import com.gec.emergency.domain.repository.OfflineEmergencyDirectoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOfflineEmergencyContactsUseCase @Inject constructor(
    private val repository: OfflineEmergencyDirectoryRepository,
) {
    operator fun invoke(): Flow<List<OfflineEmergencyContact>> {
        return repository.getEmergencyContacts()
    }
}
