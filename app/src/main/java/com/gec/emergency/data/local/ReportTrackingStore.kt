package com.gec.emergency.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gec_report_tracking_prefs")

private val INSTALLATION_ID_KEY = stringPreferencesKey("anonymous_installation_id")
private val PUBLIC_ACCESS_TOKEN_KEY = stringPreferencesKey("public_access_token")
private val PUBLIC_USER_PHONE_KEY = stringPreferencesKey("public_user_phone")

@Singleton
class ReportTrackingStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    suspend fun getOrCreateInstallationId(): String {
        val existing = context.dataStore.data.map { prefs -> prefs[INSTALLATION_ID_KEY] }.first()
        if (!existing.isNullOrBlank()) {
            return existing
        }

        val newId = UUID.randomUUID().toString()
        context.dataStore.edit { prefs ->
            prefs[INSTALLATION_ID_KEY] = newId
        }
        return newId
    }

    suspend fun savePublicAuthToken(token: String, phoneNumber: String) {
        context.dataStore.edit { prefs ->
            prefs[PUBLIC_ACCESS_TOKEN_KEY] = token
            prefs[PUBLIC_USER_PHONE_KEY] = phoneNumber
        }
    }

    suspend fun getPublicAuthToken(): String? {
        return context.dataStore.data.map { prefs -> prefs[PUBLIC_ACCESS_TOKEN_KEY] }.first()
    }

    suspend fun getPublicUserPhone(): String? {
        return context.dataStore.data.map { prefs -> prefs[PUBLIC_USER_PHONE_KEY] }.first()
    }

    suspend fun clearPublicAuthToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(PUBLIC_ACCESS_TOKEN_KEY)
            prefs.remove(PUBLIC_USER_PHONE_KEY)
        }
    }

    suspend fun saveTrackingToken(publicReference: String, trackingToken: String) {
        if (publicReference.isBlank() || trackingToken.isBlank()) return
        val key = stringPreferencesKey("token_$publicReference")
        context.dataStore.edit { prefs ->
            prefs[key] = trackingToken
        }
    }

    suspend fun getTrackingToken(publicReference: String): String? {
        if (publicReference.isBlank()) return null
        val key = stringPreferencesKey("token_$publicReference")
        return context.dataStore.data.map { prefs ->
            prefs[key]
        }.first()
    }
}
