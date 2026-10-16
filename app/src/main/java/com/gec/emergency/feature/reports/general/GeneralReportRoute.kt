package com.gec.emergency.feature.reports.general

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun GeneralReportRoute(
    onBackClick: () -> Unit,
    onViewReportDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GeneralReportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GeneralReportScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onCategorySelected = { category ->
            viewModel.selectCategory(category)
        },
        onDescriptionChange = { text ->
            viewModel.updateDescription(text)
        },
        onVehicleRegChange = { text ->
            viewModel.updateVehicleRegistration(text)
        },
        onVehicleTypeChange = { text ->
            viewModel.updateVehicleType(text)
        },
        onVehicleColourChange = { text ->
            viewModel.updateVehicleColour(text)
        },
        onDirectionChange = { text ->
            viewModel.updateDirectionOfTravel(text)
        },
        onRefreshLocation = {
            viewModel.refreshLocation()
        },
        onSubmitReport = {
            viewModel.submitReport()
        },
        onViewReportDetails = onViewReportDetails,
        modifier = modifier,
    )
}
