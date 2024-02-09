package com.example.minhike.repositories

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PersistenceRepository(
    private val persistenceService: PersistenceService
) {
    suspend fun saveGpxFile(file: String) = persistenceService.saveGpxFile(file)

    suspend fun readGpxFile(): String = persistenceService.readGpxFile()
}