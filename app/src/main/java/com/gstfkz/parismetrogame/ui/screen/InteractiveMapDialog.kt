package com.gstfkz.parismetrogame.ui.screen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.viewinterop.AndroidView
import com.gstfkz.parismetrogame.BuildConfig
import com.gstfkz.parismetrogame.data.model.MetroStation
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private fun dot(color: Int): BitmapDrawable {
    val bmp = Bitmap.createBitmap(52, 52, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(26f, 26f, 24f, paint)
    paint.color = color
    canvas.drawCircle(26f, 26f, 19f, paint)
    return BitmapDrawable(null, bmp)
}

@Composable
fun InteractiveMapDialog(departure: MetroStation, arrival: MetroStation, txt: UiText, onClose: () -> Unit) {
    val context = LocalContext.current
    val map = remember {
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(12.0)
            controller.setCenter(GeoPoint(48.8566, 2.3522))
        }
    }
    DisposableEffect(map) {
        map.onResume()
        onDispose { map.onPause(); map.onDetach() }
    }
    Dialog(onDismissRequest = onClose) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxWidth().height(520.dp).padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(txt.t("Interactive map", "Carte interactive"), style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onClose) { Text("✕") }
                }
                val a = departure.latitude?.let { lat -> departure.longitude?.let { lon -> GeoPoint(lat, lon) } }
                val b = arrival.latitude?.let { lat -> arrival.longitude?.let { lon -> GeoPoint(lat, lon) } }
                if (a == null || b == null) {
                    Text(txt.t("Station coordinates unavailable.", "Coordonnées des stations indisponibles."))
                } else {
                    AndroidView(factory = { map }, modifier = Modifier.fillMaxWidth().weight(1f),
                        update = { view ->
                            view.overlays.clear()
                            val first = Marker(view).apply {
                                position = a
                                title = departure.name
                                icon = dot(android.graphics.Color.rgb(35, 160, 70))
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            }
                            val last = Marker(view).apply {
                                position = b
                                title = arrival.name
                                icon = dot(android.graphics.Color.rgb(210, 40, 50))
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            }
                            view.overlays.add(first)
                            view.overlays.add(last)
                            view.post {
                                val north = maxOf(a.latitude, b.latitude) + 0.012
                                val south = minOf(a.latitude, b.latitude) - 0.012
                                val east = maxOf(a.longitude, b.longitude) + 0.018
                                val west = minOf(a.longitude, b.longitude) - 0.018
                                view.zoomToBoundingBox(BoundingBox(north, east, south, west), true, 48)
                            }
                        })
                    Spacer(Modifier.height(8.dp))
                    Text(txt.t("Green: departure • Red: arrival", "Vert : départ • Rouge : arrivée"),
                        style = MaterialTheme.typography.bodySmall)
                    Text("© OpenStreetMap contributors", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
