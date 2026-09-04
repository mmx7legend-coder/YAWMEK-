package com.example.domain.reset

import com.example.data.local.model.TaskEntity
import com.example.data.repository.YawmekRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Logic layer for the Daily Reset feature.
 * Identifies unfinished tasks, handles rollover to the next day, or marks them as skipped.
 */
class DailyResetManager(
    private val repository: YawmekRepository
) {

    /**
     * Identifies all unfinished tasks that were scheduled for today or earlier.
     */
    fun identifyUnfinishedTasks(tasks: List<TaskEntity>, referenceDate: LocalDate = LocalDate.now()): List<TaskEntity> {
        val zone = ZoneId.systemDefault()
        val endOfReferenceDateEpoch = referenceDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        return tasks.filter { task ->
            !task.isCompleted && task.dueDateMillis != null && task.dueDateMillis < endOfReferenceDateEpoch
        }
    }

    /**
     * Moves the specified unfinished tasks to tomorrow.
     */
    suspend fun moveTasksToTomorrow(tasks: List<TaskEntity>) {
        val zone = ZoneId.systemDefault()
        val tomorrowEpoch = LocalDate.now().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        tasks.forEach { task ->
            val updatedTask = task.copy(
                dueDateMillis = tomorrowEpoch
            )
            repository.insertTask(updatedTask)
        }
    }

    /**
     * Marks the specified tasks as skipped (soft completion with note or completed so they clear the board).
     */
    suspend fun markTasksAsSkipped(tasks: List<TaskEntity>) {
        tasks.forEach { task ->
            val skippedTask = task.copy(
                isCompleted = true,
                completedAtMillis = System.currentTimeMillis(),
                description = if (task.description.isBlank()) "[تخطّي / Skipped]" else "${task.description} [تخطّي / Skipped]"
            )
            repository.insertTask(skippedTask)
        }
    }

    /**
     * Reschedules an individual task to a specific date.
     */
    suspend fun rescheduleTask(task: TaskEntity, targetDateEpoch: Long) {
        val updatedTask = task.copy(
            dueDateMillis = targetDateEpoch
        )
        repository.insertTask(updatedTask)
    }
}
