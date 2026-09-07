package com.example.domain.life

import com.example.data.local.model.*
import com.example.domain.calendar.CalendarEventItem
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object LifeEngine {

    fun determineRecommendation(
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        habitLogs: List<HabitLogEntity>,
        goals: List<GoalEntity> = emptyList(),
        milestones: List<MilestoneEntity> = emptyList(),
        calendarEvents: List<CalendarEventItem> = emptyList(),
        workStartHour: Int = 9,
        workEndHour: Int = 18,
        nowMinuteOfDay: Int = LocalTime.now().hour * 60 + LocalTime.now().minute,
        isRescueMode: Boolean = false,
        timeConstraintMinutes: Int? = null,
        isArabic: Boolean = false
    ): LifeRecommendation {
        val nowMillis = System.currentTimeMillis()
        val today = LocalDate.now()
        val todayEpochDay = today.toEpochDay()
        val todayStartMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayEndMillis = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        // Detect next calendar event today
        val nextCalendarEvent = calendarEvents
            .filter { it.startMillis in (nowMillis + 1)..<todayEndMillis }
            .minByOrNull { it.startMillis }

        val minutesUntilNextEvent = if (nextCalendarEvent != null) {
            ((nextCalendarEvent.startMillis - nowMillis) / (60 * 1000)).toInt().coerceAtLeast(1)
        } else {
            120
        }

        val effectiveAvailableMinutes = timeConstraintMinutes ?: minutesUntilNextEvent

        // Active goal titles for alignment scoring
        val activeGoalTitles = goals.filter { !it.isCompleted }.map { it.title }

        val pendingTasks = tasks.filter { !it.isCompleted }

        // Filter by time constraint if user said "I only have X minutes"
        val candidateTasks = if (timeConstraintMinutes != null) {
            val matching = pendingTasks.filter { (if (it.durationMinutes > 0) it.durationMinutes else 30) <= timeConstraintMinutes }
            if (matching.isNotEmpty()) matching else pendingTasks
        } else {
            pendingTasks
        }

        // Check if there are candidate tasks
        if (candidateTasks.isNotEmpty()) {
            val ranked = PriorityEngine.rankTasks(
                tasks = candidateTasks,
                activeGoalTitles = activeGoalTitles,
                availableMinutes = effectiveAvailableMinutes,
                nowMinuteOfDay = nowMinuteOfDay,
                nowMillis = nowMillis
            )

            val topTaskPair = ranked.first()
            val topTask = topTaskPair.first
            val alternatives = ranked.drop(1).take(3).map { it.first }

            val duration = if (topTask.durationMinutes > 0) topTask.durationMinutes else 30
            val isOverdue = topTask.dueDateMillis != null && topTask.dueDateMillis < todayStartMillis
            val isDueSoonToday = topTask.dueDateMillis in todayStartMillis..todayEndMillis && topTask.dueTimeMinutes != null && (topTask.dueTimeMinutes - nowMinuteOfDay) in 0..120

            // Contextual Reason
            val reasonEn = when {
                isOverdue -> "Critical deadline passed. Resolving this will lift cognitive friction and protect your commitments."
                isDueSoonToday -> "Due shortly today at ${formatMinutesToTime(topTask.dueTimeMinutes ?: 0)}. High-leverage execution window."
                topTask.priority == Priority.HIGH -> "Highest priority task aligned with your primary active goals."
                duration <= 20 -> "Quick momentum builder: finish in $duration minutes to unlock deep work focus."
                else -> "Optimal fit for your current $effectiveAvailableMinutes-minute available focus block."
            }

            val reasonAr = when {
                isOverdue -> "مهمة تجاوزت موعدها النهائي. إنجازها الآن سيزيل العبء الذهني ويحمي التزاماتك."
                isDueSoonToday -> "موعد تسليمها قريب اليوم الساعة ${formatMinutesToTime(topTask.dueTimeMinutes ?: 0)}. نافذة تنفيذ مثالية."
                topTask.priority == Priority.HIGH -> "مهمة ذات أولوية قصوى متوافقة تماماً مع أهدافك النشطة الرئيسية."
                duration <= 20 -> "فرصة لإنجاز سريع: مدتها $duration دقيقة لتعزيز طاقتك وزخمك اليومي."
                else -> "الأنسب لفترة الفراغ والتركيز الحالية المتاحة ($effectiveAvailableMinutes دقيقة)."
            }

            // Expected Impact
            val matchingGoal = goals.firstOrNull { g ->
                topTask.title.contains(g.title, ignoreCase = true) || g.title.contains(topTask.title, ignoreCase = true)
            }

            val impactEn = when {
                matchingGoal != null -> "Directly advances goal '${matchingGoal.title}'."
                isOverdue -> "Clears overdue backlog & secures plan integrity."
                topTask.priority == Priority.HIGH -> "Major progress on today's core focus milestone."
                else -> "Builds continuous execution momentum."
            }

            val impactAr = when {
                matchingGoal != null -> "تحرز تقدماً مباشراً في هدف '${matchingGoal.title}'."
                isOverdue -> "تنهي المهام المتأخرة وتحمي سلامة خطتك اليومية."
                topTask.priority == Priority.HIGH -> "إنجاز حاسم في المرحلة الأساسية ليومك."
                else -> "تبني زخماً مستمراً نحو إنجاز بقية جدولك."
            }

            return LifeRecommendation(
                actionType = RecommendationActionType.TASK,
                task = topTask,
                title = topTask.title,
                subtitle = if (isArabic) "المهمة المقترحة التالية" else "Next Recommended Action",
                reasonEnglish = reasonEn,
                reasonArabic = reasonAr,
                estimatedDurationMinutes = duration,
                priority = topTask.priority,
                expectedImpactEnglish = impactEn,
                expectedImpactArabic = impactAr,
                freeTimeMinutesAvailable = effectiveAvailableMinutes,
                alternatives = alternatives,
                nextCalendarEvent = nextCalendarEvent
            )
        }

        // If no pending tasks, check for pending habits
        val past30DaysEpoch = todayEpochDay - 30
        val completedTodayHabitIds = habitLogs
            .filter { it.dateEpochDay == todayEpochDay && it.isCompleted }
            .map { it.habitId }
            .toSet()

        val pendingHabits = habits.filter { !completedTodayHabitIds.contains(it.id) }
        if (pendingHabits.isNotEmpty()) {
            val nextHabit = pendingHabits.first()
            return LifeRecommendation(
                actionType = RecommendationActionType.HABIT,
                habit = nextHabit,
                title = nextHabit.title,
                subtitle = if (isArabic) "تثبيت عادة يومية" else "Habit Consistency Anchor",
                reasonEnglish = "All scheduled tasks are complete! Protect your '${nextHabit.title}' streak.",
                reasonArabic = "أنجزت جميع مهامك المجدولة! حافظ على سلسلة استمراريتك في '${nextHabit.title}'.",
                estimatedDurationMinutes = 15,
                priority = Priority.MEDIUM,
                expectedImpactEnglish = "Protects your habit streak and reinforces your daily identity.",
                expectedImpactArabic = "يحمي استمرارية عادتك ويعزز الانضباط الشخصي.",
                freeTimeMinutesAvailable = effectiveAvailableMinutes,
                nextCalendarEvent = nextCalendarEvent
            )
        }

        // All tasks and habits completed -> Mindful Pause
        return LifeRecommendation(
            actionType = RecommendationActionType.MINDFUL_BREAK,
            title = if (isArabic) "يومك تحت السيطرة تماماً! ✨" else "All Clear! Day Under Control ✨",
            subtitle = if (isArabic) "استراحة ذهنية مستحقة" else "Well-deserved rest",
            reasonEnglish = if (nextCalendarEvent != null) {
                "No pending actions. You have $effectiveAvailableMinutes minutes before '${nextCalendarEvent.title}'."
            } else {
                "You have accomplished all your core commitments. Take time to recharge or review your long-term goals."
            },
            reasonArabic = if (nextCalendarEvent != null) {
                "لا توجد مهام معلقة. لديك $effectiveAvailableMinutes دقيقة قبل موعد '${nextCalendarEvent.title}'."
            } else {
                "أنجزت جميع التزاماتك الأساسية بنجاح. خذ وقتاً للراحة أو مراجعة أهدافك طويلة المدى."
            },
            estimatedDurationMinutes = 0,
            priority = Priority.LOW,
            expectedImpactEnglish = "Restores cognitive energy for sustained long-term productivity.",
            expectedImpactArabic = "يجدد طاقتك الذهنية لاستدامة إنتاجيتك على المدى الطويل.",
            freeTimeMinutesAvailable = effectiveAvailableMinutes,
            canStart = false,
            canComplete = false,
            canSnooze = false,
            canReschedule = false,
            nextCalendarEvent = nextCalendarEvent
        )
    }

    fun evaluateLifeEngineState(
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        habitLogs: List<HabitLogEntity>,
        goals: List<GoalEntity>,
        milestones: List<MilestoneEntity>,
        focusSessions: List<FocusSessionEntity>,
        calendarEvents: List<CalendarEventItem> = emptyList(),
        workStartHour: Int = 9,
        workEndHour: Int = 18,
        nowMinuteOfDay: Int = LocalTime.now().hour * 60 + LocalTime.now().minute,
        isRescueMode: Boolean = false,
        timeConstraintMinutes: Int? = null,
        isArabic: Boolean = false
    ): LifeEngineState {
        val activeGoalTitles = goals.filter { !it.isCompleted }.map { it.title }

        val recommendation = determineRecommendation(
            tasks = tasks,
            habits = habits,
            habitLogs = habitLogs,
            goals = goals,
            milestones = milestones,
            calendarEvents = calendarEvents,
            workStartHour = workStartHour,
            workEndHour = workEndHour,
            nowMinuteOfDay = nowMinuteOfDay,
            isRescueMode = isRescueMode,
            timeConstraintMinutes = timeConstraintMinutes,
            isArabic = isArabic
        )

        val adaptivePlan = AdaptiveDayEngine.generateAdaptivePlan(
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

        val rescuePlan = RescueModeEngine.generateRescuePlan(
            tasks = tasks,
            activeGoalTitles = activeGoalTitles,
            workEndHour = workEndHour,
            nowMinuteOfDay = nowMinuteOfDay,
            isArabic = isArabic
        )

        val goalHabitReport = GoalHabitIntelligence.analyze(
            goals = goals,
            milestones = milestones,
            habits = habits,
            habitLogs = habitLogs,
            tasks = tasks,
            isArabic = isArabic
        )

        val personalization = PersonalizationEngine.analyzePatterns(
            tasks = tasks,
            focusSessions = focusSessions,
            habits = habits,
            habitLogs = habitLogs,
            isArabic = isArabic
        )

        val dailyScore = DailyScoreCalculator.calculateDailyScore(
            tasks = tasks,
            habits = habits,
            habitLogs = habitLogs,
            focusSessions = focusSessions,
            dailyFocusTargetMinutes = 60,
            isArabic = isArabic
        )

        return LifeEngineState(
            recommendation = recommendation,
            adaptivePlan = adaptivePlan,
            rescuePlan = rescuePlan,
            goalHabitReport = goalHabitReport,
            personalization = personalization,
            dailyScore = dailyScore,
            isRescueModeActive = isRescueMode
        )
    }

    private fun formatMinutesToTime(minutes: Int): String {
        val hour24 = minutes / 60
        val min = minutes % 60
        val hour12 = when (val h = hour24 % 12) {
            0 -> 12
            else -> h
        }
        val amPm = if (hour24 < 12) "AM" else "PM"
        return String.format("%02d:%02d %s", hour12, min, amPm)
    }
}
