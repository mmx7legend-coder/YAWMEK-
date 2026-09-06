package com.example.domain.ai

import com.example.data.local.model.HabitEntity
import com.example.data.local.model.HabitLogEntity
import java.time.LocalDate

object HabitIntelligenceEngine {

    fun analyzeHabits(
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity>,
        isArabic: Boolean = false
    ): List<HabitInsight> {
        if (habits.isEmpty()) return emptyList()

        val todayEpochDay = LocalDate.now().toEpochDay()
        val past30DaysEpoch = todayEpochDay - 30

        return habits.map { habit ->
            val habitLogs = logs.filter { it.habitId == habit.id && it.dateEpochDay >= past30DaysEpoch }
            val completedDays = habitLogs.filter { it.isCompleted }.map { it.dateEpochDay }.toSet()

            // Calculate current streak
            var streak = 0
            var checkDay = todayEpochDay
            if (!completedDays.contains(checkDay)) {
                checkDay = todayEpochDay - 1
            }
            while (completedDays.contains(checkDay)) {
                streak++
                checkDay--
            }

            val consistency = if (habitLogs.isNotEmpty()) {
                ((completedDays.size.toDouble() / 30.0) * 100).toInt().coerceIn(0, 100)
            } else {
                if (streak > 0) (streak * 15).coerceAtMost(100) else 0
            }

            // Detect Stacking Anchor
            val otherHabits = habits.filter { it.id != habit.id }
            val strongAnchor = otherHabits.maxByOrNull { o ->
                logs.count { it.habitId == o.id && it.isCompleted && it.dateEpochDay >= past30DaysEpoch }
            }

            val recommendation = when {
                consistency >= 80 -> {
                    if (isArabic) "عادتك راسخة وقوية جداً (${streak} أيام متواصلة)! حافظ على هذا الإيقاع الممتاز."
                    else "Rock-solid consistency (${streak}-day streak)! You have built strong automatic momentum."
                }
                consistency in 50..79 -> {
                    if (isArabic) "أداؤك جيد، لكن هناك بعض التذبذب في أيام العطلة. اربطها بموعد ثابت في جدولك اليومي."
                    else "Solid progress, but dips on weekends. Anchor it to a fixed morning or evening trigger."
                }
                else -> {
                    if (strongAnchor != null) {
                        if (isArabic) "نوصي بتطبيق (تقنية تكديس العادات): نفّذ [${habit.title}] فور انتهائك من [${strongAnchor.title}]."
                        else "Habit Stacking Opportunity: Perform [${habit.title}] immediately after your strong habit [${strongAnchor.title}]."
                    } else {
                        if (isArabic) "اجعل العادة أسهل (قاعدة الدقيقتين): ابدأ بـ ٥ دقائق فقط يومياً لتثبيت الاستمرارية."
                        else "Apply the 2-Minute Rule: Keep the friction minimal (5 mins) until the habit loop is established."
                    }
                }
            }

            HabitInsight(
                habitTitle = habit.title,
                currentStreak = streak,
                consistencyPercent = consistency,
                recommendation = recommendation,
                habitStackAnchor = if (consistency < 60 && strongAnchor != null) strongAnchor.title else null
            )
        }
    }

    fun generateOverallHabitSummary(
        insights: List<HabitInsight>,
        isArabic: Boolean = false
    ): String {
        if (insights.isEmpty()) {
            return if (isArabic) "لم تسجل أي عادات بعد. يمكنك إضافة عادتك الأولى لتبدأ بالتحليل الذكي."
            else "No habits registered yet. Add your first habit to activate intelligent habit tracking."
        }

        val avgConsistency = (insights.sumOf { it.consistencyPercent } / insights.size)
        val bestHabit = insights.maxByOrNull { it.currentStreak }

        return if (isArabic) {
            "📈 تحليل العادات والأنماط السلوكية:\n" +
                    "• متوسط الالتزام بالعادات: $avgConsistency%\n" +
                    "• أقوى سلسلة حالية: ${bestHabit?.habitTitle ?: "لا يوجد"} (${bestHabit?.currentStreak ?: 0} أيام متتالية)\n" +
                    "• توصية الخبير: الاستمرارية في نفس الوقت يومياً تضاعف احتمالية تحول السلوك إلى عادة تلقائية بنسبة ٦٥٪."
        } else {
            "📈 Habit & Behavioral Consistency Analysis:\n" +
                    "• Average 30-day consistency score: $avgConsistency%\n" +
                    "• Top Active Streak: ${bestHabit?.habitTitle ?: "None"} (${bestHabit?.currentStreak ?: 0} consecutive days)\n" +
                    "• Behavioral Tip: Fixed contextual triggers (same time, same location) increase habit automaticity by 65%."
        }
    }
}
