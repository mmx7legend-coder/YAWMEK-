package com.example.domain.life

import com.example.data.local.model.*
import com.example.domain.calendar.CalendarEventItem
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object AdaptiveDayEngine {

    // Snapshot stack for Undo support
    private val planHistory = mutableListOf<AdaptiveDayPlan>()

    fun generateAdaptivePlan(
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        habitLogs: List<HabitLogEntity>,
        calendarEvents: List<CalendarEventItem> = emptyList(),
        activeGoalTitles: List<String> = emptyList(),
        workStartHour: Int = 9,
        workEndHour: Int = 18,
        nowMinuteOfDay: Int = LocalTime.now().hour * 60 + LocalTime.now().minute,
        isRescueMode: Boolean = false,
        isArabic: Boolean = false
    ): AdaptiveDayPlan {
        val today = LocalDate.now()
        val todayEpochDay = today.toEpochDay()
        val todayStartMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayEndMillis = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        val workStartMin = workStartHour * 60
        val workEndMin = workEndHour * 60

        // Current planning pointer starts at max(now, workStartHour)
        val planningStartMin = nowMinuteOfDay.coerceIn(workStartMin, workEndMin)
        val totalAvailableMinutesToday = (workEndMin - planningStartMin).coerceAtLeast(0)

        // Filter today's pending tasks or overdue tasks
        val pendingTasks = tasks.filter { !it.isCompleted && (
            it.dueDateMillis == null ||
            it.dueDateMillis <= todayEndMillis
        ) }

        // Rank tasks using the multi-factor Priority Engine
        val rankedTasks = PriorityEngine.rankTasks(
            tasks = pendingTasks,
            activeGoalTitles = activeGoalTitles,
            availableMinutes = totalAvailableMinutesToday,
            nowMinuteOfDay = nowMinuteOfDay
        ).map { it.first }

        // Check habits for today
        val completedHabitIdsToday = habitLogs
            .filter { it.dateEpochDay == todayEpochDay && it.isCompleted }
            .map { it.habitId }
            .toSet()

        val pendingHabits = habits.filter { !completedHabitIdsToday.contains(it.id) }

        // Collect today's external calendar busy blocks
        val todayCalEvents = calendarEvents.filter {
            it.startMillis in todayStartMillis..todayEndMillis
        }.sortedBy { it.startMillis }

        val plannedItems = mutableListOf<PlannedDayItem>()
        var currentSlotMin = planningStartMin

        // Add calendar events as immovable anchors
        val calendarBlocks = todayCalEvents.map { cal ->
            val eventCal = java.util.Calendar.getInstance().apply { timeInMillis = cal.startMillis }
            val startMin = eventCal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + eventCal.get(java.util.Calendar.MINUTE)
            PlannedDayItem(
                id = "cal_${cal.id}",
                calendarEvent = cal,
                title = cal.title,
                startMinuteOfDay = startMin,
                durationMinutes = cal.durationMinutes.coerceAtLeast(15),
                itemType = PlannedItemType.CALENDAR_EVENT,
                priority = Priority.HIGH,
                tier = RescueTier.MUST_DO,
                isCompleted = false,
                reasonEnglish = "Fixed calendar appointment",
                reasonArabic = "موعد تقويم ثابت"
            )
        }

        // Triage: In Rescue Mode, only take Must-Do and high Important tasks
        val tasksToSchedule = if (isRescueMode) {
            rankedTasks.filter { task ->
                task.priority == Priority.HIGH ||
                (task.dueDateMillis != null && task.dueDateMillis < todayStartMillis) ||
                activeGoalTitles.any { task.title.contains(it, ignoreCase = true) }
            }.take(3)
        } else {
            rankedTasks
        }

        var totalScheduledMinutes = 0

        for (task in tasksToSchedule) {
            val taskDuration = if (task.durationMinutes > 0) task.durationMinutes else 30

            // Check if currentSlotMin collides with any calendar event
            val nextConflict = calendarBlocks.firstOrNull { cal ->
                currentSlotMin < cal.endMinuteOfDay && (currentSlotMin + taskDuration) > cal.startMinuteOfDay
            }

            if (nextConflict != null) {
                // Advance pointer past the calendar event + 10 min buffer
                currentSlotMin = (nextConflict.endMinuteOfDay + 10).coerceAtMost(workEndMin)
            }

            if (currentSlotMin + taskDuration <= workEndMin) {
                val tier = when {
                    task.priority == Priority.HIGH || (task.dueDateMillis != null && task.dueDateMillis < todayStartMillis) -> RescueTier.MUST_DO
                    task.priority == Priority.MEDIUM -> RescueTier.IMPORTANT
                    else -> RescueTier.OPTIONAL
                }

                plannedItems.add(
                    PlannedDayItem(
                        id = "task_${task.id}",
                        task = task,
                        title = task.title,
                        startMinuteOfDay = currentSlotMin,
                        durationMinutes = taskDuration,
                        itemType = PlannedItemType.TASK,
                        priority = task.priority,
                        tier = tier,
                        isCompleted = task.isCompleted,
                        reasonEnglish = if (tier == RescueTier.MUST_DO) "High impact on today's core goals" else "Optimal productivity slot",
                        reasonArabic = if (tier == RescueTier.MUST_DO) "أولوية قصوى لحماية أهداف اليوم" else "فترة إنتاجية مثالية"
                    )
                )
                currentSlotMin += taskDuration + 10 // 10 min buffer between tasks
                totalScheduledMinutes += taskDuration
            } else {
                // Won't fit cleanly in remaining work hours without overloading
                break
            }
        }

        // Slot pending habits if there is room
        for (habit in pendingHabits.take(2)) {
            val habitDuration = 15
            if (currentSlotMin + habitDuration <= workEndMin) {
                plannedItems.add(
                    PlannedDayItem(
                        id = "habit_${habit.id}",
                        habit = habit,
                        title = habit.title,
                        startMinuteOfDay = currentSlotMin,
                        durationMinutes = habitDuration,
                        itemType = PlannedItemType.HABIT,
                        priority = Priority.MEDIUM,
                        tier = RescueTier.IMPORTANT,
                        isCompleted = false,
                        reasonEnglish = "Habit momentum anchor",
                        reasonArabic = "تثبيت عادة يومية"
                    )
                )
                currentSlotMin += habitDuration + 5
                totalScheduledMinutes += habitDuration
            }
        }

        // Merge calendar blocks and sort timeline chronologically
        val allMerged = (plannedItems + calendarBlocks).sortedBy { it.startMinuteOfDay }

        val remainingFreeMinutes = (totalAvailableMinutesToday - totalScheduledMinutes).coerceAtLeast(0)
        val isOverloaded = tasksToSchedule.size < rankedTasks.size && remainingFreeMinutes < 30

        val explanationEn = when {
            isRescueMode -> "Rescue plan active: prioritized ${plannedItems.size} high-impact items to protect core commitments."
            isOverloaded -> "Schedule is tight ($remainingFreeMinutes min buffer). Consider activating Rescue Mode to protect key goals."
            else -> "Day plan optimized and balanced with healthy focus blocks and transition buffers."
        }

        val explanationAr = when {
            isRescueMode -> "وضع الإنقاذ مفعّل: تم حصر الجدول في ${plannedItems.size} مهام حاسمة لحماية الأهداف الأساسية."
            isOverloaded -> "الجدول مضغوط (الفراغ المتبقي $remainingFreeMinutes دقيقة). نوصي بتفعيل وضع الإنقاذ لتفادي الإرهاق."
            else -> "تم تحسين وتوزيع يومك بمرونة مع فترات تركيز وفواصل زمنية مريحة."
        }

        val plan = AdaptiveDayPlan(
            plannedItems = allMerged,
            totalScheduledMinutes = totalScheduledMinutes,
            remainingFreeMinutesToday = remainingFreeMinutes,
            isOverloaded = isOverloaded,
            delayMinutesDetected = (nowMinuteOfDay - workStartMin).coerceAtLeast(0),
            isRescueModeActive = isRescueMode,
            canUndo = planHistory.isNotEmpty(),
            explanationEnglish = explanationEn,
            explanationArabic = explanationAr
        )

        return plan
    }

    fun recalculateRemainingDay(
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        habitLogs: List<HabitLogEntity>,
        calendarEvents: List<CalendarEventItem> = emptyList(),
        activeGoalTitles: List<String> = emptyList(),
        workStartHour: Int = 9,
        workEndHour: Int = 18,
        nowMinuteOfDay: Int = LocalTime.now().hour * 60 + LocalTime.now().minute,
        currentPlan: AdaptiveDayPlan,
        isRescueMode: Boolean = false,
        isArabic: Boolean = false
    ): AdaptiveDayPlan {
        // Push snapshot to history for Undo capability
        planHistory.add(currentPlan)
        if (planHistory.size > 5) {
            planHistory.removeAt(0)
        }

        return generateAdaptivePlan(
            tasks = tasks,
            habits = habits,
            habitLogs = habitLogs,
            calendarEvents = calendarEvents,
            activeGoalTitles = activeGoalTitles,
            workStartHour = workStartHour,
            workEndHour = workEndHour,
            nowMinuteOfDay = nowMinuteOfDay,
            isRescueMode = isRescueMode,
            isArabic = isArabic
        )
    }

    fun undoPlanRecalculation(): AdaptiveDayPlan? {
        if (planHistory.isEmpty()) return null
        return planHistory.removeAt(planHistory.size - 1)
    }

    fun clearHistory() {
        planHistory.clear()
    }
}
