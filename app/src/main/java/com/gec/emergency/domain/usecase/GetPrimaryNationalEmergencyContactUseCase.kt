package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.OfflineEmergencyContact
import com.gec.emergency.domain.repository.OfflineEmergencyDirectoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPrimaryNationalEmergencyContactUseCase @Inject constructor(
    private val repository: OfflineEmergencyDirectoryRepository,
) {
    operator fun invoke(): Flow<OfflineEmergencyContact?> {
        return repository.getPrimaryNationalEmergencyContact()
    }
}
