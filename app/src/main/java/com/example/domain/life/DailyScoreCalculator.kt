package com.example.domain.life

import com.example.data.local.model.*
import java.time.LocalDate
import java.time.ZoneId

object DailyScoreCalculator {

    fun calculateDailyScore(
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        habitLogs: List<HabitLogEntity>,
        focusSessions: List<FocusSessionEntity>,
        dailyFocusTargetMinutes: Int = 60,
        isArabic: Boolean = false
    ): DailyScoreResult {
        val today = LocalDate.now()
        val todayEpochDay = today.toEpochDay()
        val todayStartMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayEndMillis = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        // 1. Task Completion Points (Max 35)
        val todayTasks = tasks.filter {
            (it.dueDateMillis != null && it.dueDateMillis in todayStartMillis..todayEndMillis) ||
            (it.completedAtMillis != null && it.completedAtMillis in todayStartMillis..todayEndMillis)
        }
        val taskPoints = if (todayTasks.isNotEmpty()) {
            val completedCount = todayTasks.count { it.isCompleted }
            ((completedCount.toDouble() / todayTasks.size) * 35.0).toInt().coerceIn(0, 35)
        } else {
            // If user has no specific tasks today, base on general pending status
            val pending = tasks.filter { !it.isCompleted }
            if (pending.isEmpty() && tasks.isNotEmpty()) 35 else 20
        }

        // 2. Habit Consistency Points Today (Max 25)
        val habitPoints = if (habits.isNotEmpty()) {
            val completedTodayHabits = habitLogs.count { it.dateEpochDay == todayEpochDay && it.isCompleted }
            ((completedTodayHabits.toDouble() / habits.size) * 25.0).toInt().coerceIn(0, 25)
        } else {
            15 // Neutral baseline if no habits configured yet
        }

        // 3. Focus Minutes Points (Max 25)
        val todayFocusSeconds = focusSessions
            .filter { it.startedAtMillis in todayStartMillis..todayEndMillis }
            .sumOf { it.actualSeconds }
        val todayFocusMinutes = todayFocusSeconds / 60
        val target = if (dailyFocusTargetMinutes > 0) dailyFocusTargetMinutes else 60
        val focusPoints = ((todayFocusMinutes.toDouble() / target) * 25.0).toInt().coerceIn(0, 25)

        // 4. Schedule Discipline Points (Max 15)
        // Deduct if there are overdue tasks lingering
        val overdueTasksCount = tasks.count {
            !it.isCompleted && it.dueDateMillis != null && it.dueDateMillis < todayStartMillis
        }
        val disciplinePoints = (15 - (overdueTasksCount * 3)).coerceIn(0, 15)

        val totalScore = (taskPoints + habitPoints + focusPoints + disciplinePoints).coerceIn(0, 100)

        val tier = when (totalScore) {
            in 90..100 -> DailyScoreTier.MASTERY
            in 75..89 -> DailyScoreTier.MOMENTUM
            in 60..74 -> DailyScoreTier.FOCUSED
            in 40..59 -> DailyScoreTier.BUILDING
            else -> DailyScoreTier.RESTART
        }

        val feedbackEn = when (tier) {
            DailyScoreTier.MASTERY -> "Elite execution! You are operating at peak productivity with balanced focus and discipline."
            DailyScoreTier.MOMENTUM -> "Strong momentum. You have secured your core priorities and consistent habits."
            DailyScoreTier.FOCUSED -> "Solid progress. Complete your next scheduled block to break into top tier."
            DailyScoreTier.BUILDING -> "Building your foundation. Knock out one quick task to ignite your momentum."
            DailyScoreTier.RESTART -> "Fresh start. Pick one high-impact action or activate Rescue Mode to regain control."
        }

        val feedbackAr = when (tier) {
            DailyScoreTier.MASTERY -> "أداء استثنائي! تعمل بأعلى مستويات الكفاءة والتركيز والانضباط اليومي."
            DailyScoreTier.MOMENTUM -> "زخم ممتاز. أنجزت أولوياتك الرئيسية وحافظت على عاداتك."
            DailyScoreTier.FOCUSED -> "تقدم رائع وثابت. نفّذ مهمتك القادمة للوصول إلى مرتبة الإتقان."
            DailyScoreTier.BUILDING -> "في مرحلة البناء. أنجز مهمة سريعة واحدة لإشعال حماسك."
            DailyScoreTier.RESTART -> "بداية جديدة. اختر خطوة واحدة واضحة أو فعّل وضع الإنقاذ لتنظيم يومك."
        }

        return DailyScoreResult(
            totalScore = totalScore,
            tier = tier,
            taskCompletionPoints = taskPoints,
            habitConsistencyPoints = habitPoints,
            focusMinutesPoints = focusPoints,
            scheduleDisciplinePoints = disciplinePoints,
            feedbackEnglish = feedbackEn,
            feedbackArabic = feedbackAr
        )
    }
}
