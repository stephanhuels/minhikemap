package com.example.minhike.usecases

import com.example.minhike.repositories.GpxRepository
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.util.GeoPoint

class GpxUseCase(
    private val gpxRepository: GpxRepository
) {
    val track: StateFlow<List<GeoPoint>> = gpxRepository.track
    fun parseFile(gpxFile: String)= gpxRepository.parseFile(gpxFile)
}