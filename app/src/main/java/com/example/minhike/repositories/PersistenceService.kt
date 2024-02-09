package com.example.minhike.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val KEY_GPX_FILE = "gpx_file"

class PersistenceService(private val preferencesDataStore: DataStore<Preferences>) {
    suspend fun saveGpxFile(file: String) {
        val gpxFileKey = stringPreferencesKey(KEY_GPX_FILE)
        preferencesDataStore.edit { preferences ->
            preferences[gpxFileKey] = file
        }
    }

    suspend fun readGpxFile(): String {
        val gpxFileKey = stringPreferencesKey(KEY_GPX_FILE)
        return preferencesDataStore.data.map { preferences ->
            if (!preferences.contains(gpxFileKey)) {
                return@map ""
            }
            preferences[gpxFileKey] ?: ""
        }.first()
    }
}