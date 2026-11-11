package com.karan.paceburn.data

enum class AwardCategory(val label: String) {
    ALL("All"),
    STREAK("Streaks"),
    STEPS("Steps"),
    CALORIES("Calories"),
    DISTANCE("Distance"),
    SPECIAL("Special")
}

data class Award(
    val id: String,
    val title: String,
    val description: String,
    val category: AwardCategory,
    val iconName: String,
    val drawableResId: Int? = null,
    val requiredValue: Float,
    val currentValue: Float,
    val isUnlocked: Boolean,
    val xpReward: Int,
    val unlockDate: String? = null
) {
    val progressFraction: Float
        get() = (currentValue / requiredValue.coerceAtLeast(1f)).coerceIn(0f, 1f)
}
