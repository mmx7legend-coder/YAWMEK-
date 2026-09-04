package com.example.domain.calendar

import com.example.data.local.model.CalendarEventEntity
import com.example.data.local.model.Priority
import com.example.data.local.model.SmartTimeSlotProposal
import com.example.data.local.model.TaskEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class TimeInterval(
    val startMillis: Long,
    val endMillis: Long
) {
    fun overlaps(other: TimeInterval): Boolean {
        return startMillis < other.endMillis && other.startMillis < endMillis
    }
}

object SmartTimeBlockingEngine {

    /**
     * Identifies all calendar events that conflict/overlap with at least one other event.
     */
    fun findConflictingEventIds(events: List<CalendarEventEntity>): Set<Long> {
        val timedEvents = events.filter { !it.isAllDay && it.endMillis > it.startMillis }
        val conflictingIds = mutableSetOf<Long>()

        for (i in timedEvents.indices) {
            val a = timedEvents[i]
            val intervalA = TimeInterval(a.startMillis, a.endMillis)
            for (j in i + 1 until timedEvents.size) {
                val b = timedEvents[j]
                val intervalB = TimeInterval(b.startMillis, b.endMillis)
                if (intervalA.overlaps(intervalB)) {
                    conflictingIds.add(a.id)
                    conflictingIds.add(b.id)
                }
            }
        }
        return conflictingIds
    }

    /**
     * Calculates free non-overlapping time gaps on a specific day between work hours.
     */
    fun findFreeSlots(
        targetDate: LocalDate,
        occupiedIntervals: List<TimeInterval>,
        workStartHour: Int = 9,
        workEndHour: Int = 18,
        minSlotDurationMinutes: Int = 15,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<TimeInterval> {
        val dayStart = targetDate.atTime(workStartHour.coerceIn(0, 23), 0)
            .atZone(zoneId).toInstant().toEpochMilli()
        val dayEnd = targetDate.atTime(workEndHour.coerceIn(1, 24).coerceAtLeast(workStartHour + 1), 0)
            .atZone(zoneId).toInstant().toEpochMilli()

        // Clip occupied intervals to this workday window
        val relevantIntervals = occupiedIntervals
            .map { TimeInterval(it.startMillis.coerceAtLeast(dayStart), it.endMillis.coerceAtMost(dayEnd)) }
            .filter { it.startMillis < it.endMillis }
            .sortedBy { it.startMillis }

        // Merge contiguous or overlapping occupied blocks
        val mergedBusy = mutableListOf<TimeInterval>()
        for (interval in relevantIntervals) {
            if (mergedBusy.isEmpty()) {
                mergedBusy.add(interval)
            } else {
                val last = mergedBusy.last()
                if (interval.startMillis <= last.endMillis) {
                    mergedBusy[mergedBusy.size - 1] = TimeInterval(last.startMillis, maxOf(last.endMillis, interval.endMillis))
                } else {
                    mergedBusy.add(interval)
                }
            }
        }

        // Compute available free gaps
        val freeSlots = mutableListOf<TimeInterval>()
        val minDurationMillis = minSlotDurationMinutes * 60000L
        var currentPointer = dayStart

        for (busy in mergedBusy) {
            if (busy.startMillis > currentPointer) {
                val gap = busy.startMillis - currentPointer
                if (gap >= minDurationMillis) {
                    freeSlots.add(TimeInterval(currentPointer, busy.startMillis))
                }
            }
            if (busy.endMillis > currentPointer) {
                currentPointer = busy.endMillis
            }
        }

        if (dayEnd > currentPointer && (dayEnd - currentPointer) >= minDurationMillis) {
            freeSlots.add(TimeInterval(currentPointer, dayEnd))
        }

        return freeSlots
    }

    /**
     * Proposes smart, non-overlapping time blocks for eligible pending tasks based on priority,
     * due date, and estimated duration.
     */
    fun proposeTimeBlocksForDay(
        targetDate: LocalDate,
        pendingTasks: List<TaskEntity>,
        existingEvents: List<CalendarEventEntity>,
        workStartHour: Int = 9,
        workEndHour: Int = 18,
        bufferMinutes: Int = 10,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<SmartTimeSlotProposal> {
        // Collect existing busy intervals
        val busyIntervals = existingEvents
            .filter { !it.isAllDay }
            .map { TimeInterval(it.startMillis, it.endMillis) }
            .toMutableList()

        // Filter and rank eligible pending tasks
        val eligibleTasks = pendingTasks
            .filter { !it.isCompleted }
            .sortedWith(
                compareByDescending<TaskEntity> { it.priority == Priority.HIGH }
                    .thenByDescending { it.priority == Priority.MEDIUM }
                    .thenBy { it.dueDateMillis ?: Long.MAX_VALUE }
                    .thenByDescending { it.durationMinutes }
            )

        val proposals = mutableListOf<SmartTimeSlotProposal>()

        for (task in eligibleTasks) {
            val taskDurationMinutes = task.durationMinutes.coerceIn(15, 120)
            val neededMillis = taskDurationMinutes * 60000L
            val bufferMillis = bufferMinutes * 60000L

            val freeSlots = findFreeSlots(
                targetDate = targetDate,
                occupiedIntervals = busyIntervals,
                workStartHour = workStartHour,
                workEndHour = workEndHour,
                minSlotDurationMinutes = taskDurationMinutes,
                zoneId = zoneId
            )

            val slot = freeSlots.firstOrNull { (it.endMillis - it.startMillis) >= neededMillis }
            if (slot != null) {
                val proposedStart = slot.startMillis
                val proposedEnd = proposedStart + neededMillis

                val priorityAr = when (task.priority) {
                    Priority.HIGH -> "عالية"
                    Priority.MEDIUM -> "متوسطة"
                    Priority.LOW -> "منخفضة"
                }

                proposals.add(
                    SmartTimeSlotProposal(
                        taskId = task.id,
                        taskTitle = task.title,
                        startMillis = proposedStart,
                        endMillis = proposedEnd,
                        durationMinutes = taskDurationMinutes,
                        priority = task.priority,
                        reasonEn = "Prioritized during peak focus slot based on ${task.priority.name} priority.",
                        reasonAr = "تم حجز فترة تركيز ذكية مناسبة لأولوية $priorityAr."
                    )
                )

                // Add to busy intervals including the buffer to prevent back-to-back fatigue
                busyIntervals.add(TimeInterval(proposedStart, proposedEnd + bufferMillis))
            }
        }

        return proposals
    }
}
