package com.gec.emergency.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gec.emergency.domain.model.OfflineRegionalHeadquartersContact
import com.gec.emergency.domain.model.RegionRef
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.offlineDirDataStore: DataStore<Preferences> by preferencesDataStore(name = "gec_offline_directory_prefs")

@Singleton
class OfflineEmergencyDirectoryStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val gson = Gson()
    private val keyHeadquarters = stringPreferencesKey("cached_headquarters_contacts_json")
    private val keyRegions = stringPreferencesKey("cached_regions_json")

    suspend fun saveHeadquartersContacts(contacts: List<OfflineRegionalHeadquartersContact>) {
        val json = gson.toJson(contacts)
        context.offlineDirDataStore.edit { prefs ->
            prefs[keyHeadquarters] = json
        }
    }

    suspend fun getHeadquartersContacts(): List<OfflineRegionalHeadquartersContact> {
        val json = context.offlineDirDataStore.data.map { prefs ->
            prefs[keyHeadquarters]
        }.first() ?: return emptyList()

        return try {
            val type = object : TypeToken<List<OfflineRegionalHeadquartersContact>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun saveRegions(regions: List<RegionRef>) {
        val json = gson.toJson(regions)
        context.offlineDirDataStore.edit { prefs ->
            prefs[keyRegions] = json
        }
    }

    suspend fun getRegions(): List<RegionRef> {
        val json = context.offlineDirDataStore.data.map { prefs ->
            prefs[keyRegions]
        }.first() ?: return emptyList()

        return try {
            val type = object : TypeToken<List<RegionRef>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
