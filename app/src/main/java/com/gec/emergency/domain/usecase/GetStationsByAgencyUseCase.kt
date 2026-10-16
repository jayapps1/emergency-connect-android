package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.repository.StationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStationsByAgencyUseCase @Inject constructor(
    private val stationRepository: StationRepository,
) {
    operator fun invoke(agencyId: String): Flow<List<Station>> {
        return stationRepository.getStationsByAgency(agencyId)
    }
}
