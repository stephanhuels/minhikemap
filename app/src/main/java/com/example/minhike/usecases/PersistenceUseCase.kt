package com.example.minhike.usecases

import com.example.minhike.repositories.PersistenceRepository

class PersistenceUseCase(
    private val persistenceRepository: PersistenceRepository
) {
    suspend fun saveGpxFile(file: String) = persistenceRepository.saveGpxFile(file)

    suspend fun readGpxFile(): String = persistenceRepository.readGpxFile()
}