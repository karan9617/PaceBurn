package com.karan.paceburn.ui

import android.app.Application
import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.karan.paceburn.data.PreferencesManager
import com.karan.paceburn.data.SavedWorkout
import com.karan.paceburn.location.LocationPoint
import com.karan.paceburn.location.LocationTracker
import com.karan.paceburn.location.WorkoutMetrics
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    val locationTracker = LocationTracker(application)
    val metrics: StateFlow<WorkoutMetrics> = locationTracker.workoutMetrics

    private val prefsManager = PreferencesManager(application)

    private val _timerSeconds = MutableStateFlow(0L)
    val timerSeconds: StateFlow<Long> = _timerSeconds.asStateFlow()

    private val _savedWorkouts = MutableStateFlow(prefsManager.getSavedWorkouts())
    val savedWorkouts: StateFlow<List<SavedWorkout>> = _savedWorkouts.asStateFlow()

    private var timerJob: Job? = null

    fun hasLocationPermission(): Boolean = locationTracker.hasLocationPermission()

    fun startWorkout(userWeightKg: Float = 70f) {
        if (!hasLocationPermission()) return
        locationTracker.startTracking()
        startTimer(userWeightKg)
    }

    fun pauseWorkout() {
        locationTracker.stopTracking()
        stopTimer()
    }

    fun stopAndSaveWorkout(locale: Locale, userWeightKg: Float = 70f, mapView: MapView? = null) {
        pauseWorkout()

        val currentMetrics = metrics.value
        val currentTimer = _timerSeconds.value

        if (currentMetrics.isRecording || currentTimer > 0 || currentMetrics.distanceMiles > 0) {
            val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", locale)
            val workoutId = UUID.randomUUID().toString()
            var savedMapPath: String? = null

            if (mapView != null && currentMetrics.routePoints.isNotEmpty()) {
                savedMapPath = saveMapViewSnapshot(mapView, workoutId, currentMetrics.routePoints)
            }

            val newWorkout = SavedWorkout(
                id = workoutId,
                dateString = sdf.format(Date()),
                distanceMiles = currentMetrics.distanceMiles,
                distanceKm = currentMetrics.distanceKm,
                durationSeconds = currentTimer,
                caloriesBurned = currentMetrics.caloriesBurned,
                avgSpeedMph = currentMetrics.avgSpeedMph,
                paceMinsPerMile = currentMetrics.paceMinsPerMile,
                mapImagePath = savedMapPath
            )
            prefsManager.saveWorkout(newWorkout)
            _savedWorkouts.value = prefsManager.getSavedWorkouts()

            resetWorkout()
            Toast.makeText(getApplication(), "Workout saved successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveMapViewSnapshot(
        mapView: MapView,
        workoutId: String,
        routePoints: List<LocationPoint>
    ): String? {
        return try {
            val geoPoints = routePoints.map { GeoPoint(it.latitude, it.longitude) }
            if (geoPoints.size >= 2) {
                val box = BoundingBox.fromGeoPoints(geoPoints)
                mapView.zoomToBoundingBox(box, false, 80)
                mapView.invalidate()
            } else if (geoPoints.isNotEmpty()) {
                mapView.controller.setCenter(geoPoints.first())
                mapView.controller.setZoom(17.0)
                mapView.invalidate()
            }

            val width = mapView.width.coerceAtLeast(1)
            val height = mapView.height.coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            mapView.draw(canvas)

            val mapsDir = File(getApplication<Application>().filesDir, "workout_maps")
            if (!mapsDir.exists()) {
                mapsDir.mkdirs()
            }
            val file = File(mapsDir, "map_$workoutId.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun resetWorkout() {
        locationTracker.stopTracking()
        locationTracker.resetWorkout()
        stopTimer()
        _timerSeconds.value = 0L
    }

    fun deleteSavedWorkout(id: String) {
        val target = _savedWorkouts.value.find { it.id == id }
        target?.mapImagePath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        prefsManager.deleteSavedWorkout(id)
        _savedWorkouts.value = prefsManager.getSavedWorkouts()
    }

    private fun startTimer(userWeightKg: Float) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _timerSeconds.value += 1
                locationTracker.updateDuration(_timerSeconds.value, userWeightKg)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        stopTimer()
        locationTracker.stopTracking()
        super.onCleared()
    }
}
