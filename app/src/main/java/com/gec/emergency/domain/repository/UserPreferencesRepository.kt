package com.gec.emergency.domain.repository

import com.gec.emergency.domain.model.AppLanguage
import com.gec.emergency.domain.model.AppThemeMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun observeThemeMode(): Flow<AppThemeMode>
    suspend fun setThemeMode(mode: AppThemeMode)

    fun observeLanguage(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)

    fun observeVoiceGuidanceEnabled(): Flow<Boolean>
    suspend fun setVoiceGuidanceEnabled(enabled: Boolean)

    fun observeLargeTextEnabled(): Flow<Boolean>
    suspend fun setLargeTextEnabled(enabled: Boolean)
}
