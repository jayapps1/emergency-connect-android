package com.gec.emergency.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onThemeSelected = { mode -> viewModel.setThemeMode(mode) },
        onLanguageSelected = { lang -> viewModel.setLanguage(lang) },
        onVoiceGuidanceToggled = { voice -> viewModel.setVoiceGuidanceEnabled(voice) },
        onLargeTextToggled = { large -> viewModel.setLargeTextEnabled(large) },
        modifier = modifier,
    )
}
