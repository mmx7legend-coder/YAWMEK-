package com.example.domain.life

import com.example.data.local.model.Priority
import com.example.data.local.model.TaskEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object RescueModeEngine {

    fun generateRescuePlan(
        tasks: List<TaskEntity>,
        activeGoalTitles: List<String> = emptyList(),
        workEndHour: Int = 18,
        nowMinuteOfDay: Int = LocalTime.now().hour * 60 + LocalTime.now().minute,
        isArabic: Boolean = false
    ): RescuePlan {
        val today = LocalDate.now()
        val todayStartMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayEndMillis = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        val availableMinutesRemaining = ((workEndHour * 60) - nowMinuteOfDay).coerceAtLeast(30)

        // Pending tasks due today or overdue
        val todayPending = tasks.filter {
            !it.isCompleted && (it.dueDateMillis == null || it.dueDateMillis <= todayEndMillis)
        }

        val mustDo = mutableListOf<RescueTaskItem>()
        val important = mutableListOf<RescueTaskItem>()
        val optional = mutableListOf<RescueTaskItem>()

        for (task in todayPending) {
            val isOverdue = task.dueDateMillis != null && task.dueDateMillis < todayStartMillis
            val isDueSoonToday = task.dueDateMillis in todayStartMillis..todayEndMillis && task.dueTimeMinutes != null && task.dueTimeMinutes <= nowMinuteOfDay + 180
            val alignsWithGoal = activeGoalTitles.any { task.title.contains(it, ignoreCase = true) }

            when {
                // Must Do: Overdue, high priority, strict imminent deadline, or goal-critical
                isOverdue -> {
                    mustDo.add(
                        RescueTaskItem(
                            task = task,
                            tier = RescueTier.MUST_DO,
                            selectionReasonEnglish = "Overdue deadline requires immediate intervention to unblock commitments.",
                            selectionReasonArabic = "مهمة متأخرة عن موعدها تتطلب حسماً فورياً لمنع تراكم الالتزامات."
                        )
                    )
                }
                task.priority == Priority.HIGH || (isDueSoonToday && alignsWithGoal) -> {
                    mustDo.add(
                        RescueTaskItem(
                            task = task,
                            tier = RescueTier.MUST_DO,
                            selectionReasonEnglish = "High-impact priority directly protecting your core active milestone.",
                            selectionReasonArabic = "أولوية قصوى تحمي بشكل مباشر أهدافك ومراحل خطتك النشطة."
                        )
                    )
                }
                // Important: Medium priority, due today but non-catastrophic
                task.priority == Priority.MEDIUM || alignsWithGoal -> {
                    important.add(
                        RescueTaskItem(
                            task = task,
                            tier = RescueTier.IMPORTANT,
                            selectionReasonEnglish = "Valuable progress contributor. Execute if Must-Do items wrap up ahead of schedule.",
                            selectionReasonArabic = "مهمة مهمة للمحافظة على التقدم. نفذها إذا انتهت مهام الإلزامية مبكراً."
                        )
                    )
                }
                // Optional: Low priority, exploratory, or large task without strict deadline
                else -> {
                    val tomorrowStartMillis = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    optional.add(
                        RescueTaskItem(
                            task = task,
                            tier = RescueTier.OPTIONAL,
                            selectionReasonEnglish = "Safe to defer to tomorrow morning with zero penalty or risk to active goals.",
                            selectionReasonArabic = "آمنة للتأجيل لصباح الغد بدون أي تأثير سلبي أو خطر على أهدافك الحالية.",
                            suggestedNewDueDateMillis = tomorrowStartMillis,
                            suggestedNewDueTimeMinutes = 600 // 10:00 AM tomorrow
                        )
                    )
                }
            }
        }

        val totalMinutesRequired = todayPending.sumOf { if (it.durationMinutes > 0) it.durationMinutes else 30 }
        val minutesSavedByPostponing = optional.sumOf { if (it.task.durationMinutes > 0) it.task.durationMinutes else 30 }

        val isTriggered = totalMinutesRequired > availableMinutesRemaining || mustDo.any { it.task.dueDateMillis != null && it.task.dueDateMillis < todayStartMillis }

        val summaryEn = if (isTriggered) {
            "Emergency triage active: focusing exclusively on ${mustDo.size} Must-Do tasks. Deferring ${optional.size} optional tasks saves $minutesSavedByPostponing min."
        } else {
            "Schedule is currently manageable. Rescue Mode standby."
        }

        val summaryAr = if (isTriggered) {
            "خطة الإنقاذ جاهزة: التركيز فقط على ${mustDo.size} مهام إلزامية. تأجيل ${optional.size} مهام اختيارية يوفر $minutesSavedByPostponing دقيقة."
        } else {
            "جدولك في النطاق الآمن حالياً. وضع الإنقاذ في وضع الاستعداد."
        }

        return RescuePlan(
            isTriggered = isTriggered,
            mustDoTasks = mustDo,
            importantTasks = important,
            optionalTasks = optional,
            totalMinutesRequired = totalMinutesRequired,
            availableMinutesRemaining = availableMinutesRemaining,
            minutesSavedByPostponing = minutesSavedByPostponing,
            summaryEnglish = summaryEn,
            summaryArabic = summaryAr
        )
    }
}
