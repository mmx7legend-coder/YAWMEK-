package com.example.domain.ai

import com.example.data.local.model.Priority
import com.example.data.local.model.TaskEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object PriorityAndReschedulingEngine {

    fun calculatePriorityScore(
        task: TaskEntity,
        activeGoalTitles: List<String> = emptyList(),
        nowMillis: Long = System.currentTimeMillis()
    ): Double {
        var score = 0.0

        // 1. Priority Base
        when (task.priority) {
            Priority.HIGH -> score += 35.0
            Priority.MEDIUM -> score += 20.0
            Priority.LOW -> score += 10.0
        }

        // 2. Urgency & Deadlines
        val dueDate = task.dueDateMillis
        if (dueDate != null) {
            val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val todayEnd = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

            if (dueDate < todayStart) {
                // Overdue penalty / urgency boost
                score += 50.0
            } else if (dueDate in todayStart..todayEnd) {
                score += 30.0

                // Proximity to due time
                if (task.dueTimeMinutes != null) {
                    val nowMinutes = LocalTime.now().hour * 60 + LocalTime.now().minute
                    val diff = task.dueTimeMinutes - nowMinutes
                    if (diff in 0..120) {
                        score += 25.0 // Due within 2 hours
                    } else if (diff < 0) {
                        score += 40.0 // Past due time today
                    }
                }
            } else {
                // Future task
                score += 5.0
            }
        }

        // 3. Goal Relevance
        val matchesGoal = activeGoalTitles.any { goalTitle ->
            task.title.contains(goalTitle, ignoreCase = true) ||
            task.description.contains(goalTitle, ignoreCase = true) ||
            goalTitle.contains(task.title, ignoreCase = true)
        }
        if (matchesGoal) {
            score += 20.0
        }

        // 4. Quick Win Boost (if duration <= 20 min, easier to knock out)
        if (task.durationMinutes in 1..20) {
            score += 8.0
        }

        return score
    }

    fun assessScheduleHealth(
        pendingTasks: List<TaskEntity>,
        availableMinutesToday: Int
    ): Pair<ScheduleHealth, String> {
        val totalNeededMinutes = pendingTasks.sumOf { if (it.durationMinutes > 0) it.durationMinutes else 30 }

        return when {
            totalNeededMinutes > availableMinutesToday + 60 -> {
                val excess = totalNeededMinutes - availableMinutesToday
                ScheduleHealth.OVERLOADED to "Schedule is overloaded by $excess min. Risk of burnout is high; consider deferring 2-3 non-critical tasks."
            }
            totalNeededMinutes > availableMinutesToday -> {
                ScheduleHealth.TIGHT to "Schedule is very tight with minimal buffer. Complete high-priority items first."
            }
            else -> {
                ScheduleHealth.BALANCED to "Schedule is balanced with healthy buffer time for deep work."
            }
        }
    }

    fun generateIntelligentReschedule(
        tasks: List<TaskEntity>,
        workStartHour: Int = 9,
        workEndHour: Int = 18,
        isArabic: Boolean = false
    ): RescheduleProposal {
        val today = LocalDate.now()
        val todayStartMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val tomorrow = today.plusDays(1)
        val tomorrowStartMillis = tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dayAfter = today.plusDays(2)
        val dayAfterStartMillis = dayAfter.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val pending = tasks.filter { !it.isCompleted }

        // Overdue tasks = dueDate in the past OR (dueDate is today but dueTime was earlier and missed)
        val nowMin = LocalTime.now().hour * 60 + LocalTime.now().minute
        val overdueTasks = pending.filter { task ->
            val due = task.dueDateMillis
            if (due != null && due < todayStartMillis) {
                true
            } else if (due != null && due <= todayStartMillis + 86400000L && task.dueTimeMinutes != null && task.dueTimeMinutes < nowMin - 30) {
                true
            } else {
                false
            }
        }.sortedByDescending { calculatePriorityScore(it) }

        if (overdueTasks.isEmpty()) {
            val emptyMsg = if (isArabic) {
                "رائع! لا توجد مهام متأخرة تحتاج إلى إعادة جدولة حالياً. جميع مهامك في مسارها الصحيح. ✨"
            } else {
                "Awesome! No overdue tasks detected right now. Your schedule is currently on track. ✨"
            }
            return RescheduleProposal(
                items = emptyList(),
                explanation = emptyMsg,
                totalOverdueCount = 0,
                resolvedTodayCount = 0,
                scheduledTomorrowCount = 0
            )
        }

        val items = mutableListOf<TaskRescheduleItem>()
        var todayRemainingSlots = if (nowMin < (workEndHour * 60 - 45)) 1 else 0
        var tomorrowSlotMin = workStartHour * 60 + 30
        var dayAfterSlotMin = workStartHour * 60 + 30

        var resolvedTodayCount = 0
        var scheduledTomorrowCount = 0

        for (task in overdueTasks) {
            val duration = if (task.durationMinutes > 0) task.durationMinutes else 30

            if (task.priority == Priority.HIGH && todayRemainingSlots > 0) {
                // Squeeze into today's focus window
                val proposedMin = (nowMin + 30).coerceIn(workStartHour * 60, workEndHour * 60)
                items.add(
                    TaskRescheduleItem(
                        task = task,
                        newDueDateMillis = todayStartMillis,
                        newDueTimeMinutes = proposedMin,
                        reason = if (isArabic) "أولوية عالية: تم حجزها لفترة المساء اليوم" else "High Priority: Slotted for this evening's focus window"
                    )
                )
                todayRemainingSlots--
                resolvedTodayCount++
            } else if (tomorrowSlotMin + duration <= workEndHour * 60) {
                // Slot into tomorrow
                items.add(
                    TaskRescheduleItem(
                        task = task,
                        newDueDateMillis = tomorrowStartMillis,
                        newDueTimeMinutes = tomorrowSlotMin,
                        reason = if (isArabic) "تمت إعادة جدولتها لصباح الغد بفترة تركيز صافية" else "Rescheduled to tomorrow with a fresh focus block"
                    )
                )
                tomorrowSlotMin += duration + 15 // 15 min buffer
                scheduledTomorrowCount++
            } else {
                // Distribute to day after tomorrow to prevent tomorrow overload
                items.add(
                    TaskRescheduleItem(
                        task = task,
                        newDueDateMillis = dayAfterStartMillis,
                        newDueTimeMinutes = dayAfterSlotMin,
                        reason = if (isArabic) "تم ترحيلها لليوم التالي لتفادي الضغط وتراكم الأعباء" else "Distributed to day after tomorrow to prevent schedule congestion"
                    )
                )
                dayAfterSlotMin += duration + 15
            }
        }

        val explanation = if (isArabic) {
            "تمت إعادة التوزيع الذكي لـ ${overdueTasks.size} مهمة متأخرة:\n" +
                    "• تم إنقاذ $resolvedTodayCount مهمة ذات أولوية قصوى لفترة اليوم.\n" +
                    "• تمت جدولة $scheduledTomorrowCount مهمة للغد بفترات راحة ١٥ دقيقة.\n" +
                    "• تم توزيع باقي المهام لتجنب الإرهاق والحفاظ على توازن طاقتك."
        } else {
            "Intelligently redistributed ${overdueTasks.size} overdue tasks:\n" +
                    "• $resolvedTodayCount urgent task(s) retained for today's remaining focus window.\n" +
                    "• $scheduledTomorrowCount task(s) scheduled for tomorrow with 15-min buffers.\n" +
                    "• Remaining distributed smoothly across upcoming days to avoid overload."
        }

        return RescheduleProposal(
            items = items,
            explanation = explanation,
            totalOverdueCount = overdueTasks.size,
            resolvedTodayCount = resolvedTodayCount,
            scheduledTomorrowCount = scheduledTomorrowCount
        )
    }
}
