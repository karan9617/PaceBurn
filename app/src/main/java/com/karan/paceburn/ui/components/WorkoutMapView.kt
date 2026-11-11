package com.karan.paceburn.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.karan.paceburn.location.LocationPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.InfoWindow

@Composable
fun WorkoutMapView(
    routePoints: List<LocationPoint>,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    onMapViewCreated: ((MapView) -> Unit)? = null
) {
    val context = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    val startMarkerDrawable = remember(context) {
        createModernMarkerDrawable(
            context = context,
            coreColor = android.graphics.Color.parseColor("#10B981"), // Emerald Green
            glowColor = android.graphics.Color.parseColor("#4010B981"),
            sizeDp = 28
        )
    }

    val currentMarkerDrawable = remember(context) {
        createModernMarkerDrawable(
            context = context,
            coreColor = android.graphics.Color.parseColor("#FF385C"), // Vibrant Workout Accent
            glowColor = android.graphics.Color.parseColor("#40FF385C"),
            sizeDp = 30
        )
    }

    Box(modifier = modifier) {
        if (routePoints.isNotEmpty()) {
            AndroidView(
                factory = { ctx ->
                    Configuration.getInstance().userAgentValue = "PaceBurnTrackerApp/1.0 (Android Fitness)"
                    Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE))

                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
                        controller.setZoom(17.0)
                    }.also { view ->
                        mapViewRef = view
                        onMapViewCreated?.invoke(view)
                    }
                },
                update = { view ->
                    InfoWindow.closeAllInfoWindowsOn(view)
                    view.overlays.clear()

                    val geoPoints = routePoints.map { GeoPoint(it.latitude, it.longitude) }

                    if (geoPoints.isNotEmpty()) {
                        // Outer Glow Casing Polyline
                        val glowPolyline = Polyline().apply {
                            setPoints(geoPoints)
                            outlinePaint.color = android.graphics.Color.parseColor("#3BFF385C")
                            outlinePaint.strokeWidth = 24f
                            outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                            outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                        }
                        view.overlays.add(glowPolyline)

                        // Main Vibrant Route Polyline
                        val mainPolyline = Polyline().apply {
                            setPoints(geoPoints)
                            outlinePaint.color = android.graphics.Color.parseColor("#FF385C")
                            outlinePaint.strokeWidth = 12f
                            outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                            outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                        }
                        view.overlays.add(mainPolyline)
                    }

                    // Draw Start Point Marker
                    geoPoints.firstOrNull()?.let { startGeo ->
                        val startMarker = Marker(view).apply {
                            position = startGeo
                            title = "Start Point"
                            icon = startMarkerDrawable
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            infoWindow = null
                            setOnMarkerClickListener { _, _ -> true }
                        }
                        view.overlays.add(startMarker)
                    }

                    // Draw Current Position Marker
                    geoPoints.lastOrNull()?.let { currentGeo ->
                        val currentMarker = Marker(view).apply {
                            position = currentGeo
                            title = "Current Position"
                            icon = currentMarkerDrawable
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            infoWindow = null
                            setOnMarkerClickListener { _, _ -> true }
                        }
                        view.overlays.add(currentMarker)

                        // Smoothly animate map to latest GPS coordinate
                        view.controller.animateTo(currentGeo)
                    }

                    view.invalidate()
                },
                onRelease = { view ->
                    InfoWindow.closeAllInfoWindowsOn(view)
                    view.onDetach()
                    mapViewRef = null
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Modern Animated Map Canvas Placeholder before GPS active
            val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "PulseScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // Grid lines
                    val step = 48.dp.toPx()
                    var x = 0f
                    while (x < canvasWidth) {
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.12f),
                            start = Offset(x, 0f),
                            end = Offset(x, canvasHeight),
                            strokeWidth = 1.dp.toPx()
                        )
                        x += step
                    }
                    var y = 0f
                    while (y < canvasHeight) {
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.12f),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        y += step
                    }

                    // Radar pulse effect at center
                    val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
                    val maxRadius = canvasWidth.coerceAtMost(canvasHeight) * 0.35f
                    drawCircle(
                        color = Color(0xFFFF385C).copy(alpha = (1f - pulseScale) * 0.35f),
                        radius = maxRadius * pulseScale,
                        center = center
                    )

                    // Sample aesthetic path
                    val path = Path().apply {
                        moveTo(canvasWidth * 0.15f, canvasHeight * 0.65f)
                        cubicTo(
                            canvasWidth * 0.35f, canvasHeight * 0.25f,
                            canvasWidth * 0.65f, canvasHeight * 0.85f,
                            canvasWidth * 0.85f, canvasHeight * 0.35f
                        )
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFFFF385C).copy(alpha = 0.25f),
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "GPS Route Map",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Press 'Start Workout' to begin live route tracking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Floating Location Center Button
        if (routePoints.isNotEmpty()) {
            FloatingActionButton(
                onClick = {
                    routePoints.lastOrNull()?.let { last ->
                        mapViewRef?.controller?.animateTo(GeoPoint(last.latitude, last.longitude))
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Re-center Map",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun createModernMarkerDrawable(
    context: Context,
    coreColor: Int,
    glowColor: Int,
    sizeDp: Int
): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val sizePx = (sizeDp * density).toInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val center = sizePx / 2f
    val glowRadius = sizePx / 2f
    val whiteRadius = sizePx * 0.38f
    val coreRadius = sizePx * 0.26f

    // Glow aura
    val glowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = glowColor
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(center, center, glowRadius, glowPaint)

    // White border ring
    val whitePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(center, center, whiteRadius, whitePaint)

    // Inner core
    val corePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = coreColor
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(center, center, coreRadius, corePaint)

    return BitmapDrawable(context.resources, bitmap)
}
