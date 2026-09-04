package com.example

import com.example.data.local.model.CalendarEventEntity
import com.example.domain.calendar.SmartTimeBlockingEngine
import com.example.domain.calendar.TimeInterval
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CalendarSchedulingTest {

    @Test
    fun testTimeIntervalOverlap() {
        val intervalA = TimeInterval(1000L, 2000L)
        val intervalB = TimeInterval(1500L, 2500L)
        val intervalC = TimeInterval(2000L, 3000L) // Back-to-back, no overlap

        assertTrue(intervalA.overlaps(intervalB))
        assertTrue(intervalB.overlaps(intervalA))
        assertFalse(intervalA.overlaps(intervalC))
    }

    @Test
    fun testFindConflictingEventIds() {
        val event1 = CalendarEventEntity(
            id = 101L,
            title = "Morning Standup",
            startMillis = 1000000L,
            endMillis = 1001800L,
            isAllDay = false
        )
        val event2 = CalendarEventEntity(
            id = 102L,
            title = "Client Sync",
            startMillis = 1001000L, // Overlaps with event1
            endMillis = 1003000L,
            isAllDay = false
        )
        val event3 = CalendarEventEntity(
            id = 103L,
            title = "Deep Focus Block",
            startMillis = 1004000L, // Isolated, no overlap
            endMillis = 1007000L,
            isAllDay = false
        )

        val conflicts = SmartTimeBlockingEngine.findConflictingEventIds(listOf(event1, event2, event3))
        assertEquals(2, conflicts.size)
        assertTrue(conflicts.contains(101L))
        assertTrue(conflicts.contains(102L))
        assertFalse(conflicts.contains(103L))
    }

    @Test
    fun testFindFreeSlotsCalculation() {
        val today = LocalDate.of(2026, 9, 4)
        val zone = ZoneId.of("UTC")
        val workStart = today.atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
        val workEnd = today.atTime(17, 0).atZone(zone).toInstant().toEpochMilli()

        // Meeting from 10:00 to 11:00
        val meetingStart = today.atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        val meetingEnd = today.atTime(11, 0).atZone(zone).toInstant().toEpochMilli()

        val occupied = listOf(TimeInterval(meetingStart, meetingEnd))
        val freeSlots = SmartTimeBlockingEngine.findFreeSlots(
            targetDate = today,
            occupiedIntervals = occupied,
            workStartHour = 9,
            workEndHour = 17,
            minSlotDurationMinutes = 30,
            zoneId = zone
        )

        assertEquals(2, freeSlots.size)
        // First slot: 9:00 to 10:00 (1 hour)
        assertEquals(workStart, freeSlots[0].startMillis)
        assertEquals(meetingStart, freeSlots[0].endMillis)

        // Second slot: 11:00 to 17:00 (6 hours)
        assertEquals(meetingEnd, freeSlots[1].startMillis)
        assertEquals(workEnd, freeSlots[1].endMillis)
    }
}
