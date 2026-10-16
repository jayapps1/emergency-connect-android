package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.IncidentCategory
import com.gec.emergency.domain.model.ReportMode
import com.gec.emergency.domain.repository.IncidentReportRepository
import javax.inject.Inject

class GetGeneralIncidentCategoriesUseCase @Inject constructor(
    private val repository: IncidentReportRepository,
) {
    suspend operator fun invoke(): List<IncidentCategory> {
        return repository.getCategories().filter { ReportMode.GENERAL in it.reportModeSupported }
    }
}
