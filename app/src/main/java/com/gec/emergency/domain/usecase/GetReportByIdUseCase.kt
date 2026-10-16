package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.IncidentReport
import com.gec.emergency.domain.repository.IncidentReportRepository
import javax.inject.Inject

class GetReportByIdUseCase @Inject constructor(
    private val repository: IncidentReportRepository,
) {
    suspend operator fun invoke(id: String): IncidentReport? {
        return repository.getReportById(id)
    }
}
