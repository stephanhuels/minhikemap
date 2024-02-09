package com.example.minhike.userinterface

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minhike.userinterface.theme.MinhikeTheme
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 0)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        super.onCreate(savedInstanceState)
        setContent {
            MinhikeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val dialogState: MutableState<Boolean> = remember {
                        mutableStateOf(false)
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        OsmdroidMapView(viewModel)

                        Card (modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp) ) {
                            Row {
                                Button(
                                    modifier = Modifier
                                        .wrapContentSize(),
                                    onClick = {
                                        dialogState.value = true
                                    }
                                ) {
                                    Text("open")
                                }
                                Button(
                                    modifier = Modifier
                                        .wrapContentSize(),
                                    onClick = {
                                        viewModel.centerMap()
                                    }
                                ) {
                                    Text("center")
                                }
                                Button(
                                    modifier = Modifier
                                        .wrapContentSize(),
                                    onClick = {
                                        viewModel.showTrack()
                                    }
                                ) {
                                    Text("track")
                                }
                            }
                        }

                        if (dialogState.value) {
                            Dialog(onDismissRequest = { dialogState.value = false }) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                ) {
                                     GpxList(dialogState, viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = "minhike"
        viewModel.collectData()
        viewModel.requestLocation(this)
        viewModel.readGpxFile()
    }

    override fun onStart() {
        super.onStart()
        viewModel.centerMap()
    }
}

@Composable
fun GpxList(dialogState: MutableState<Boolean>, viewModel: MainViewModel) {
    val downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    var gpxFiles = mutableListOf<String>()
    if (downloadDirectory != null) {
        Log.i("MINHIKE", "download folder ${downloadDirectory.absolutePath}")
        val files = File(downloadDirectory.absolutePath).listFiles()
        files?.mapIndexed { index, item ->
            if (item != null) {
                if (item.isFile) {
                    if (item.name.lowercase().contains(".gpx")) {
                        Log.i("MINHIKE", "gpx file ${item.name}")
                        gpxFiles.add(item.name)
                    }
                }
            }
        }
    }

    LazyColumn(modifier = Modifier) {
        items(gpxFiles.size) { index ->
            Card( modifier = Modifier
                .clickable {
                    Log.i("MINHIKE", "click on file ${gpxFiles.get(index)}")

                    if (downloadDirectory.absolutePath != null) {
                        val gpxFileAbsolute =
                            downloadDirectory.absolutePath + "/" + gpxFiles.get(index)
                        viewModel.storeGpxFile(gpxFileAbsolute)
                        viewModel.parseGpxFile(gpxFileAbsolute)
                    }

                    dialogState.value = false
                }
                .padding(all = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background,

                ) ) {
                Text(
                    text = gpxFiles.get(index),
                    modifier = Modifier.padding(all = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun OsmdroidMapView(viewModel: MainViewModel) {
    val uiState = viewModel.uiState.collectAsState().value
    val currentLocation = viewModel.currentLocation.collectAsState().value
    val track = viewModel.track.collectAsState().value
    Log.i("MINHIKE", "currentLocation $currentLocation, uiState $uiState")
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            var mapView = MapView(context)
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.setMultiTouchControls(true)
            val currentLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
            currentLocationOverlay.enableMyLocation()
            mapView.overlays.add(0, currentLocationOverlay)
            mapView.controller.setZoom(17.5)

            mapView.setOnTouchListener { v, event ->
                Log.i("MINHIKE", "touch event $event")
                viewModel.interactiveMap()
                false
            }
            mapView
        },
        update = { view ->
            Log.i("MINHIKE", "$currentLocation")

            if (view.overlays.size >= 2) {
                view.overlays.removeAt(2)
                view.overlays.removeAt(1)
            }

            var bbox = BoundingBox()

            if (track.isNotEmpty()) {
                val start = GeoPoint(track.get(0).latitude, track.get(0).longitude)
                val startMarker = Marker(view)
                startMarker.position = start
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                view.overlays.add(1, startMarker)

                Log.i("MINHIKE", "overlay size ${view.overlays.size}")
                val line = Polyline(view)

                val geoPoints = mutableListOf<GeoPoint>()
                track.forEach {
                    line.addPoint(GeoPoint(it.latitude, it.longitude))
                    Log.i("MINHIKE", "point ${it.latitude} ${it.longitude}")
                    geoPoints.add(GeoPoint(it.latitude, it.longitude))
                }
                bbox = BoundingBox.fromGeoPoints(geoPoints)
                Log.i("MINHIKE", "bbox $bbox")
                line.width = 2.0f
                line.color = Color.BLUE
                view.overlays.add(2, line)
            }

            Log.i("MINHIKE", "ui state $uiState")

            when (uiState) {
                MainViewModel.UIState.ZoomTrack, MainViewModel.UIState.OpenTrack -> {
                    if (track.isNotEmpty()) {
                        view.controller.setCenter(GeoPoint(bbox.centerLatitude, bbox.centerLongitude))
                        view.zoomToBoundingBox(bbox, false, 20)
                    } else {
                        view.controller.setZoom(14.5)
                    }
                }
                MainViewModel.UIState.ZoomCenter -> view.controller.setCenter(currentLocation)
                MainViewModel.UIState.InteractiveMap -> {}
            }
        }
    )
}
