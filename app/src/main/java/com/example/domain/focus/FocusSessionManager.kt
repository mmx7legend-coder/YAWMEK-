package com.example.domain.focus

import com.example.data.local.model.FocusSessionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class FocusAnalytics(
    val todayMinutes: Int,
    val weekMinutes: Int,
    val allTimeMinutes: Int,
    val streakDays: Int,
    val focusScore: Int,
    val dailyProgressPct: Float,
    val weeklyProgressPct: Float,
    val completedSessionsCount: Int
)

object FocusSessionManager {

    const val MAX_HOURLY_XP_CAP = 150
    const val MIN_MINUTES_FOR_XP = 3 // Minimum focus duration to prevent exploit loops

    /**
     * Calculates earned XP for a focus session with anti-farming protection.
     * Prevents spamming start/stop to farm XP.
     */
    fun calculateSessionXp(
        actualSeconds: Int,
        targetDurationMinutes: Int,
        isCompleted: Boolean,
        hasLinkedTask: Boolean,
        recentSessionsInPastHour: List<FocusSessionEntity> = emptyList()
    ): Int {
        val actualMinutes = actualSeconds / 60

        // Anti-farming check 1: prematurely aborted or too short
        if (actualMinutes < MIN_MINUTES_FOR_XP) {
            return 0
        }

        // Must achieve at least 70% of planned duration if not flagged complete
        if (!isCompleted && actualMinutes < (targetDurationMinutes * 0.7).toInt()) {
            return 0
        }

        // Base XP: 4 XP per minute focused
        var earnedXp = (actualMinutes * 4).coerceIn(10, 360)

        // Bonus for linked task
        if (hasLinkedTask) {
            earnedXp += 25
        }

        // Anti-farming check 2: Hourly cap enforcement
        val xpInPastHour = recentSessionsInPastHour.sumOf {
            (it.actualSeconds / 60) * 4
        }

        val allowedRemaining = (MAX_HOURLY_XP_CAP - xpInPastHour).coerceAtLeast(0)
        return earnedXp.coerceAtMost(allowedRemaining)
    }

    /**
     * Calculates streak of consecutive days with at least one focus session.
     */
    fun calculateStreak(
        sessions: List<FocusSessionEntity>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Int {
        val activeDates = sessions
            .filter { (it.actualSeconds / 60) >= MIN_MINUTES_FOR_XP }
            .map {
                Instant.ofEpochMilli(it.startedAtMillis).atZone(zoneId).toLocalDate()
            }
            .distinct()
            .sortedDescending()

        if (activeDates.isEmpty()) return 0

        val today = LocalDate.now(zoneId)
        val yesterday = today.minusDays(1)

        val first = activeDates.first()
        if (first != today && first != yesterday) {
            return 0
        }

        var streak = 0
        var checkDate = first
        for (date in activeDates) {
            if (date == checkDate) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    /**
     * Computes focus score (0-100) based on daily goal completion and consistency.
     */
    fun calculateFocusScore(
        todayMinutes: Int,
        dailyTargetMinutes: Int,
        streakDays: Int,
        totalSessionsToday: Int
    ): Int {
        if (dailyTargetMinutes <= 0) return 70

        val targetPct = (todayMinutes.toFloat() / dailyTargetMinutes.toFloat()).coerceIn(0f, 1f)
        var score = (targetPct * 50).toInt() // Up to 50 points from daily target

        // Up to 30 points from streak
        score += (streakDays * 5).coerceAtMost(30)

        // Up to 20 points from session count / stability
        score += (totalSessionsToday * 5).coerceAtMost(20)

        return score.coerceIn(10, 100)
    }

    /**
     * Computes full analytics.
     */
    fun computeAnalytics(
        sessions: List<FocusSessionEntity>,
        dailyTargetMinutes: Int = 120,
        weeklyTargetMinutes: Int = 600,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): FocusAnalytics {
        val today = LocalDate.now(zoneId)
        val startOfDay = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val validSessions = sessions.filter { it.actualSeconds > 0 }

        val todayMinutes = validSessions
            .filter { it.startedAtMillis >= startOfDay }
            .sumOf { it.actualSeconds / 60 }

        val weekMinutes = validSessions
            .filter { it.startedAtMillis >= startOfWeek }
            .sumOf { it.actualSeconds / 60 }

        val allTimeMinutes = validSessions.sumOf { it.actualSeconds / 60 }

        val streak = calculateStreak(validSessions, zoneId)
        val completedToday = validSessions.count { it.startedAtMillis >= startOfDay && it.isCompleted }
        val score = calculateFocusScore(todayMinutes, dailyTargetMinutes, streak, completedToday)

        val dailyPct = if (dailyTargetMinutes > 0) (todayMinutes.toFloat() / dailyTargetMinutes.toFloat()).coerceIn(0f, 1f) else 0f
        val weeklyPct = if (weeklyTargetMinutes > 0) (weekMinutes.toFloat() / weeklyTargetMinutes.toFloat()).coerceIn(0f, 1f) else 0f

        return FocusAnalytics(
            todayMinutes = todayMinutes,
            weekMinutes = weekMinutes,
            allTimeMinutes = allTimeMinutes,
            streakDays = streak,
            focusScore = score,
            dailyProgressPct = dailyPct,
            weeklyProgressPct = weeklyPct,
            completedSessionsCount = validSessions.count { it.isCompleted }
        )
    }
}
