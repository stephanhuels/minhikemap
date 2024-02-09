package com.example.minhike.repositories

import android.util.Log
import io.ticofab.androidgpxparser.parser.GPXParser
import io.ticofab.androidgpxparser.parser.domain.Gpx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.IOException
import java.io.InputStream

class GpxService {
    private val _track: MutableStateFlow<List<GeoPoint>> = MutableStateFlow(emptyList())
    val track: StateFlow<List<GeoPoint>> = _track

   fun parseFile(gpxFile: String) {
       CoroutineScope(Dispatchers.IO).launch {
           try {
               val inputStream: InputStream = File(gpxFile).inputStream()
               val parser = GPXParser()
               val parsedGpx: Gpx? = parser.parse(inputStream)
               parsedGpx?.let {
                   Log.i("MINHIKE", "gpx paring successful")
                   val trackList = mutableListOf<GeoPoint>()
                   it.tracks?.forEach{
                       if (it != null) {
                           Log.i("MINHIKE", "track it")
                           it.trackSegments?.forEach{
                               if (it != null) {
                                   it.trackPoints?.forEach{
                                       if (it != null) {
                                           Log.i("MINHIKE", "trackpoint ${it.latitude} ${it.longitude}")
                                           trackList.add(GeoPoint(it.latitude, it.longitude))
                                       }
                                   }
                               }
                           }
                       }
                       _track.emit(trackList)
                   }
               } ?: {
                   Log.i("MINHIKE", "gpx can not be parsed")
               }
           } catch (e: IOException) {
               Log.i("MINHIKE", "IOException")
               e.printStackTrace()
           } catch (e: XmlPullParserException) {
               Log.i("MINHIKE", "XmlPullParserException")
               e.printStackTrace()
           }
       }
    }
}