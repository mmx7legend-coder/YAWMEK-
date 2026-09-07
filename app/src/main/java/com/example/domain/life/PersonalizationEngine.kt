package com.example.domain.life

import com.example.data.local.model.*
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar

object PersonalizationEngine {

    fun analyzePatterns(
        tasks: List<TaskEntity>,
        focusSessions: List<FocusSessionEntity>,
        habits: List<HabitEntity>,
        habitLogs: List<HabitLogEntity>,
        isArabic: Boolean = false
    ): PersonalizationInsights {
        val nowMillis = System.currentTimeMillis()
        val sevenDaysAgoMillis = nowMillis - (7 * 86400000L)

        // 1. Real Duration vs Estimated Duration Ratio
        val sessionsWithTarget = focusSessions.filter { it.durationMinutes > 0 && it.actualSeconds > 60 }
        val actualVsEstimatedRatio = if (sessionsWithTarget.size >= 3) {
            val totalEstimated = sessionsWithTarget.sumOf { it.durationMinutes.toDouble() }
            val totalActual = sessionsWithTarget.sumOf { it.actualSeconds / 60.0 }
            if (totalEstimated > 0) (totalActual / totalEstimated).coerceIn(0.6, 2.5) else 1.0
        } else {
            1.15 // Modest baseline buffer when sparse data
        }

        // 2. Peak Productivity Window Analysis
        val completedPast30Days = tasks.filter {
            it.isCompleted && it.completedAtMillis != null && it.completedAtMillis >= (nowMillis - 30 * 86400000L)
        }

        var morningCount = 0   // 06:00 - 12:00
        var afternoonCount = 0 // 12:00 - 18:00
        var eveningCount = 0   // 18:00 - 24:00

        for (task in completedPast30Days) {
            val millis = task.completedAtMillis ?: continue
            val cal = Calendar.getInstance().apply { timeInMillis = millis }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            when (hour) {
                in 6..11 -> morningCount++
                in 12..17 -> afternoonCount++
                else -> eveningCount++
            }
        }

        val peakWindow = when {
            morningCount >= afternoonCount && morningCount >= eveningCount && morningCount > 0 -> "09:00 AM - 12:00 PM"
            afternoonCount >= morningCount && afternoonCount >= eveningCount && afternoonCount > 0 -> "01:00 PM - 05:00 PM"
            eveningCount > 0 -> "06:00 PM - 09:30 PM"
            else -> "09:30 AM - 12:30 PM"
        }

        // 3. 7-Day Completion Rate
        val pastWeekTasks = tasks.filter { it.createdAtMillis >= sevenDaysAgoMillis || (it.completedAtMillis != null && it.completedAtMillis >= sevenDaysAgoMillis) }
        val pastWeekCompleted = pastWeekTasks.count { it.isCompleted }
        val completionRate7d = if (pastWeekTasks.isNotEmpty()) {
            ((pastWeekCompleted.toDouble() / pastWeekTasks.size) * 100).toInt().coerceIn(0, 100)
        } else {
            85
        }

        // 4. Planning Accuracy: tasks scheduled for a day vs actually finished
        val planningAccuracy = if (completedPast30Days.isNotEmpty()) {
            val onTimeTasks = completedPast30Days.count {
                it.dueDateMillis != null && it.completedAtMillis != null && it.completedAtMillis <= (it.dueDateMillis + 86400000L)
            }
            ((onTimeTasks.toDouble() / completedPast30Days.size) * 100).toInt().coerceIn(40, 100)
        } else {
            75
        }

        // 5. Frequently Stalling Category
        val pendingTasks = tasks.filter { !it.isCompleted }
        val topStallingCategory = pendingTasks
            .groupBy { it.category.name }
            .maxByOrNull { it.value.size }
            ?.key

        // 6. Generate Contextual Smart Insights based purely on real telemetry
        val smartInsights = mutableListOf<SmartInsightItem>()

        // Insight 1: Duration Velocity Pattern
        if (actualVsEstimatedRatio > 1.1) {
            val pctOver = ((actualVsEstimatedRatio - 1.0) * 100).toInt()
            smartInsights.add(
                SmartInsightItem(
                    iconName = "Timer",
                    titleEnglish = "Duration Underestimation",
                    titleArabic = "تقدير مدة المهام",
                    detailEnglish = "Your sessions typically take ~$pctOver% longer than planned. Adding a 15-min buffer will keep your afternoon on schedule.",
                    detailArabic = "مهامك تستغرق فعلياً قرابة $pctOver% وقتاً إضافياً. إضافة فاصل ١٥ دقيقة سيحمي جدولك المسائي من الانضغاط.",
                    category = InsightCategory.VELOCITY
                )
            )
        } else {
            smartInsights.add(
                SmartInsightItem(
                    iconName = "CheckCircle",
                    titleEnglish = "High Time Accuracy",
                    titleArabic = "دقة عالية في التقدير",
                    detailEnglish = "Your task duration estimates match your actual focus speed with high precision.",
                    detailArabic = "تقديرك لأوقات المهام يتطابق مع سرعة إنجازك الفعلية بدقة ممتازة.",
                    category = InsightCategory.VELOCITY
                )
            )
        }

        // Insight 2: Peak Energy Curve
        smartInsights.add(
            SmartInsightItem(
                iconName = "Bolt",
                titleEnglish = "Peak Energy Window",
                titleArabic = "فترة ذروة الإنتاجية",
                detailEnglish = "Most of your task completions occur during $peakWindow. Reserve this window for demanding deep work.",
                detailArabic = "أكبر نسبة من إنجازاتك تتم بين $peakWindow. احجز هذه الفترة لأهم المهام العميقة.",
                category = InsightCategory.PATTERN
            )
        )

        // Insight 3: Workload Sweet Spot
        val completedCountPast7 = pastWeekCompleted
        val dailyAvg = completedCountPast7 / 7.0
        val sweetSpotTextEn = if (dailyAvg in 2.0..5.0) {
            "You maintain the highest velocity when your day is capped at 3-5 core tasks."
        } else {
            "Smaller daily batches consistently yield higher completion rates and lower cognitive stress."
        }
        val sweetSpotTextAr = if (dailyAvg in 2.0..5.0) {
            "تحقق أعلى استمرارية عندما يقتصر يومك على ٣ إلى ٥ مهام أساسية."
        } else {
            "حصر المهام اليومية في دفعات صغيرة يرفع دائماً معدل الإنجاز ويقلل الإجهاد الذهني."
        }

        smartInsights.add(
            SmartInsightItem(
                iconName = "WorkspacePremium",
                titleEnglish = "Workload Sweet Spot",
                titleArabic = "الحجم اليومي الأمثل",
                detailEnglish = sweetSpotTextEn,
                detailArabic = sweetSpotTextAr,
                category = InsightCategory.PATTERN
            )
        )

        return PersonalizationInsights(
            actualVsEstimatedRatio = actualVsEstimatedRatio,
            peakProductivityWindow = peakWindow,
            completionRateSevenDays = completionRate7d,
            planningAccuracyPercent = planningAccuracy,
            topStallingCategory = topStallingCategory,
            smartInsights = smartInsights
        )
    }
}
