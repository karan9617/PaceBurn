package com.example.paceburn.data

import com.example.paceburn.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserLevelInfo(
    val level: Int,
    val title: String,
    val totalXp: Int,
    val currentLevelXp: Int,
    val xpForNextLevel: Int,
    val unlockedBadgeCount: Int,
    val totalBadgeCount: Int
)

class AwardManager(private val preferencesManager: PreferencesManager) {

    fun calculateAwards(): List<Award> {
        val todaySteps = preferencesManager.getTodaySteps()
        val stepGoal = preferencesManager.getStepGoal()
        val weight = preferencesManager.getUserWeight()
        val stepLength = preferencesManager.getStepLength()

        val todayCalories = preferencesManager.calculateCalories(todaySteps, weight)
        val todayDistanceKm = preferencesManager.calculateDistanceKm(todaySteps, stepLength)

        val history = preferencesManager.getHistory()

        // Calculate max single day values across today and history
        val allDaysSteps = listOf(todaySteps) + history.map { it.steps }
        val allDaysCalories = listOf(todayCalories) + history.map { it.calories }
        val allDaysDistanceKm = listOf(todayDistanceKm) + history.map { it.distanceKm }

        val maxSteps = allDaysSteps.maxOrNull() ?: todaySteps
        val maxCalories = allDaysCalories.maxOrNull() ?: todayCalories
        val maxDistanceKm = allDaysDistanceKm.maxOrNull() ?: todayDistanceKm
        val maxDistanceMiles = maxDistanceKm * 0.621371f

        // Calculate current streak
        val currentStreak = calculateStreak(history, todaySteps, stepGoal)

        val currentDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val awardList = mutableListOf<Award>()

        // 1. FIRST STEPS
        awardList.add(
            Award(
                id = "first_steps",
                title = "First Steps",
                description = "Walk at least 1,000 steps in a day",
                category = AwardCategory.STEPS,
                iconName = "DirectionsWalk",
                drawableResId = R.drawable.first_steps,
                requiredValue = 1000f,
                currentValue = maxSteps.toFloat(),
                isUnlocked = maxSteps >= 1000,
                xpReward = 50,
                unlockDate = if (maxSteps >= 1000) currentDateStr else null
            )
        )

        // 2. PACE STARTER
        awardList.add(
            Award(
                id = "steps_5k",
                title = "Pace Starter",
                description = "Reach 5,000 steps in a single day",
                category = AwardCategory.STEPS,
                iconName = "FlashOn",
                drawableResId = R.drawable.pace_starter,
                requiredValue = 5000f,
                currentValue = maxSteps.toFloat(),
                isUnlocked = maxSteps >= 5000,
                xpReward = 100,
                unlockDate = if (maxSteps >= 5000) currentDateStr else null
            )
        )

        // 3. 10K GOAL CRUSHER
        awardList.add(
            Award(
                id = "steps_10k",
                title = "10K Goal Crusher",
                description = "Hit the 10,000 steps daily milestone",
                category = AwardCategory.STEPS,
                iconName = "EmojiEvents",
                drawableResId = R.drawable.ten_k_goal_crusher,
                requiredValue = 10000f,
                currentValue = maxSteps.toFloat(),
                isUnlocked = maxSteps >= 10000,
                xpReward = 200,
                unlockDate = if (maxSteps >= 10000) currentDateStr else null
            )
        )

        // 4. 15K STEP CRUSHER
        awardList.add(
            Award(
                id = "steps_15k",
                title = "15K Step Crusher",
                description = "Reach 15,000 steps in a single day",
                category = AwardCategory.STEPS,
                iconName = "MilitaryTech",
                drawableResId = R.drawable.fifteen_k_goal_crusher,
                requiredValue = 15000f,
                currentValue = maxSteps.toFloat(),
                isUnlocked = maxSteps >= 15000,
                xpReward = 350,
                unlockDate = if (maxSteps >= 15000) currentDateStr else null
            )
        )

        // 5. 20K TITAN WALK
        awardList.add(
            Award(
                id = "steps_20k",
                title = "20K Titan Walk",
                description = "Conquer 20,000 steps in a single day",
                category = AwardCategory.STEPS,
                iconName = "FitnessCenter",
                drawableResId = R.drawable.twenty_k_titan_walk,
                requiredValue = 20000f,
                currentValue = maxSteps.toFloat(),
                isUnlocked = maxSteps >= 20000,
                xpReward = 500,
                unlockDate = if (maxSteps >= 20000) currentDateStr else null
            )
        )

        // 6. 30K ULTRA WALKER
        awardList.add(
            Award(
                id = "steps_30k",
                title = "30K Ultra Walker",
                description = "Crush 30,000 steps in a single day",
                category = AwardCategory.STEPS,
                iconName = "WorkspacePremium",
                drawableResId = R.drawable.pacetracker,
                requiredValue = 30000f,
                currentValue = maxSteps.toFloat(),
                isUnlocked = maxSteps >= 30000,
                xpReward = 1000,
                unlockDate = if (maxSteps >= 30000) currentDateStr else null
            )
        )

        // 7. 3-DAY STREAK
        awardList.add(
            Award(
                id = "streak_3",
                title = "3-Day Ignition",
                description = "Reach your step goal 3 days in a row",
                category = AwardCategory.STREAK,
                iconName = "LocalFireDepartment",
                drawableResId = R.drawable.three_day_ignition_walk,
                requiredValue = 3f,
                currentValue = currentStreak.toFloat(),
                isUnlocked = currentStreak >= 3,
                xpReward = 150,
                unlockDate = if (currentStreak >= 3) currentDateStr else null
            )
        )

        // 8. 7-DAY STREAK
        awardList.add(
            Award(
                id = "streak_7",
                title = "7-Day Streak Warrior",
                description = "Hit your step target 7 days in a row",
                category = AwardCategory.STREAK,
                iconName = "Whatshot",
                drawableResId = R.drawable.seven_day_streak,
                requiredValue = 7f,
                currentValue = currentStreak.toFloat(),
                isUnlocked = currentStreak >= 7,
                xpReward = 300,
                unlockDate = if (currentStreak >= 7) currentDateStr else null
            )
        )

        // 9. 14-DAY CHAMPION
        awardList.add(
            Award(
                id = "streak_14",
                title = "14-Day Champion",
                description = "Maintain a 14-day goal streak",
                category = AwardCategory.STREAK,
                iconName = "Verified",
                drawableResId = R.drawable.fourteen_day,
                requiredValue = 14f,
                currentValue = currentStreak.toFloat(),
                isUnlocked = currentStreak >= 14,
                xpReward = 600,
                unlockDate = if (currentStreak >= 14) currentDateStr else null
            )
        )

        // 10. 30-DAY MASTER
        awardList.add(
            Award(
                id = "streak_30",
                title = "30-Day Master",
                description = "Achieve a perfect 30-day streak",
                category = AwardCategory.STREAK,
                iconName = "MilitaryTech",
                drawableResId = R.drawable.thirty_streak_master,
                requiredValue = 30f,
                currentValue = currentStreak.toFloat(),
                isUnlocked = currentStreak >= 30,
                xpReward = 1200,
                unlockDate = if (currentStreak >= 30) currentDateStr else null
            )
        )

        // 11. CALORIE IGNITER
        awardList.add(
            Award(
                id = "cal_300",
                title = "Calorie Igniter",
                description = "Burn at least 300 kcal in a single day",
                category = AwardCategory.CALORIES,
                iconName = "LocalFireDepartment",
                drawableResId = R.drawable.calorie_igniter,
                requiredValue = 300f,
                currentValue = maxCalories,
                isUnlocked = maxCalories >= 300f,
                xpReward = 100,
                unlockDate = if (maxCalories >= 300f) currentDateStr else null
            )
        )

        // 12. INFERNO BURNER
        awardList.add(
            Award(
                id = "cal_600",
                title = "Metabolic warrior",
                description = "Burn 600 kcal in a single day",
                category = AwardCategory.CALORIES,
                iconName = "Metabolic warrior",
                drawableResId = R.drawable.metabolic_warrior,
                requiredValue = 600f,
                currentValue = maxCalories,
                isUnlocked = maxCalories >= 600f,
                xpReward = 250,
                unlockDate = if (maxCalories >= 600f) currentDateStr else null
            )
        )

        // 13. 1000 KCAL FLAME
        awardList.add(
            Award(
                id = "cal_1000",
                title = "1000 Kcal Flame",
                description = "Burn 1,000 kcal in a single day",
                category = AwardCategory.CALORIES,
                iconName = "Thunderstorm",
                drawableResId = R.drawable.thousand_calorie_batch,
                requiredValue = 1000f,
                currentValue = maxCalories,
                isUnlocked = maxCalories >= 1000f,
                xpReward = 500,
                unlockDate = if (maxCalories >= 1000f) currentDateStr else null
            )
        )

        // 14. 5 KM EXPLORER
        awardList.add(
            Award(
                id = "dist_5k",
                title = "5 Km Explorer",
                description = "Cover 5 km (3.1 miles) in a day",
                category = AwardCategory.DISTANCE,
                iconName = "Map",
                drawableResId = R.drawable.five_km_explorer,
                requiredValue = 5.0f,
                currentValue = maxDistanceKm,
                isUnlocked = maxDistanceKm >= 5.0f,
                xpReward = 150,
                unlockDate = if (maxDistanceKm >= 5.0f) currentDateStr else null
            )
        )

        // 15. 10 MILE ENDURANCE
        awardList.add(
            Award(
                id = "dist_10mi",
                title = "10 Mile Endurance",
                description = "Run/Walk 10 miles (16.1 km) in a day",
                category = AwardCategory.DISTANCE,
                iconName = "DirectionsRun",
                drawableResId = R.drawable.ten_miles_endurance,
                requiredValue = 10.0f,
                currentValue = maxDistanceMiles,
                isUnlocked = maxDistanceMiles >= 10.0f,
                xpReward = 450,
                unlockDate = if (maxDistanceMiles >= 10.0f) currentDateStr else null
            )
        )

        // 16. GPS PATHFINDER
        awardList.add(
            Award(
                id = "special_gps",
                title = "GPS Pathfinder",
                description = "Record an outdoor workout session on the map",
                category = AwardCategory.SPECIAL,
                iconName = "LocationOn",
                drawableResId = R.drawable.gps_pathfinder,
                requiredValue = 1.0f,
                currentValue = if (maxDistanceKm >= 1.0f) 1.0f else 0.5f,
                isUnlocked = maxDistanceKm >= 1.0f,
                xpReward = 200,
                unlockDate = if (maxDistanceKm >= 1.0f) currentDateStr else null
            )
        )

        // 17. WEEKEND WARRIOR
        awardList.add(
            Award(
                id = "special_weekend",
                title = "Weekend Warrior",
                description = "Stay active and hit step targets over the weekend",
                category = AwardCategory.SPECIAL,
                iconName = "MilitaryTech",
                drawableResId = R.drawable.pacetracker,
                requiredValue = 1.0f,
                currentValue = if (todaySteps >= stepGoal) 1.0f else 0.5f,
                isUnlocked = todaySteps >= stepGoal,
                xpReward = 250,
                unlockDate = if (todaySteps >= stepGoal) currentDateStr else null
            )
        )

        // 18. PACEBURN LEGEND
        val currentTotalXp = awardList.filter { it.isUnlocked }.sumOf { it.xpReward }
        awardList.add(
            Award(
                id = "special_legend",
                title = "PaceBurn Legend",
                description = "Earn 1,000 XP by unlocking fitness milestones",
                category = AwardCategory.SPECIAL,
                iconName = "Star",
                drawableResId = R.drawable.paceburn_legend,
                requiredValue = 1000f,
                currentValue = currentTotalXp.toFloat(),
                isUnlocked = currentTotalXp >= 1000,
                xpReward = 800,
                unlockDate = if (currentTotalXp >= 1000) currentDateStr else null
            )
        )

        return awardList
    }

