package com.gec.emergency.feature.stations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gec.emergency.R
import com.gec.emergency.core.calling.CallResult

@Composable
fun StationsRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    agencyId: String? = null,
    onStationClick: (String) -> Unit = {},
    onOfflineDirectoryClick: () -> Unit = {},
    viewModel: StationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val invalidNumberMsg = stringResource(R.string.error_invalid_phone_number)
    val unsupportedMsg = stringResource(R.string.error_calling_unsupported)
    val failedMsg = stringResource(R.string.error_call_failed)

    LaunchedEffect(agencyId) {
        viewModel.selectAgency(agencyId)
    }

    LaunchedEffect(viewModel.callEvents) {
        viewModel.callEvents.collect { result ->
            val message = when (result) {
                CallResult.Started -> null
                CallResult.InvalidNumber -> invalidNumberMsg
                CallResult.Unsupported -> unsupportedMsg
                is CallResult.Failed -> failedMsg
            }
            if (message != null) {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            StationsScreen(
                uiState = uiState,
                onBackClick = onBackClick,
                onStationClick = onStationClick,
                onInitiateCall = { station, contact ->
                    viewModel.initiateCall(station, contact)
                },
                onConfirmCall = { request ->
                    viewModel.confirmCall(request)
                },
                onDismissCallConfirmation = {
                    viewModel.dismissCallConfirmation()
                },
                onRetryClick = { viewModel.selectAgency(agencyId) },
                onOfflineDirectoryClick = onOfflineDirectoryClick,
            )
        }
    }
}
