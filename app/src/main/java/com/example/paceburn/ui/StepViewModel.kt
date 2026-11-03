package com.example.paceburn.ui

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.paceburn.data.AwardManager
import com.example.paceburn.data.DailyRecord
import com.example.paceburn.data.PreferencesManager
import com.example.paceburn.sensor.StepSensorManager
import com.example.paceburn.service.StepCounterService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StepUiState(
    val steps: Int = 0,
    val goal: Int = PreferencesManager.DEFAULT_STEP_GOAL,
    val calories: Float = 0f,
    val distanceKm: Float = 0f,
    val activeTimeSeconds: Long = 0L,
    val userWeightKg: Float = PreferencesManager.DEFAULT_WEIGHT_KG,
    val stepLengthCm: Float = PreferencesManager.DEFAULT_STEP_LENGTH_CM,
    val isTracking: Boolean = true,
    val hasHardwareSensor: Boolean = true,
    val hasPermission: Boolean = true,
    val history: List<DailyRecord> = emptyList()
)

class StepViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val stepSensorManager = StepSensorManager(application)
    private val awardManager = AwardManager(preferencesManager)

    private val _uiState = MutableStateFlow(StepUiState())
    val uiState: StateFlow<StepUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var isServiceActive = false

    init {
        refreshState()
        checkHardwareSensorAndPermission()

        stepSensorManager.onStepIncrementListener = { increment ->
            // In-app fallback listener when service is not active
            if (!isServiceActive) {
                val currentSteps = preferencesManager.getTodaySteps() + increment
                preferencesManager.saveTodaySteps(currentSteps)
                refreshState()
            }
        }

        if (_uiState.value.isTracking) {
            startActiveTimer()
            if (_uiState.value.hasPermission) {
                startStepService()
            } else {
                stepSensorManager.startListening()
            }
        }
    }

    fun refreshState() {
        preferencesManager.checkNewDayReset()
        awardManager.checkAndNotifyNewAwards(getApplication())
        val steps = preferencesManager.getTodaySteps()
        val goal = preferencesManager.getStepGoal()
        val weight = preferencesManager.getUserWeight()
        val stepLength = preferencesManager.getStepLength()
        val calories = preferencesManager.calculateCalories(steps, weight)
        val distance = preferencesManager.calculateDistanceKm(steps, stepLength)
        val activeTime = preferencesManager.getActiveTimeSeconds()
        val history = preferencesManager.getHistory()

        _uiState.value = _uiState.value.copy(
            steps = steps,
            goal = goal,
            calories = calories,
            distanceKm = distance,
            activeTimeSeconds = activeTime,
            userWeightKg = weight,
            stepLengthCm = stepLength,
            history = history
        )
    }

    fun checkHardwareSensorAndPermission() {
        val hasSensor = stepSensorManager.isSensorAvailable()
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                getApplication(),
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        _uiState.value = _uiState.value.copy(
            hasHardwareSensor = hasSensor,
            hasPermission = hasPermission
        )
    }

    fun setPermissionGranted(granted: Boolean) {
        _uiState.value = _uiState.value.copy(hasPermission = granted)
        if (granted && _uiState.value.isTracking) {
            stepSensorManager.stopListening()
            startStepService()
        }
    }

    fun addSteps(count: Int) {
        val currentSteps = _uiState.value.steps + count
        preferencesManager.saveTodaySteps(currentSteps)

        // Increment active time slightly when steps are added (e.g. 10 steps ≈ 5 seconds)
        val addedTimeSeconds = (count * 0.5f).toLong().coerceAtLeast(1L)
        val updatedTime = _uiState.value.activeTimeSeconds + addedTimeSeconds
        preferencesManager.saveActiveTimeSeconds(updatedTime)

        refreshState()
    }

    fun toggleTracking() {
        val newTrackingState = !_uiState.value.isTracking
        _uiState.value = _uiState.value.copy(isTracking = newTrackingState)

        if (newTrackingState) {
            startActiveTimer()
            if (_uiState.value.hasPermission) {
                stepSensorManager.stopListening()
                startStepService()
            } else {
                stepSensorManager.startListening()
            }
        } else {
            stopActiveTimer()
            stepSensorManager.stopListening()
            stopStepService()
        }
    }

    fun resetTodaySteps() {
        preferencesManager.resetTodayData()
        refreshState()
    }

    fun updateStepGoal(newGoal: Int) {
        preferencesManager.saveStepGoal(newGoal)
        refreshState()
    }

    fun updateUserProfile(weightKg: Float, stepLengthCm: Float) {
        preferencesManager.saveUserWeight(weightKg)
        preferencesManager.saveStepLength(stepLengthCm)
        refreshState()
    }

    private fun startActiveTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (_uiState.value.isTracking) {
                    val updatedTime = _uiState.value.activeTimeSeconds + 1
                    preferencesManager.saveActiveTimeSeconds(updatedTime)
                    refreshState()
                }
            }
        }
    }

    private fun stopActiveTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun startStepService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                getApplication(),
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) {
                stepSensorManager.startListening()
                return
            }
        }

        val context = getApplication<Application>()
        val intent = Intent(context, StepCounterService::class.java).apply {
            action = StepCounterService.ACTION_START
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            isServiceActive = true
            stepSensorManager.stopListening()
        } catch (e: Exception) {
            e.printStackTrace()
            isServiceActive = false
            stepSensorManager.startListening()
        }
    }

    private fun stopStepService() {
        val context = getApplication<Application>()
        val intent = Intent(context, StepCounterService::class.java).apply {
            action = StepCounterService.ACTION_STOP
        }
        try {
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isServiceActive = false
    }

    override fun onCleared() {
        stopActiveTimer()
        stepSensorManager.stopListening()
        super.onCleared()
    }
}