    fun calculateUserLevel(awards: List<Award>): UserLevelInfo {
        val unlockedList = awards.filter { it.isUnlocked }
        val totalXp = unlockedList.sumOf { it.xpReward }

        val level = (totalXp / 300) + 1
        val levelTitles = listOf(
            "Novice Walker",
            "Pace Setter",
            "Active Runner",
            "Blaze Walker",
            "Fitness Titan",
            "Burn Master",
            "Endurance Legend",
            "PaceBurn Supreme"
        )
        val title = levelTitles.getOrElse(level - 1) { "PaceBurn Supreme" }

        val xpInCurrentLevel = totalXp % 300
        val xpForNextLevel = 300

        return UserLevelInfo(
            level = level,
            title = title,
            totalXp = totalXp,
            currentLevelXp = xpInCurrentLevel,
            xpForNextLevel = xpForNextLevel,
            unlockedBadgeCount = unlockedList.size,
            totalBadgeCount = awards.size
        )
    }

    private fun calculateStreak(history: List<DailyRecord>, todaySteps: Int, stepGoal: Int): Int {
        var streak = 0
        if (todaySteps >= stepGoal) {
            streak++
        }
        for (record in history) {
            if (record.steps >= stepGoal) {
                streak++
            } else {
                break
            }
        }
        return streak
    }
}
