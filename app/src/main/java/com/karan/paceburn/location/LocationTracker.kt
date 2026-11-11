package com.karan.paceburn.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class WorkoutMetrics(
    val routePoints: List<LocationPoint> = emptyList(),
    val distanceMeters: Float = 0f,
    val distanceMiles: Float = 0f,
    val distanceKm: Float = 0f,
    val durationSeconds: Long = 0L,
    val caloriesBurned: Float = 0f,
    val avgSpeedMph: Float = 0f,
    val avgSpeedKmh: Float = 0f,
    val paceMinsPerMile: Float = 0f,
    val paceMinsPerKm: Float = 0f,
    val isRecording: Boolean = false,
    val lastLocation: LocationPoint? = null
)

class LocationTracker(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _workoutMetrics = MutableStateFlow(WorkoutMetrics())
    val workoutMetrics: StateFlow<WorkoutMetrics> = _workoutMetrics.asStateFlow()

    private var locationCallback: LocationCallback? = null

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (!hasLocationPermission()) return

        stopTracking()

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        ).apply {
            setMinUpdateIntervalMillis(1000L)
            setMinUpdateDistanceMeters(2f)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    addLocationPoint(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )

        _workoutMetrics.value = _workoutMetrics.value.copy(isRecording = true)
    }

    fun stopTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
        _workoutMetrics.value = _workoutMetrics.value.copy(isRecording = false)
    }

    fun resetWorkout() {
        stopTracking()
        _workoutMetrics.value = WorkoutMetrics()
    }

    fun addLocationPoint(location: Location) {
        val currentPoints = _workoutMetrics.value.routePoints.toMutableList()
        val newPoint = LocationPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            timestamp = location.time
        )

        var addedDistance = 0f
        if (currentPoints.isNotEmpty()) {
            val lastPoint = currentPoints.last()
            val results = FloatArray(1)
            Location.distanceBetween(
                lastPoint.latitude,
                lastPoint.longitude,
                newPoint.latitude,
                newPoint.longitude,
                results
            )
            // Filter noise/jitter (< 1m or > 200m single jump)
            if (results[0] in 1.0f..200.0f) {
                addedDistance = results[0]
            }
        }

        currentPoints.add(newPoint)
        val totalDistanceMeters = _workoutMetrics.value.distanceMeters + addedDistance
        val totalDistanceKm = totalDistanceMeters / 1000.0f
        val totalDistanceMiles = totalDistanceMeters * 0.000621371f

        _workoutMetrics.value = recalculateMetrics(
            currentPoints = currentPoints,
            distanceMeters = totalDistanceMeters,
            distanceKm = totalDistanceKm,
            distanceMiles = totalDistanceMiles,
            durationSeconds = _workoutMetrics.value.durationSeconds,
            userWeightKg = 70.0f
        )
    }

    fun updateDuration(durationSeconds: Long, userWeightKg: Float = 70.0f) {
        val current = _workoutMetrics.value
        _workoutMetrics.value = recalculateMetrics(
            currentPoints = current.routePoints,
            distanceMeters = current.distanceMeters,
            distanceKm = current.distanceKm,
            distanceMiles = current.distanceMiles,
            durationSeconds = durationSeconds,
            userWeightKg = userWeightKg
        )
    }

    private fun recalculateMetrics(
        currentPoints: List<LocationPoint>,
        distanceMeters: Float,
        distanceKm: Float,
        distanceMiles: Float,
        durationSeconds: Long,
        userWeightKg: Float
    ): WorkoutMetrics {
        val hours = durationSeconds / 3600.0f
        val avgSpeedMph = if (hours > 0 && distanceMiles > 0) (distanceMiles / hours) else 0f
        val avgSpeedKmh = if (hours > 0 && distanceKm > 0) (distanceKm / hours) else 0f

        val paceMinsPerMile = if (distanceMiles > 0) (durationSeconds / 60.0f) / distanceMiles else 0f
        val paceMinsPerKm = if (distanceKm > 0) (durationSeconds / 60.0f) / distanceKm else 0f

        // Outdoor Running/Walking Calorie MET formula
        // MET ~8.0 for outdoor running/jogging -> kcal = MET * weightInKg * durationInHours
        val calories = if (distanceKm > 0) {
            (distanceKm * userWeightKg * 1.036f)
        } else {
            (durationSeconds / 3600.0f) * userWeightKg * 7.0f
        }

        return WorkoutMetrics(
            routePoints = currentPoints,
            distanceMeters = distanceMeters,
            distanceMiles = distanceMiles,
            distanceKm = distanceKm,
            durationSeconds = durationSeconds,
            caloriesBurned = calories,
            avgSpeedMph = avgSpeedMph,
            avgSpeedKmh = avgSpeedKmh,
            paceMinsPerMile = paceMinsPerMile,
            paceMinsPerKm = paceMinsPerKm,
            isRecording = _workoutMetrics.value.isRecording,
            lastLocation = currentPoints.lastOrNull()
        )
    }
}
