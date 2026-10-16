package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.StationContact
import com.gec.emergency.domain.repository.StationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStationContactsUseCase @Inject constructor(
    private val stationRepository: StationRepository,
) {
    operator fun invoke(stationId: String): Flow<List<StationContact>> {
        return stationRepository.getContactsForStation(stationId)
    }
}
