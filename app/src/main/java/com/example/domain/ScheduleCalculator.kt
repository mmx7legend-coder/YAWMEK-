package com.example.domain

import com.example.data.local.model.Priority
import com.example.data.local.model.TaskCategory
import com.example.data.local.model.TaskEntity
import java.time.LocalDate
import java.time.ZoneId

sealed class TimelineItem {
    data class TaskBlock(
        val task: TaskEntity,
        val startMinuteOfDay: Int,
        val endMinuteOfDay: Int,
        val durationMinutes: Int
    ) : TimelineItem()

    data class FreeTimeSlot(
        val startMinuteOfDay: Int,
        val endMinuteOfDay: Int,
        val durationMinutes: Int
    ) : TimelineItem()
}

object ScheduleCalculator {

    fun buildTimelineForDate(
        tasks: List<TaskEntity>,
        targetDate: LocalDate,
        dayStartHour: Int = 8,
        dayEndHour: Int = 22
    ): List<TimelineItem> {
        val targetStartMillis = targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val targetEndMillis = targetDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        val dayTasks = tasks.filter { task ->
            task.dueDateMillis != null && task.dueDateMillis in targetStartMillis..targetEndMillis
        }

        val scheduledTasks = dayTasks.mapNotNull { task ->
            val startMin = task.dueTimeMinutes
            if (startMin != null) {
                val duration = if (task.durationMinutes > 0) task.durationMinutes else 30
                val endMin = startMin + duration
                TimelineItem.TaskBlock(task, startMin, endMin, duration)
            } else null
        }.sortedBy { it.startMinuteOfDay }

        val result = mutableListOf<TimelineItem>()
        var currentPointer = dayStartHour * 60
        val dayEndMin = dayEndHour * 60

        for (block in scheduledTasks) {
            if (block.startMinuteOfDay > currentPointer + 15) {
                val freeDuration = block.startMinuteOfDay - currentPointer
                result.add(
                    TimelineItem.FreeTimeSlot(
                        startMinuteOfDay = currentPointer,
                        endMinuteOfDay = block.startMinuteOfDay,
                        durationMinutes = freeDuration
                    )
                )
            }
            result.add(block)
            currentPointer = maxOf(currentPointer, block.endMinuteOfDay)
        }

        if (currentPointer < dayEndMin - 15) {
            val remainingFree = dayEndMin - currentPointer
            result.add(
                TimelineItem.FreeTimeSlot(
                    startMinuteOfDay = currentPointer,
                    endMinuteOfDay = dayEndMin,
                    durationMinutes = remainingFree
                )
            )
        }

        return result
    }

    fun formatMinuteOfDay(minuteOfDay: Int, is24Hour: Boolean = false): String {
        val hours = minuteOfDay / 60
        val minutes = minuteOfDay % 60
        return if (is24Hour) {
            String.format("%02d:%02d", hours, minutes)
        } else {
            val period = if (hours >= 12) "PM" else "AM"
            val displayHour = when {
                hours == 0 -> 12
                hours > 12 -> hours - 12
                else -> hours
            }
            String.format("%d:%02d %s", displayHour, minutes, period)
        }
    }
}
