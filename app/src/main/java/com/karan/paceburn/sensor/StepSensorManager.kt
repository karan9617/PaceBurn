package com.karan.paceburn.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class SensorStatus {
    object Active : SensorStatus()
    object HardwareSensorMissing : SensorStatus()
    object Stopped : SensorStatus()
}

class StepSensorManager(context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val prefs = context.applicationContext
        .getSharedPreferences("step_sensor_state", Context.MODE_PRIVATE)

    private val stepCounterSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val _sensorStatus = MutableStateFlow<SensorStatus>(SensorStatus.Stopped)
    val sensorStatus: StateFlow<SensorStatus> = _sensorStatus.asStateFlow()

    var onStepIncrementListener: ((Int) -> Unit)? = null

    private var isRegistered = false

    fun isSensorAvailable(): Boolean = stepCounterSensor != null

    fun startListening() {
        val sensor = stepCounterSensor
        if (sensor == null) {
            _sensorStatus.value = SensorStatus.HardwareSensorMissing
            return
        }
        if (isRegistered) return

        // Baseline is NOT reset here, so steps taken while we weren't listening
        // are picked up as a delta on the first event.
        val success = sensorManager.registerListener(
            this, sensor, SensorManager.SENSOR_DELAY_NORMAL
        )
        isRegistered = success
        _sensorStatus.value =
            if (success) SensorStatus.Active else SensorStatus.HardwareSensorMissing
    }

    fun stopListening() {
        if (!isRegistered) return
        sensorManager.unregisterListener(this)
        isRegistered = false
        _sensorStatus.value = SensorStatus.Stopped
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val total = event.values[0].toLong()
                val last = prefs.getLong(KEY_LAST_COUNTER, -1L)

                val delta = when {
                    last < 0 -> 0L          // very first reading ever: baseline only
                    total < last -> total   // reboot: counter restarted from 0
                    else -> total - last
                }

                prefs.edit().putLong(KEY_LAST_COUNTER, total).apply()

                if (delta > 0) onStepIncrementListener?.invoke(delta.toInt())
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values[0] == 1.0f) onStepIncrementListener?.invoke(1)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private companion object {
        const val KEY_LAST_COUNTER = "last_counter"
    }
}