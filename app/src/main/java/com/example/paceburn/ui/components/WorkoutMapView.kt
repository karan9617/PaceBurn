package com.example.paceburn.ui.components

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.paceburn.location.LocationPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

val OSM_HOT_TILE_SOURCE = object : OnlineTileSourceBase(
    "OpenStreetMapHOT",
    0, 19, 256, ".png",
    arrayOf(
        "https://a.tile.openstreetmap.fr/hot/",
        "https://b.tile.openstreetmap.fr/hot/"
    )
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        return baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" +
                MapTileIndex.getX(pMapTileIndex) + "/" +
                MapTileIndex.getY(pMapTileIndex) + mImageFilenameEnding
    }
}

@Composable
fun WorkoutMapView(
    routePoints: List<LocationPoint>,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val mapView = remember {
        // Set unique User-Agent and load OSM preferences
        Configuration.getInstance().userAgentValue = "PaceBurnTrackerApp/1.0 (Android Fitness)"
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE))

        MapView(context).apply {
            setTileSource(OSM_HOT_TILE_SOURCE)
            setMultiTouchControls(true)
            zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(16.5)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView.onDetach()
        }
    }

    Box(modifier = modifier) {
        if (routePoints.isNotEmpty()) {
            AndroidView(
                factory = { mapView },
                update = { view ->
                    view.overlays.clear()

                    val geoPoints = routePoints.map { GeoPoint(it.latitude, it.longitude) }

                    // Draw Route Polyline
                    val polyline = Polyline().apply {
                        setPoints(geoPoints)
                        outlinePaint.color = android.graphics.Color.parseColor("#FF512F")
                        outlinePaint.strokeWidth = 14f
                        outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                    }
                    view.overlays.add(polyline)

                    // Draw Start Marker
                    geoPoints.firstOrNull()?.let { startGeo ->
                        val startMarker = Marker(view).apply {
                            position = startGeo
                            title = "Start Point"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        view.overlays.add(startMarker)
                    }

                    // Draw Current Location Marker
                    geoPoints.lastOrNull()?.let { currentGeo ->
                        val currentMarker = Marker(view).apply {
                            position = currentGeo
                            title = "Current Position"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        }
                        view.overlays.add(currentMarker)

                        // Center map on latest position
                        view.controller.animateTo(currentGeo)
                    }

                    view.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Default Map Canvas Placeholder before GPS lock
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Grid overlay
                for (i in 1..8) {
                    val x = canvasWidth * i / 8f
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        start = Offset(x, 0f),
                        end = Offset(x, canvasHeight),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                for (i in 1..8) {
                    val y = canvasHeight * i / 8f
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Sample stylized map route curve
                val path = Path().apply {
                    moveTo(canvasWidth * 0.2f, canvasHeight * 0.7f)
                    cubicTo(
                        canvasWidth * 0.4f, canvasHeight * 0.2f,
                        canvasWidth * 0.6f, canvasHeight * 0.8f,
                        canvasWidth * 0.8f, canvasHeight * 0.3f
                    )
                }

                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFFFF512F), Color(0xFFF09819))
                    ),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Center on Location Button
        if (routePoints.isNotEmpty()) {
            FloatingActionButton(
                onClick = {
                    routePoints.lastOrNull()?.let { last ->
                        mapView.controller.animateTo(GeoPoint(last.latitude, last.longitude))
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Center Map",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
