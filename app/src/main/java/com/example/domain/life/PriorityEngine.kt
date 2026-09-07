package com.example.domain.life

import com.example.data.local.model.Priority
import com.example.data.local.model.TaskEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object PriorityEngine {

    fun scoreTask(
        task: TaskEntity,
        activeGoalTitles: List<String> = emptyList(),
        availableMinutes: Int = 120,
        nowMinuteOfDay: Int = LocalTime.now().hour * 60 + LocalTime.now().minute,
        nowMillis: Long = System.currentTimeMillis()
    ): Double {
        var score = 0.0

        // 1. Base Priority Weight
        when (task.priority) {
            Priority.HIGH -> score += 35.0
            Priority.MEDIUM -> score += 20.0
            Priority.LOW -> score += 10.0
        }

        // 2. Deadline Urgency & Proximity
        val dueDate = task.dueDateMillis
        if (dueDate != null) {
            val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val todayEnd = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

            when {
                dueDate < todayStart -> {
                    // Overdue task receives maximum urgency boost
                    score += 55.0
                }
                dueDate in todayStart..todayEnd -> {
                    score += 25.0
                    val dueMin = task.dueTimeMinutes
                    if (dueMin != null) {
                        val diff = dueMin - nowMinuteOfDay
                        when {
                            diff in 0..120 -> score += 30.0 // Due within next 2 hours
                            diff < 0 -> score += 45.0       // Past due time today
                            diff in 121..300 -> score += 15.0
                        }
                    }
                }
                dueDate <= todayEnd + (2 * 86400000L) -> {
                    // Due within the next 48 hours
                    score += 10.0
                }
            }
        }

        // 3. Alignment with Active Goals & Milestones
        val matchesGoal = activeGoalTitles.any { goalTitle ->
            goalTitle.isNotBlank() && (
                task.title.contains(goalTitle, ignoreCase = true) ||
                task.description.contains(goalTitle, ignoreCase = true) ||
                goalTitle.contains(task.title, ignoreCase = true)
            )
        }
        if (matchesGoal) {
            score += 25.0
        }

        // 4. Temporal Fit (Can it be completed cleanly in the available time window?)
        val duration = if (task.durationMinutes > 0) task.durationMinutes else 30
        if (duration <= availableMinutes) {
            score += 15.0
        } else {
            // Task is too large for the immediate window: slight damping so user isn't stuck starting something they can't finish
            score -= 10.0
        }

        // 5. Quick Win Momentum (<= 20 min)
        if (duration in 1..20) {
            score += 12.0
        }

        // 6. Cognitive Friction / Stalling Age Factor
        // If task was created more than 3 days ago and is still pending, give it a push so it doesn't linger forever
        val ageDays = ((nowMillis - task.createdAtMillis) / (86400000L)).toInt()
        if (ageDays >= 3) {
            score += (ageDays.coerceAtMost(10) * 2.0)
        }

        // 7. Energy Curve: High priority tasks get an edge during morning/early afternoon focus hours (8 AM - 2 PM)
        val isDeepWorkHours = nowMinuteOfDay in 480..840
        if (isDeepWorkHours && task.priority == Priority.HIGH) {
            score += 10.0
        }

        return score
    }

    fun rankTasks(
        tasks: List<TaskEntity>,
        activeGoalTitles: List<String> = emptyList(),
        availableMinutes: Int = 120,
        nowMinuteOfDay: Int = LocalTime.now().hour * 60 + LocalTime.now().minute,
        nowMillis: Long = System.currentTimeMillis()
    ): List<Pair<TaskEntity, Double>> {
        return tasks
            .filter { !it.isCompleted }
            .map { task ->
                task to scoreTask(task, activeGoalTitles, availableMinutes, nowMinuteOfDay, nowMillis)
            }
            .sortedByDescending { it.second }
    }
}
