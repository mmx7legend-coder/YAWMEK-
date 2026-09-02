package com.example.domain

import com.example.data.local.model.*
import com.example.domain.calendar.CalendarEventItem
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class RecommendationResult(
    val task: TaskEntity?,
    val habit: HabitEntity? = null,
    val title: String,
    val subtitle: String,
    val reasonEnglish: String,
    val reasonArabic: String,
    val estimatedDurationMinutes: Int,
    val freeTimeMinutesAvailable: Int,
    val urgencyLevel: UrgencyLevel,
    val alternatives: List<TaskEntity> = emptyList(),
    val nextCalendarEvent: CalendarEventItem? = null
)

enum class UrgencyLevel {
    OVERDUE, URGENT, HIGH_PRIORITY, GOOD_TIMING, MINDFUL_PAUSE
}

object SmartRecommendationEngine {

    fun recommendNextAction(
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        habitLogs: List<HabitLogEntity>,
        calendarEvents: List<CalendarEventItem> = emptyList(),
        workStartHour: Int = 9,
        workEndHour: Int = 18
    ): RecommendationResult {
        val now = LocalTime.now()
        val today = LocalDate.now()
        val currentMinuteOfDay = now.hour * 60 + now.minute
        val todayEpochDay = today.toEpochDay()
        val todayStartMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayEndMillis = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        val nowMillis = System.currentTimeMillis()

        // Detect upcoming calendar event today
        val nextCalendarEvent = calendarEvents
            .filter { it.startMillis > nowMillis && it.startMillis < todayEndMillis }
            .minByOrNull { it.startMillis }

        val minutesUntilNextEvent = if (nextCalendarEvent != null) {
            ((nextCalendarEvent.startMillis - nowMillis) / (60 * 1000)).toInt().coerceAtLeast(1)
        } else {
            120
        }

        val pendingTasks = tasks.filter { !it.isCompleted }

        // If no pending tasks at all
        if (pendingTasks.isEmpty()) {
            // Check if there is an uncompleted habit
            val completedHabitIdsToday = habitLogs.filter { it.dateEpochDay == todayEpochDay }.map { it.habitId }.toSet()
            val uncompletedHabits = habits.filter { !completedHabitIdsToday.contains(it.id) }

            if (uncompletedHabits.isNotEmpty()) {
                val nextHabit = uncompletedHabits.first()
                return RecommendationResult(
                    task = null,
                    habit = nextHabit,
                    title = nextHabit.title,
                    subtitle = "Daily Habit Check-in",
                    reasonEnglish = "You have completed all your tasks! Now is a great moment to keep your '${nextHabit.title}' habit streak alive.",
                    reasonArabic = "أنجزت جميع مهامك اليوم! هذه لحظة مثالية لإكمال عادتك '${nextHabit.title}' والحفاظ على استمراريتك.",
                    estimatedDurationMinutes = 15,
                    freeTimeMinutesAvailable = minutesUntilNextEvent,
                    urgencyLevel = UrgencyLevel.GOOD_TIMING,
                    nextCalendarEvent = nextCalendarEvent
                )
            }

            return RecommendationResult(
                task = null,
                habit = null,
                title = "All clear!",
                subtitle = "No pending actions",
                reasonEnglish = if (nextCalendarEvent != null) {
                    "No pending tasks. You have $minutesUntilNextEvent free minutes before '${nextCalendarEvent.title}'."
                } else {
                    "You are completely on top of your day. Take a well-deserved rest, or add your next goal when you are ready."
                },
                reasonArabic = if (nextCalendarEvent != null) {
                    "لا توجد مهام معلقة. لديك $minutesUntilNextEvent دقيقة فراغ قبل موعد '${nextCalendarEvent.title}'."
                } else {
                    "يومك تحت السيطرة تماماً! استمتع بوقتك أو أضف مهامك وأهدافك القادمة عندما تكون مستعداً."
                },
                estimatedDurationMinutes = 0,
                freeTimeMinutesAvailable = minutesUntilNextEvent,
                urgencyLevel = UrgencyLevel.MINDFUL_PAUSE,
                nextCalendarEvent = nextCalendarEvent
            )
        }

        // Score pending tasks
        // If there's an upcoming event within 60 minutes, prioritize tasks that can comfortably fit!
        val fittingTask = if (minutesUntilNextEvent in 10..60) {
            pendingTasks.firstOrNull { it.durationMinutes <= minutesUntilNextEvent - 5 }
        } else null

        val scoredTasks = pendingTasks.map { task ->
            var score = 0
            var isOverdue = false
            var isDueToday = false

            if (task.dueDateMillis != null) {
                if (task.dueDateMillis < todayStartMillis) {
                    score += 100
                    isOverdue = true
                } else if (task.dueDateMillis in todayStartMillis..todayEndMillis) {
                    score += 50
                    isDueToday = true
                }
            }

            when (task.priority) {
                Priority.HIGH -> score += 40
                Priority.MEDIUM -> score += 20
                Priority.LOW -> score += 5
            }

            if (task.dueTimeMinutes != null) {
                val diff = task.dueTimeMinutes - currentMinuteOfDay
                if (diff in 0..120) {
                    score += 35 // Scheduled very soon
                } else if (diff < 0 && isDueToday) {
                    score += 60 // Missed scheduled time today
                }
            }

            // Gap fit bonus if calendar event approaching
            if (fittingTask != null && task.id == fittingTask.id) {
                score += 85
            }

            Triple(task, score, Pair(isOverdue, isDueToday))
        }.sortedByDescending { it.second }

        val best = scoredTasks.first().first
        val bestScore = scoredTasks.first().second
        val (isOverdue, isDueToday) = scoredTasks.first().third
        val alternatives = scoredTasks.drop(1).take(3).map { it.first }

        val duration = if (best.durationMinutes > 0) best.durationMinutes else 30
        val urgency = when {
            isOverdue -> UrgencyLevel.OVERDUE
            best.priority == Priority.HIGH && (isDueToday || best.dueTimeMinutes != null) -> UrgencyLevel.URGENT
            best.priority == Priority.HIGH -> UrgencyLevel.HIGH_PRIORITY
            else -> UrgencyLevel.GOOD_TIMING
        }

        val reasonEn: String
        val reasonAr: String

        when {
            fittingTask != null && best.id == fittingTask.id && nextCalendarEvent != null -> {
                reasonEn = "You have $minutesUntilNextEvent free minutes before '${nextCalendarEvent.title}'. Finishing '${best.title}' (~${duration}m) is a perfect quick win."
                reasonAr = "لديك $minutesUntilNextEvent دقيقة فراغ قبل '${nextCalendarEvent.title}'. إنجاز '${best.title}' (~${duration} دقيقة) هو استغلال مثالي للوقت المتاح."
            }
            isOverdue -> {
                reasonEn = "This task is past its scheduled deadline. Finishing it first will clear cognitive load and reset your momentum."
                reasonAr = "هذه المهمة تجاوزت موعدها المحدد. إنجازها الآن سيزيل التشتت ويعيد ترتيب صفاء ذهنك."
            }
            best.priority == Priority.HIGH && isDueToday -> {
                reasonEn = "High priority and due today. Dedicating your current energy here creates the biggest impact for your day."
                reasonAr = "أولوية قصوى ومطلوبة اليوم. توجيه تركيزك إليها الآن سيحقق الفارق الأكبر في يومك."
            }
            best.dueTimeMinutes != null && Math.abs(best.dueTimeMinutes - currentMinuteOfDay) <= 60 -> {
                val hour = best.dueTimeMinutes / 60
                val min = best.dueTimeMinutes % 60
                val formattedTime = String.format("%02d:%02d", hour, min)
                reasonEn = "Scheduled around $formattedTime (~${duration}m). Jumping in now keeps you precisely on track with your plan."
                reasonAr = "مجدولة في حوالي $formattedTime (~${duration} دقيقة). البدء فيها الآن يحافظ على انسيابية جدولك."
            }
            best.priority == Priority.HIGH -> {
                reasonEn = "Marked as High Priority (~${duration}m). Tackling high-leverage work during active hours maximizes progress."
                reasonAr = "مصنفة كأولوية عالية (~${duration} دقيقة). إنجاز الأعمال المؤثرة الآن يمنحك شعوراً فورياً بالتحكم والإنتاجية."
            }
            else -> {
                reasonEn = "Fits nicely into your available time (~${duration}m). Completing this now will keep your flow steady."
                reasonAr = "تناسب وقتك المتاح حالياً (~${duration} دقيقة). إتمامها سيبقي وتيرة إنجازك مستمرة بسلاسة."
            }
        }

        return RecommendationResult(
            task = best,
            habit = null,
            title = best.title,
            subtitle = if (best.category != TaskCategory.OTHER) best.category.name.lowercase().replaceFirstChar { it.uppercase() } else "Task",
            reasonEnglish = reasonEn,
            reasonArabic = reasonAr,
            estimatedDurationMinutes = duration,
            freeTimeMinutesAvailable = minutesUntilNextEvent,
            urgencyLevel = urgency,
            alternatives = alternatives,
            nextCalendarEvent = nextCalendarEvent
        )
    }
}
