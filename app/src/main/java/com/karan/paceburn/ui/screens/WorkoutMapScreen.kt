package com.karan.paceburn.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.karan.paceburn.data.SavedWorkout
import com.karan.paceburn.ui.WorkoutViewModel
import com.karan.paceburn.ui.components.WorkoutMapView
import org.osmdroid.views.MapView
import java.io.File
import java.util.Locale

@Composable
fun WorkoutMapScreen(
    userWeightKg: Float,
    modifier: Modifier = Modifier,
    workoutViewModel: WorkoutViewModel = viewModel()
) {
    val metrics by workoutViewModel.metrics.collectAsState()
    val timerSeconds by workoutViewModel.timerSeconds.collectAsState()
    val savedWorkouts by workoutViewModel.savedWorkouts.collectAsState()

    var activeMapView by remember { mutableStateOf<MapView?>(null) }
    val locale = LocalConfiguration.current.locales[0]

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            workoutViewModel.startWorkout(userWeightKg)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Outdoor Workout Tracker",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "GPS Route & Live Workout Performance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (metrics.isRecording) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (metrics.isRecording) "● Live GPS" else "Paused",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (metrics.isRecording) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Interactive Map Box with Polyline Route
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                WorkoutMapView(
                    routePoints = metrics.routePoints,
                    isRecording = metrics.isRecording,
                    modifier = Modifier.fillMaxSize(),
                    onMapViewCreated = { view ->
                        activeMapView = view
                    }
                )
            }
        }

        // Live Workout HUD Stats Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Main Prominent Distance Banner (Miles & Km)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "DISTANCE RUN",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format(locale, "%.2f", metrics.distanceMiles),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MILES",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format(locale, "%.2f km", metrics.distanceKm),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Kilometers",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Secondary Metrics Grid (Duration, Calories, Speed, Pace)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Duration
                        val hrs = timerSeconds / 3600
                        val mins = (timerSeconds % 3600) / 60
                        val secs = timerSeconds % 60
                        val timeStr = if (hrs > 0) String.format(locale, "%02d:%02d:%02d", hrs, mins, secs)
                        else String.format(locale, "%02d:%02d", mins, secs)

                        WorkoutStatItem(
                            title = "Duration",
                            value = timeStr,
                            unit = "",
                            icon = Icons.Default.Timer,
                            modifier = Modifier.weight(1f)
                        )

                        // Calories
                        WorkoutStatItem(
                            title = "Calories",
                            value = String.format(locale, "%.0f", metrics.caloriesBurned),
                            unit = "kcal",
                            icon = Icons.Default.LocalFireDepartment,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Avg Speed
                        WorkoutStatItem(
                            title = "Avg Speed",
                            value = String.format(locale, "%.1f", metrics.avgSpeedMph),
                            unit = "mph",
                            icon = Icons.Default.Speed,
                            modifier = Modifier.weight(1f)
                        )

                        // Pace
                        val paceMins = metrics.paceMinsPerMile.toInt()
                        val paceSecs = ((metrics.paceMinsPerMile - paceMins) * 60).toInt().coerceIn(0, 59)
                        val paceStr = if (metrics.distanceMiles > 0) String.format(locale, "%d'%02d\"", paceMins, paceSecs) else "--'--"

                        WorkoutStatItem(
                            title = "Pace",
                            value = paceStr,
                            unit = "/mi",
                            icon = Icons.AutoMirrored.Filled.DirectionsRun,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Workout Action Control Buttons
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Start / Pause Button
                    Button(
                        onClick = {
                            if (!workoutViewModel.hasLocationPermission()) {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else {
                                if (metrics.isRecording) {
                                    workoutViewModel.pauseWorkout()
                                } else {
                                    workoutViewModel.startWorkout(userWeightKg)
                                }
                            }
                        },
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (metrics.isRecording) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = if (metrics.isRecording) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (metrics.isRecording) "Pause" else "Start",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Stop & Save Button
                    val hasActiveData = metrics.isRecording || timerSeconds > 0 || metrics.distanceMiles > 0
                    Button(
                        onClick = {
                            workoutViewModel.stopAndSaveWorkout(locale, userWeightKg, activeMapView)
                        },
                        enabled = hasActiveData,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Stop & Save",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Reset Button
                    OutlinedButton(
                        onClick = {
                            workoutViewModel.resetWorkout()
                        },
                        modifier = Modifier.weight(0.8f),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Recent Saved Workouts List Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Saved Workouts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${savedWorkouts.size} saved",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (savedWorkouts.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No saved workouts yet",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Press 'Start', run your route, and tap 'Stop & Save' to record your workout history.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    savedWorkouts.forEach { workout ->
                        SavedWorkoutCard(
                            workout = workout,
                            locale = locale,
                            onDelete = {
                                workoutViewModel.deleteSavedWorkout(workout.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedWorkoutCard(
    workout: SavedWorkout,
    locale: Locale,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    val mapBitmap = remember(workout.mapImagePath) {
        workout.mapImagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                try {
                    BitmapFactory.decodeFile(path)?.asImageBitmap()
                } catch (_: Exception) {
                    null
                }
            } else null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workout.dateString.ifEmpty { "Outdoor Run" },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            shareWorkout(context, workout, locale)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Workout",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Workout",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (mapBitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Image(
                        bitmap = mapBitmap,
                        contentDescription = "Saved Workout Map Route",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "📍 Captured Route Map",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format(locale, "%.2f", workout.distanceMiles),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "miles",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format(locale, "(%.2f km)", workout.distanceKm),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val hrs = workout.durationSeconds / 3600
                val mins = (workout.durationSeconds % 3600) / 60
                val secs = workout.durationSeconds % 60
                val timeStr = if (hrs > 0) String.format(locale, "%02d:%02d:%02d", hrs, mins, secs)
                else String.format(locale, "%02d:%02d", mins, secs)

                val paceMins = workout.paceMinsPerMile.toInt()
                val paceSecs = ((workout.paceMinsPerMile - paceMins) * 60).toInt().coerceIn(0, 59)
                val paceStr = if (workout.distanceMiles > 0) String.format(locale, "%d'%02d\"", paceMins, paceSecs) else "--'--"

                CompactStatBadge(title = "Time", value = timeStr)
                CompactStatBadge(title = "Calories", value = String.format(locale, "%.0f kcal", workout.caloriesBurned))
                CompactStatBadge(title = "Pace", value = "$paceStr/mi")
                CompactStatBadge(title = "Speed", value = String.format(locale, "%.1f mph", workout.avgSpeedMph))
            }
        }
    }
}

private fun shareWorkout(context: Context, workout: SavedWorkout, locale: Locale) {
    try {
        val hrs = workout.durationSeconds / 3600
        val mins = (workout.durationSeconds % 3600) / 60
        val secs = workout.durationSeconds % 60
        val timeStr = if (hrs > 0) String.format(locale, "%02d:%02d:%02d", hrs, mins, secs)
        else String.format(locale, "%02d:%02d", mins, secs)

        val paceMins = workout.paceMinsPerMile.toInt()
        val paceSecs = ((workout.paceMinsPerMile - paceMins) * 60).toInt().coerceIn(0, 59)
        val paceStr = if (workout.distanceMiles > 0) String.format(locale, "%d'%02d\"", paceMins, paceSecs) else "--'--"

        val shareText = """
            🏃‍♂️ Outdoor Workout with PaceBurn
            
            📍 Distance: ${String.format(locale, "%.2f", workout.distanceMiles)} mi (${String.format(locale, "%.2f", workout.distanceKm)} km)
            ⏱️ Duration: $timeStr
            ⚡ Pace: $paceStr/mi
            🔥 Calories: ${String.format(locale, "%.0f", workout.caloriesBurned)} kcal
            💨 Avg Speed: ${String.format(locale, "%.1f", workout.avgSpeedMph)} mph
            
            #PaceBurn #FitnessTracker #Workout
        """.trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            val imagePath = workout.mapImagePath
            if (imagePath != null) {
                val imageFile = File(imagePath)
                if (imageFile.exists()) {
                    val uri: Uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        imageFile
                    )
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "image/png"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    type = "text/plain"
                }
            } else {
                type = "text/plain"
            }
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Workout"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Unable to share workout", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun CompactStatBadge(
    title: String,
    value: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun WorkoutStatItem(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
