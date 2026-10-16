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
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gec_report_tracking_prefs")

@Singleton
class ReportTrackingStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
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
