package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.repository.StationRepository
import javax.inject.Inject

class GetStationByIdUseCase @Inject constructor(
    private val stationRepository: StationRepository,
) {
    suspend operator fun invoke(stationId: String): Station? {
        return stationRepository.getStationById(stationId)
    }
}
