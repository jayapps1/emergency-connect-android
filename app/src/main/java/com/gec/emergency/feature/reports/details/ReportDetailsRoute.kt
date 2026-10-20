package com.gec.emergency.feature.reports.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ReportDetailsRoute(
    reportId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(reportId) {
        viewModel.loadReport(reportId)
    }

    ReportDetailsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onOpenRatingDialog = { assignment ->
            viewModel.openRatingDialog(assignment)
        },
        onDismissRatingDialog = {
            viewModel.dismissRatingDialog()
        },
        onSubmitReview = { rating, speed, prof, helpful, comment ->
            viewModel.submitResponseReview(rating, speed, prof, helpful, comment)
        },
        modifier = modifier,
    )
}
