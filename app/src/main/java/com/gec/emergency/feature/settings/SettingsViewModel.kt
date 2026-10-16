package com.gec.emergency.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.domain.model.AppLanguage
import com.gec.emergency.domain.model.AppThemeMode
import com.gec.emergency.domain.usecase.ObserveAppLanguageUseCase
import com.gec.emergency.domain.usecase.ObserveLargeTextUseCase
import com.gec.emergency.domain.usecase.ObserveThemeModeUseCase
import com.gec.emergency.domain.usecase.ObserveVoiceGuidanceUseCase
import com.gec.emergency.domain.usecase.SetAppLanguageUseCase
import com.gec.emergency.domain.usecase.SetLargeTextUseCase
import com.gec.emergency.domain.usecase.SetThemeModeUseCase
import com.gec.emergency.domain.usecase.SetVoiceGuidanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observeThemeModeUseCase: ObserveThemeModeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val observeAppLanguageUseCase: ObserveAppLanguageUseCase,
    private val setAppLanguageUseCase: SetAppLanguageUseCase,
    private val observeVoiceGuidanceUseCase: ObserveVoiceGuidanceUseCase,
    private val setVoiceGuidanceUseCase: SetVoiceGuidanceUseCase,
    private val observeLargeTextUseCase: ObserveLargeTextUseCase,
    private val setLargeTextUseCase: SetLargeTextUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                observeThemeModeUseCase(),
                observeAppLanguageUseCase(),
                observeVoiceGuidanceUseCase(),
                observeLargeTextUseCase(),
            ) { theme, lang, voice, largeText ->
                SettingsUiState(
                    themeMode = theme,
                    language = lang,
                    voiceGuidanceEnabled = voice,
                    largeTextEnabled = largeText,
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            setThemeModeUseCase(mode)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            setAppLanguageUseCase(language)
        }
    }

    fun setVoiceGuidanceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            setVoiceGuidanceUseCase(enabled)
        }
    }

    fun setLargeTextEnabled(enabled: Boolean) {
        viewModelScope.launch {
            setLargeTextUseCase(enabled)
        }
    }
}
