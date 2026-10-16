package com.gec.emergency.feature.stations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AgencySelectorRoute(
    onBackClick: () -> Unit,
    onAgencySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOfflineDirectoryClick: () -> Unit = {},
    viewModel: AgencySelectorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AgencySelectorScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onAgencySelected = onAgencySelected,
        onRetryClick = { viewModel.loadActiveAgencies() },
        onOfflineDirectoryClick = onOfflineDirectoryClick,
        modifier = modifier,
    )
}
