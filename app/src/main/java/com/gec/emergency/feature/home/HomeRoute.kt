package com.gec.emergency.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gec.emergency.R
import com.gec.emergency.core.calling.CallResult
import kotlinx.coroutines.launch

@Composable
fun HomeRoute(
    onEmergencyServicesClick: () -> Unit,
    onReportIncidentClick: () -> Unit,
    onSafetyClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val invalidNumberMsg = stringResource(R.string.error_invalid_phone_number)
    val unsupportedMsg = stringResource(R.string.error_calling_unsupported)
    val failedMsg = stringResource(R.string.error_call_failed)

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            HomeScreen(
                uiState = uiState,
                onEmergencyServicesClick = onEmergencyServicesClick,
                onReportIncidentClick = onReportIncidentClick,
                onSafetyClick = onSafetyClick,
                onSettingsClick = onSettingsClick,
                onAccountClick = onAccountClick,
                onNotificationsClick = onNotificationsClick,
                onNationalEmergencyCall = {
                    viewModel.makeNationalEmergencyCall { result ->
                        scope.launch {
                            val msg = when (result) {
                                CallResult.Started -> null
                                CallResult.InvalidNumber -> invalidNumberMsg
                                CallResult.Unsupported -> unsupportedMsg
                                is CallResult.Failed -> failedMsg
                            }
                            if (msg != null) {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    }
                },
            )
        }
    }
}
