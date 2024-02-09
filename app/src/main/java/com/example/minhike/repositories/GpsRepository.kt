package com.example.minhike.repositories

import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.util.GeoPoint

class GpsRepository(
    private val gpsService: GpsService
) {
    val currentLocation: StateFlow<GeoPoint> =gpsService.currentLocation
    fun requestLocation(context: ComponentActivity) = gpsService.requestLocation(context)
}