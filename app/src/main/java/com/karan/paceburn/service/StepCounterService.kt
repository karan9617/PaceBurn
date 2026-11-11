package com.karan.paceburn.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import com.karan.paceburn.MainActivity
import com.karan.paceburn.R
import com.karan.paceburn.data.AwardManager
import com.karan.paceburn.data.PreferencesManager
import com.karan.paceburn.sensor.StepSensorManager
import java.util.Locale

class StepCounterService : Service() {

    private val binder = LocalBinder()
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var stepSensorManager: StepSensorManager
    private lateinit var awardManager: AwardManager

    companion object {
        const val CHANNEL_ID = "paceburn_step_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_STEP_TRACKING"
        const val ACTION_STOP = "ACTION_STOP_STEP_TRACKING"
    }

    inner class LocalBinder : Binder() {
        fun getService(): StepCounterService = this@StepCounterService
    }

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
        stepSensorManager = StepSensorManager(this)
        awardManager = AwardManager(preferencesManager)
        createNotificationChannel()

        stepSensorManager.onStepIncrementListener = { increment ->
            val currentSteps = preferencesManager.getTodaySteps() + increment
            preferencesManager.saveTodaySteps(currentSteps)
            awardManager.checkAndNotifyNewAwards(this)
            updateNotification(currentSteps)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_START, null -> {
                val success = startForegroundServiceWithNotification()
                if (success) {
                    startTracking()
                } else {
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    private fun startTracking() {
        stepSensorManager.startListening()
    }

    private fun stopTracking() {
        stepSensorManager.stopListening()
    }

    private fun startForegroundServiceWithNotification(): Boolean {
        // Verify Activity Recognition permission on Android 10+ (API 29+) before calling startForeground health type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                return false
            }
        }

        return try {
            val notification = createNotification(preferencesManager.getTodaySteps())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                    )
                } else {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
                    )
                }
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun updateNotification(steps: Int) {
        try {
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, createNotification(steps))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotification(steps: Int): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val weight = preferencesManager.getUserWeight()
        val calories = preferencesManager.calculateCalories(steps, weight)
        val distance = preferencesManager.calculateDistanceKm(
            steps,
            preferencesManager.getStepLength()
        )

        val contentText = String.format(
            Locale.getDefault(),
            "🔥 %.1f kcal | 🏃 %.2f km | Target: %d steps",
            calories,
            distance,
            preferencesManager.getStepGoal()
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PaceBurn - Steps: $steps")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "PaceBurn Step Counter",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows continuous step count and calories burned"
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        stopTracking()
        super.onDestroy()
    }
}
