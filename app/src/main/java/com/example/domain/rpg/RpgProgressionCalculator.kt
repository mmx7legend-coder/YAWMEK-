package com.example.domain.rpg

import java.time.LocalDate
import kotlin.math.floor
import kotlin.math.pow

/**
 * Progression formulas, anti-farming logic, and leveling rules.
 * Intent: Slow, difficult, deeply rewarding progress tied to true consistency.
 */
object RpgProgressionCalculator {

    /**
     * Level XP Curve:
     * Level 1 -> 2: 120 XP
     * Level 2 -> 3: 280 XP
     * Level 5 -> 6: ~1,100 XP
     * Level 20 -> 21: ~15,000 XP
     * Level 50 -> 51: ~90,000 XP
     * Formula: 120 * (Level ^ 1.65)
     */
    fun getXpRequiredForNextLevel(currentLevel: Int): Long {
        if (currentLevel < 1) return 120L
        val base = 120.0
        val exp = 1.65
        return floor(base * (currentLevel.toDouble().pow(exp))).toLong()
    }

    /**
     * XP Awards for real actions (Calibrated strictly to prevent rapid level leaps):
     */
    const val XP_REGULAR_TASK = 18L
    const val XP_HIGH_PRIORITY_TASK = 35L
    const val XP_HABIT_LOGGED = 14L
    const val XP_FOCUS_MINUTE = 1L // 25m Pomodoro -> 25 XP
    const val XP_READING_MINUTE = 2L // 20m Reading -> 40 XP
    const val XP_GOAL_MILESTONE = 60L
    const val XP_DAILY_PLAN_APPLIED = 25L
    const val XP_BRAIN_GAME_SESSION = 15L

    // Anti-farming maximum daily caps per category
    private const val MAX_DAILY_TASKS_FOR_XP = 15
    private const val MAX_DAILY_HABITS_FOR_XP = 12
    private const val MAX_DAILY_FOCUS_MINUTES_FOR_XP = 240 // 4 hours
    private const val MAX_DAILY_READING_MINUTES_FOR_XP = 180 // 3 hours
    private const val MAX_DAILY_GAMES_FOR_XP = 5

    /**
     * Calculates anti-farming adjusted XP
     */
    fun evaluateTaskXp(isHighPriority: Boolean, completedTodayCount: Int): Long {
        if (completedTodayCount > MAX_DAILY_TASKS_FOR_XP) {
            return 2L // Diminishing returns after 15 tasks in a single day
        }
        return if (isHighPriority) XP_HIGH_PRIORITY_TASK else XP_REGULAR_TASK
    }

    fun evaluateHabitXp(habitsLoggedToday: Int): Long {
        if (habitsLoggedToday > MAX_DAILY_HABITS_FOR_XP) {
            return 1L // Diminishing returns
        }
        return XP_HABIT_LOGGED
    }

    fun evaluateFocusXp(minutes: Int, totalFocusedMinutesToday: Int): Long {
        val eligibleMinutes = (MAX_DAILY_FOCUS_MINUTES_FOR_XP - totalFocusedMinutesToday).coerceAtLeast(0)
        val cappedMinutes = minutes.coerceAtMost(eligibleMinutes)
        val excessMinutes = (minutes - cappedMinutes).coerceAtLeast(0)
        return (cappedMinutes * XP_FOCUS_MINUTE) + (excessMinutes * 0)
    }

    fun evaluateReadingXp(minutes: Int, totalReadingMinutesToday: Int): Long {
        val eligibleMinutes = (MAX_DAILY_READING_MINUTES_FOR_XP - totalReadingMinutesToday).coerceAtLeast(0)
        val cappedMinutes = minutes.coerceAtMost(eligibleMinutes)
        return cappedMinutes * XP_READING_MINUTE
    }

    /**
     * Determines dragon stage based on character level and milestones
     */
    fun computeDragonStage(level: Int, completedGoals: Int): com.example.data.local.model.DragonStage {
        return when {
            level >= 85 && completedGoals >= 15 -> com.example.data.local.model.DragonStage.ANCIENT
            level >= 60 && completedGoals >= 10 -> com.example.data.local.model.DragonStage.ADULT
            level >= 40 && completedGoals >= 5 -> com.example.data.local.model.DragonStage.YOUNG
            level >= 25 && completedGoals >= 2 -> com.example.data.local.model.DragonStage.HATCHLING
            level >= 15 -> com.example.data.local.model.DragonStage.EGG
            else -> com.example.data.local.model.DragonStage.NONE
        }
    }

    /**
     * Adventure area unlocked based on level
     */
    fun computeAdventureArea(level: Int): com.example.data.local.model.AdventureArea {
        return com.example.data.local.model.AdventureArea.values()
            .filter { level >= it.unlockLevel }
            .maxByOrNull { it.order } ?: com.example.data.local.model.AdventureArea.VILLAGE
    }
}
