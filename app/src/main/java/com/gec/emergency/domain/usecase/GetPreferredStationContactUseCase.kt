package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.StationContact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Encapsulates the domain rule for selecting the primary/preferred contact number for a station.
 * Returns the first eligible public contact ordered by priority.
 */
class GetPreferredStationContactUseCase @Inject constructor(
    private val getPublicStationContactsUseCase: GetPublicStationContactsUseCase,
) {
    operator fun invoke(stationId: String): Flow<StationContact?> {
        return getPublicStationContactsUseCase(stationId).map { contacts ->
            contacts.firstOrNull()
        }
    }
}
