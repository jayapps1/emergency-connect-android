package com.gec.emergency.feature.settings

import com.gec.emergency.domain.model.AppLanguage
import com.gec.emergency.domain.model.AppThemeMode

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val voiceGuidanceEnabled: Boolean = false,
    val largeTextEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
