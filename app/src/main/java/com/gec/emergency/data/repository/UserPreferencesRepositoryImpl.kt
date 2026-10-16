package com.gec.emergency.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gec.emergency.domain.model.AppLanguage
import com.gec.emergency.domain.model.AppThemeMode
import com.gec.emergency.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gec_user_preferences")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : UserPreferencesRepository {

    private val themeKey = stringPreferencesKey("theme_mode")
    private val languageKey = stringPreferencesKey("language_code")
    private val voiceGuidanceKey = booleanPreferencesKey("voice_guidance_enabled")
    private val largeTextKey = booleanPreferencesKey("large_text_enabled")

    override fun observeThemeMode(): Flow<AppThemeMode> {
        return context.dataStore.data.map { prefs ->
            val modeStr = prefs[themeKey]
            runCatching { AppThemeMode.valueOf(modeStr ?: "") }.getOrDefault(AppThemeMode.SYSTEM)
        }
    }

    override suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeKey] = mode.name
        }
    }

    override fun observeLanguage(): Flow<AppLanguage> {
        return context.dataStore.data.map { prefs ->
            val code = prefs[languageKey]
            AppLanguage.fromCode(code)
        }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[languageKey] = language.code
        }
    }

    override fun observeVoiceGuidanceEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[voiceGuidanceKey] ?: false
        }
    }

    override suspend fun setVoiceGuidanceEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[voiceGuidanceKey] = enabled
        }
    }

    override fun observeLargeTextEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[largeTextKey] ?: false
        }
    }

    override suspend fun setLargeTextEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[largeTextKey] = enabled
        }
    }
}
