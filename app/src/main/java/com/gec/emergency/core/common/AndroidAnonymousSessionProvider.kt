package com.gec.emergency.core.common

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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gec_anonymous_session_prefs")

@Singleton
class AndroidAnonymousSessionProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AnonymousSessionProvider {

    private val sessionIdKey = stringPreferencesKey("anonymous_session_id")

    override suspend fun getOrCreateSessionId(): String {
        val existingId = context.dataStore.data.map { prefs ->
            prefs[sessionIdKey]
        }.first()

        if (!existingId.isNullOrBlank()) {
            return existingId
        }

        val newSessionId = UUID.randomUUID().toString()
        context.dataStore.edit { prefs ->
            prefs[sessionIdKey] = newSessionId
        }
        return newSessionId
    }
}
