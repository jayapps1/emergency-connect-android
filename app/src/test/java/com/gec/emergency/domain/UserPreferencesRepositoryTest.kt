package com.gec.emergency.domain

import com.gec.emergency.domain.model.AppLanguage
import com.gec.emergency.domain.model.AppThemeMode
import com.gec.emergency.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class UserPreferencesRepositoryTest {

    private val fakeRepository = object : UserPreferencesRepository {
        private var currentTheme = AppThemeMode.SYSTEM
        private var currentLanguage = AppLanguage.ENGLISH
        private var voiceEnabled = false
        private var largeTextEnabled = false

        override fun observeThemeMode(): Flow<AppThemeMode> = flowOf(currentTheme)
        override suspend fun setThemeMode(mode: AppThemeMode) { currentTheme = mode }

        override fun observeLanguage(): Flow<AppLanguage> = flowOf(currentLanguage)
        override suspend fun setLanguage(language: AppLanguage) { currentLanguage = language }

        override fun observeVoiceGuidanceEnabled(): Flow<Boolean> = flowOf(voiceEnabled)
        override suspend fun setVoiceGuidanceEnabled(enabled: Boolean) { voiceEnabled = enabled }

        override fun observeLargeTextEnabled(): Flow<Boolean> = flowOf(largeTextEnabled)
        override suspend fun setLargeTextEnabled(enabled: Boolean) { largeTextEnabled = enabled }
    }

    @Test
    fun defaultPreferences_matchesExpectedDefaults() = runBlocking {
        assertEquals(AppThemeMode.SYSTEM, fakeRepository.observeThemeMode().first())
        assertEquals(AppLanguage.ENGLISH, fakeRepository.observeLanguage().first())
        assertFalse(fakeRepository.observeVoiceGuidanceEnabled().first())
        assertFalse(fakeRepository.observeLargeTextEnabled().first())
    }

    @Test
    fun appLanguage_fromCode_mapsSupportedCodesAndFallback() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("en"))
        assertEquals(AppLanguage.TWI, AppLanguage.fromCode("tw"))
        assertEquals(AppLanguage.GA, AppLanguage.fromCode("gaa"))
        assertEquals(AppLanguage.EWE, AppLanguage.fromCode("ee"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("unknown_code"))
    }
}
