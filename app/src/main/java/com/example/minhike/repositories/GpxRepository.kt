package com.example.minhike.repositories

import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.util.GeoPoint

class GpxRepository(
    private val gpxService: GpxService
) {
    val track: StateFlow<List<GeoPoint>> = gpxService.track
    fun parseFile(gpxFile: String) = gpxService.parseFile(gpxFile)
}