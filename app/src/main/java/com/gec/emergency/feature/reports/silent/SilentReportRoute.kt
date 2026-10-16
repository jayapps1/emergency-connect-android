package com.gec.emergency.feature.reports.silent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SilentReportRoute(
    onBackClick: () -> Unit,
    onViewReportDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SilentReportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SilentReportScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onCategorySelected = { category ->
            viewModel.selectCategory(category)
        },
        onDescriptionChange = { text ->
            viewModel.updateDescription(text)
        },
        onRefreshLocation = {
            viewModel.refreshLocation()
        },
        onSubmitReport = {
            viewModel.submitSilentReport()
        },
        onViewReportDetails = onViewReportDetails,
        modifier = modifier,
    )
}
