package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.StationContact
import com.gec.emergency.domain.repository.StationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Encapsulates the domain visibility rule for station contacts:
 * Returns contacts that are public, active, and verified, sorted by priority ascending.
 */
class GetPublicStationContactsUseCase @Inject constructor(
    private val stationRepository: StationRepository,
) {
    operator fun invoke(stationId: String): Flow<List<StationContact>> {
        return stationRepository.getContactsForStation(stationId).map { contacts ->
            contacts
                .filter { it.isPublic && it.isActive && it.isVerified }
                .sortedBy { it.priority }
        }
    }
}
