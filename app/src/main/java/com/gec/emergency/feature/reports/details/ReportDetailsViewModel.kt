package com.gec.emergency.feature.reports.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.domain.repository.IncidentReportRepository
import com.gec.emergency.domain.usecase.GetReportByIdUseCase
import com.gec.emergency.domain.usecase.RouteIncidentReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportDetailsViewModel @Inject constructor(
    private val getReportByIdUseCase: GetReportByIdUseCase,
    private val incidentReportRepository: IncidentReportRepository,
    private val routeIncidentReportUseCase: RouteIncidentReportUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportDetailsUiState())
    val uiState: StateFlow<ReportDetailsUiState> = _uiState.asStateFlow()

    fun loadReport(reportId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val report = getReportByIdUseCase(reportId)
            if (report != null) {
                val category = incidentReportRepository.getCategoryById(report.categoryId)
                val targetAgencies = if (category != null) {
                    routeIncidentReportUseCase(report, category).targetAgencyTypes
                } else emptySet()

                val history = incidentReportRepository.getStatusHistory(reportId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    report = report,
                    targetAgencyTypes = targetAgencies,
                    statusHistory = history,
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Report not found.",
                )
            }
        }
    }
}
