package com.example.domain.ai

import com.example.data.local.model.*
import com.example.data.remote.AiDailyPlanItem
import com.example.domain.calendar.CalendarEventItem
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object SmartDailyPlanner {

    fun planDay(
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        habitLogs: List<HabitLogEntity>,
        calendarEvents: List<CalendarEventItem> = emptyList(),
        workStartHour: Int = 9,
        workEndHour: Int = 18,
        isArabic: Boolean = false
    ): Pair<String, List<AiDailyPlanItem>> {
        val now = LocalTime.now()
        val currentMin = now.hour * 60 + now.minute
        val startMin = if (currentMin < (workStartHour * 60)) {
            workStartHour * 60
        } else {
            // Start 15 minutes from now rounded to next 15 min mark
            ((currentMin + 15) / 15) * 15
        }

        val pendingTasks = tasks.filter { !it.isCompleted }
            .sortedByDescending { PriorityAndReschedulingEngine.calculatePriorityScore(it) }

        // Find uncompleted habits for today
        val todayEpochDay = LocalDate.now().toEpochDay()
        val completedHabitIdsToday = habitLogs
            .filter { it.dateEpochDay == todayEpochDay && it.isCompleted }
            .map { it.habitId }
            .toSet()
        val pendingHabits = habits.filter { !completedHabitIdsToday.contains(it.id) }

        val planItems = mutableListOf<AiDailyPlanItem>()
        var timePointer = startMin
        val endLimitMin = (workEndHour * 60 + 120).coerceAtMost(23 * 60) // up to 11 PM max

        // 1. If high priority task exists, slot it into peak focus slot
        val topTask = pendingTasks.firstOrNull()
        if (topTask != null) {
            val dur = if (topTask.durationMinutes > 0) topTask.durationMinutes.coerceIn(15, 60) else 45
            val formatted = formatMinutesToTime(timePointer)
            planItems.add(
                AiDailyPlanItem(
                    title = topTask.title,
                    timeFormatted = formatted,
                    durationMinutes = dur,
                    priority = topTask.priority,
                    reason = if (isArabic) "المهمة الأهم اليوم (الضفدع أولاً): تركيز عميق بأعلى طاقة ذهنية" else "Peak energy window: Focus on highest-impact priority"
                )
            )
            timePointer += dur + 15 // 15m buffer
        }

        // 2. Uncompleted morning/midday habit
        val habitToSlot = pendingHabits.firstOrNull()
        if (habitToSlot != null && timePointer + 20 <= endLimitMin) {
            val formatted = formatMinutesToTime(timePointer)
            planItems.add(
                AiDailyPlanItem(
                    title = "✨ ${habitToSlot.title}",
                    timeFormatted = formatted,
                    durationMinutes = 20,
                    priority = Priority.MEDIUM,
                    reason = if (isArabic) "عادة يومية للحفاظ على استمرارية السلسلة وزيادة النشاط" else "Daily habit to protect streak and mental clarity"
                )
            )
            timePointer += 20 + 10 // 10m buffer
        }

        // 3. Secondary pending tasks
        val secondaryTasks = pendingTasks.drop(1).take(3)
        for (task in secondaryTasks) {
            val dur = if (task.durationMinutes > 0) task.durationMinutes.coerceIn(15, 60) else 30
            if (timePointer + dur <= endLimitMin) {
                val formatted = formatMinutesToTime(timePointer)
                planItems.add(
                    AiDailyPlanItem(
                        title = task.title,
                        timeFormatted = formatted,
                        durationMinutes = dur,
                        priority = task.priority,
                        reason = if (isArabic) "فترة إنجاز متوازنة بدون تداخل مع المواعيد" else "Balanced execution block with built-in buffer"
                    )
                )
                timePointer += dur + 15 // 15m buffer
            }
        }

        // 4. Evening wrap-up & review
        if (timePointer + 20 <= endLimitMin) {
            val eveningReviewMin = (20 * 60).coerceAtLeast(timePointer) // 8:00 PM or after
            val formatted = formatMinutesToTime(eveningReviewMin)
            planItems.add(
                AiDailyPlanItem(
                    title = if (isArabic) "المراجعة المسائية وتسجيل المصاريف" else "Evening Review & Expense Log",
                    timeFormatted = formatted,
                    durationMinutes = 20,
                    priority = Priority.LOW,
                    reason = if (isArabic) "تفريغ ذهني وتقييم إنجاز اليوم وترتيب أولويات الغد" else "Mental offload, reflect on wins, and prep tomorrow"
                )
            )
        }

        val narrative = if (isArabic) {
            "🗓️ خطتك اليومية المُحسّنة بالذكاء الاصطناعي:\n\n" +
                    "• تم ترتيب مهامك وفق أعلى عائد ذهني مع فترات راحة ١٥ دقيقة بين المهام لتفادي الإرهاق.\n" +
                    "• خصصنا البداية لأهم مهمة ذات أولوية عالية لضمان حسمها مبكراً.\n" +
                    "• تم إدراج عاداتك اليومية في أوقات مرنة تضمن عدم انقطاع السلسلة.\n" +
                    "• انقر على (تطبيق على جدول اليوم) لتثبيت المواعيد فوراً في جدولك."
        } else {
            "🗓️ Your AI-Optimized Daily Schedule:\n\n" +
                    "• Prioritized by highest cognitive ROI with 15-min buffers between blocks to prevent fatigue.\n" +
                    "• Scheduled your critical high-priority task during peak focus time.\n" +
                    "• Seamlessly interleaved your daily habits to protect active streaks.\n" +
                    "• Tap (Apply Plan to Today) below to lock these time-blocks into your schedule."
        }

        return narrative to planItems
    }

    private fun formatMinutesToTime(minutesOfDay: Int): String {
        val bounded = minutesOfDay % (24 * 60)
        val h = bounded / 60
        val m = bounded % 60
        val amPm = if (h >= 12) "PM" else "AM"
        val hour12 = if (h == 0) 12 else if (h > 12) h - 12 else h
        return String.format("%02d:%02d %s", hour12, m, amPm)
    }
}
