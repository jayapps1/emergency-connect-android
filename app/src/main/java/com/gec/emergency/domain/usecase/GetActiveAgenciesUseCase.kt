package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.Agency
import com.gec.emergency.domain.repository.AgencyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveAgenciesUseCase @Inject constructor(
    private val agencyRepository: AgencyRepository,
) {
    operator fun invoke(): Flow<List<Agency>> {
        return agencyRepository.getActiveAgencies()
    }
}
