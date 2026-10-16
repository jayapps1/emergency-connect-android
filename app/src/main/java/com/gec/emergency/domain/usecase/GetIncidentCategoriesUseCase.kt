package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.IncidentCategory
import com.gec.emergency.domain.repository.IncidentReportRepository
import javax.inject.Inject

class GetIncidentCategoriesUseCase @Inject constructor(
    private val repository: IncidentReportRepository,
) {
    suspend operator fun invoke(): List<IncidentCategory> {
        return repository.getCategories()
    }
}
