package com.example.minhike.userinterface

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minhike.usecases.GpsUseCase
import com.example.minhike.usecases.GpxUseCase
import com.example.minhike.usecases.PersistenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val persistenceUseCase: PersistenceUseCase,
    private val gpxUseCase: GpxUseCase,
    private val gpsUseCase: GpsUseCase
) : ViewModel() {
    private val _track: MutableStateFlow<List<GeoPoint>> = MutableStateFlow(emptyList())
    val track: StateFlow<List<GeoPoint>> = _track

    private val _currentLocation: MutableStateFlow<GeoPoint> = MutableStateFlow(GeoPoint(0.0, 0.0))
    val currentLocation: StateFlow<GeoPoint> = _currentLocation

    sealed interface UIState {
        object ZoomTrack : UIState
        object ZoomCenter : UIState
        object InteractiveMap : UIState
        object OpenTrack : UIState
    }

    private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState.ZoomCenter)
    val uiState: StateFlow<UIState> = _uiState

    fun collectData() {
        viewModelScope.launch {
            gpsUseCase.currentLocation.collect {
                _currentLocation.emit(GeoPoint(it.latitude, it.longitude))
                Log.i("MINHIKE", "current location changed $it")
                _uiState.emit(UIState.ZoomCenter)
            }
        }
        viewModelScope.launch {
            gpxUseCase.track.collect {
                Log.i("MINHIKE", "emit track")
                _track.emit(it)
                _uiState.emit(UIState.ZoomTrack)
            }
        }
    }

    fun requestLocation(context: ComponentActivity) {
        gpsUseCase.requestLocation(context)
    }

    fun centerMap() {
        Log.i("MINHIKE", "center map")
        viewModelScope.launch {
            _uiState.emit(UIState.ZoomCenter)
        }
    }

    fun showTrack() {
        viewModelScope.launch {
            _uiState.emit(UIState.ZoomTrack)
        }
    }

    fun interactiveMap() {
        viewModelScope.launch {
            _uiState.emit(UIState.InteractiveMap)
        }
    }

    fun parseGpxFile(gpxFile: String) {
        Log.i("MINHIKE", "parseGpxFile $gpxFile")
        viewModelScope.launch {
            _uiState.emit(UIState.OpenTrack)
        }
        gpxUseCase.parseFile(gpxFile)
    }

    fun storeGpxFile(gpxFile: String) {
        Log.i("MINHIKE", "storeGpxFile $gpxFile")
        viewModelScope.launch {
            persistenceUseCase.saveGpxFile(gpxFile)
        }
    }

    fun readGpxFile() {
        viewModelScope.launch {
            val gpxFile = persistenceUseCase.readGpxFile()
            Log.i("MINHIKE", "readGpxFile $gpxFile")
            if (gpxFile.isNotEmpty()) {
                gpxUseCase.parseFile(gpxFile)
            }
        }
    }
}