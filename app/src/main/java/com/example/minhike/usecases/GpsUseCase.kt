package com.example.minhike.usecases

import androidx.activity.ComponentActivity
import com.example.minhike.repositories.GpsRepository
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.util.GeoPoint

class GpsUseCase(
    private val gpsRepository: GpsRepository
) {
    val currentLocation: StateFlow<GeoPoint> = gpsRepository.currentLocation
    fun requestLocation(context: ComponentActivity) = gpsRepository.requestLocation(context)
}