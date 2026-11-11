package com.karan.paceburn.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class DailyRecord(
    val date: String,
    val dayOfWeek: String,
    val steps: Int,
    val calories: Float,
    val distanceKm: Float
)

data class SavedWorkout(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = "",
    val distanceMiles: Float = 0f,
    val distanceKm: Float = 0f,
    val durationSeconds: Long = 0L,
    val caloriesBurned: Float = 0f,
    val avgSpeedMph: Float = 0f,
    val paceMinsPerMile: Float = 0f,
    val mapImagePath: String? = null
)

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("paceburn_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TODAY_DATE = "today_date"
        private const val KEY_TODAY_STEPS = "today_steps"
        private const val KEY_STEP_GOAL = "step_goal"
        private const val KEY_USER_WEIGHT = "user_weight_kg"
        private const val KEY_STEP_LENGTH = "step_length_cm"
        private const val KEY_INITIAL_SENSOR_STEPS = "initial_sensor_steps"
        private const val KEY_ACTIVE_TIME_SECONDS = "active_time_seconds"
        private const val KEY_DAILY_HISTORY = "daily_history_json"
        private const val KEY_SAVED_WORKOUTS = "saved_workouts_json"
        private const val KEY_UNLOCKED_AWARDS_SET = "unlocked_awards_set"
        private const val KEY_AWARDS_INITIALIZED = "awards_initialized_v1"
        private const val KEY_DAILY_QUOTES_ENABLED = "daily_quotes_enabled"

        const val DEFAULT_STEP_GOAL = 10000
        const val DEFAULT_WEIGHT_KG = 70.0f
        const val DEFAULT_STEP_LENGTH_CM = 75.0f
    }

    init {
        checkNewDayReset()
    }

    fun checkNewDayReset() {
        val currentDate = getCurrentDateString()
        val lastSavedDate = prefs.getString(KEY_TODAY_DATE, "") ?: ""

        if (lastSavedDate.isNotEmpty() && lastSavedDate != currentDate) {
            val lastSteps = getTodaySteps()
            if (lastSteps > 0) {
                val lastCalories = calculateCalories(lastSteps, getUserWeight())
                val lastDistance = calculateDistanceKm(lastSteps, getStepLength())
                archiveDayRecord(
                    DailyRecord(
                        date = lastSavedDate,
                        dayOfWeek = getDayOfWeekFromDate(lastSavedDate),
                        steps = lastSteps,
                        calories = lastCalories,
                        distanceKm = lastDistance
                    )
                )
            }
            prefs.edit()
                .putString(KEY_TODAY_DATE, currentDate)
                .putInt(KEY_TODAY_STEPS, 0)
                .putInt(KEY_INITIAL_SENSOR_STEPS, -1)
                .putLong(KEY_ACTIVE_TIME_SECONDS, 0L)
                .apply()
        } else if (lastSavedDate.isEmpty()) {
            prefs.edit().putString(KEY_TODAY_DATE, currentDate).apply()
        }
    }

    fun getTodaySteps(): Int = prefs.getInt(KEY_TODAY_STEPS, 0)

    fun saveTodaySteps(steps: Int) {
        prefs.edit().putInt(KEY_TODAY_STEPS, steps.coerceAtLeast(0)).apply()
    }

    fun getStepGoal(): Int = prefs.getInt(KEY_STEP_GOAL, DEFAULT_STEP_GOAL)

    fun saveStepGoal(goal: Int) {
        prefs.edit().putInt(KEY_STEP_GOAL, goal.coerceAtLeast(1000)).apply()
    }

    fun getUserWeight(): Float = prefs.getFloat(KEY_USER_WEIGHT, DEFAULT_WEIGHT_KG)

    fun saveUserWeight(weightKg: Float) {
        prefs.edit().putFloat(KEY_USER_WEIGHT, weightKg.coerceAtLeast(30.0f)).apply()
    }

    fun getStepLength(): Float = prefs.getFloat(KEY_STEP_LENGTH, DEFAULT_STEP_LENGTH_CM)

    fun saveStepLength(stepLengthCm: Float) {
        prefs.edit().putFloat(KEY_STEP_LENGTH, stepLengthCm.coerceAtLeast(30.0f)).apply()
    }

    fun getInitialSensorSteps(): Int = prefs.getInt(KEY_INITIAL_SENSOR_STEPS, -1)

    fun saveInitialSensorSteps(steps: Int) {
        prefs.edit().putInt(KEY_INITIAL_SENSOR_STEPS, steps).apply()
    }

    fun getActiveTimeSeconds(): Long = prefs.getLong(KEY_ACTIVE_TIME_SECONDS, 0L)

    fun saveActiveTimeSeconds(seconds: Long) {
        prefs.edit().putLong(KEY_ACTIVE_TIME_SECONDS, seconds.coerceAtLeast(0L)).apply()
    }

    fun archiveDayRecord(record: DailyRecord) {
        val historyList = getHistory().toMutableList()
        historyList.removeAll { it.date == record.date }
        historyList.add(0, record)

        // Keep last 30 days
        val trimmedList = historyList.take(30)
        saveHistoryList(trimmedList)
    }

    private fun saveHistoryList(list: List<DailyRecord>) {
        val jsonArray = JSONArray()
        list.forEach { item ->
            val json = JSONObject().apply {
                put("date", item.date)
                put("dayOfWeek", item.dayOfWeek)
                put("steps", item.steps)
                put("calories", item.calories.toDouble())
                put("distanceKm", item.distanceKm.toDouble())
            }
            jsonArray.put(json)
        }
        prefs.edit().putString(KEY_DAILY_HISTORY, jsonArray.toString()).apply()
    }

    fun getHistory(): List<DailyRecord> {
        val jsonString = prefs.getString(KEY_DAILY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<DailyRecord>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    DailyRecord(
                        date = obj.getString("date"),
                        dayOfWeek = obj.optString("dayOfWeek", "Day"),
                        steps = obj.getInt("steps"),
                        calories = obj.getDouble("calories").toFloat(),
                        distanceKm = obj.getDouble("distanceKm").toFloat()
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_DAILY_HISTORY).apply()
    }

    fun resetTodayData() {
        prefs.edit()
            .putInt(KEY_TODAY_STEPS, 0)
            .putInt(KEY_INITIAL_SENSOR_STEPS, -1)
            .putLong(KEY_ACTIVE_TIME_SECONDS, 0L)
            .apply()
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getDayOfWeekFromDate(dateStr: String): String {
        return try {
            val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdfInput.parse(dateStr) ?: Date()
            val sdfOutput = SimpleDateFormat("EEE", Locale.getDefault())
            sdfOutput.format(date)
        } catch (e: Exception) {
            "Day"
        }
    }

    fun calculateCalories(steps: Int, weightKg: Float): Float {
        return steps * weightKg * 0.00057f
    }

    fun calculateDistanceKm(steps: Int, stepLengthCm: Float): Float {
        return (steps * stepLengthCm) / 100000.0f
    }

    fun getUnlockedAwardIds(): Set<String> {
        return prefs.getStringSet(KEY_UNLOCKED_AWARDS_SET, emptySet()) ?: emptySet()
    }

    fun saveUnlockedAwardId(awardId: String) {
        val currentSet = getUnlockedAwardIds().toMutableSet()
        currentSet.add(awardId)
        prefs.edit().putStringSet(KEY_UNLOCKED_AWARDS_SET, currentSet).apply()
    }

    fun isAwardsInitialized(): Boolean {
        return prefs.getBoolean(KEY_AWARDS_INITIALIZED, false)
    }

    fun setAwardsInitialized() {
        prefs.edit().putBoolean(KEY_AWARDS_INITIALIZED, true).apply()
    }

    fun isDailyQuotesEnabled(): Boolean {
        return prefs.getBoolean(KEY_DAILY_QUOTES_ENABLED, true)
    }

    fun setDailyQuotesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DAILY_QUOTES_ENABLED, enabled).apply()
    }

    fun saveWorkout(workout: SavedWorkout) {
        val currentList = getSavedWorkouts().toMutableList()
        currentList.add(0, workout)
        saveWorkoutsList(currentList)
    }

    private fun saveWorkoutsList(list: List<SavedWorkout>) {
        val jsonArray = JSONArray()
        list.forEach { item ->
            val json = JSONObject().apply {
                put("id", item.id)
                put("timestamp", item.timestamp)
                put("dateString", item.dateString)
                put("distanceMiles", item.distanceMiles.toDouble())
                put("distanceKm", item.distanceKm.toDouble())
                put("durationSeconds", item.durationSeconds)
                put("caloriesBurned", item.caloriesBurned.toDouble())
                put("avgSpeedMph", item.avgSpeedMph.toDouble())
                put("paceMinsPerMile", item.paceMinsPerMile.toDouble())
                put("mapImagePath", item.mapImagePath ?: "")
            }
            jsonArray.put(json)
        }
        prefs.edit().putString(KEY_SAVED_WORKOUTS, jsonArray.toString()).apply()
    }

    fun getSavedWorkouts(): List<SavedWorkout> {
        val jsonString = prefs.getString(KEY_SAVED_WORKOUTS, null) ?: return emptyList()
        val list = mutableListOf<SavedWorkout>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val imgPath = obj.optString("mapImagePath", "")
                list.add(
                    SavedWorkout(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        dateString = obj.optString("dateString", ""),
                        distanceMiles = obj.optDouble("distanceMiles", 0.0).toFloat(),
                        distanceKm = obj.optDouble("distanceKm", 0.0).toFloat(),
                        durationSeconds = obj.optLong("durationSeconds", 0L),
                        caloriesBurned = obj.optDouble("caloriesBurned", 0.0).toFloat(),
                        avgSpeedMph = obj.optDouble("avgSpeedMph", 0.0).toFloat(),
                        paceMinsPerMile = obj.optDouble("paceMinsPerMile", 0.0).toFloat(),
                        mapImagePath = if (imgPath.isNotEmpty()) imgPath else null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun deleteSavedWorkout(id: String) {
        val currentList = getSavedWorkouts().toMutableList()
        currentList.removeAll { it.id == id }
        saveWorkoutsList(currentList)
    }
}
