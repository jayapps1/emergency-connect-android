package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.NearbyStation
import com.gec.emergency.domain.model.NearbyStationQuery
import com.gec.emergency.domain.repository.StationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNearbyStationsUseCase @Inject constructor(
    private val stationRepository: StationRepository,
) {
    operator fun invoke(query: NearbyStationQuery): Flow<List<NearbyStation>> {
        return stationRepository.getNearbyStations(query)
    }
}
