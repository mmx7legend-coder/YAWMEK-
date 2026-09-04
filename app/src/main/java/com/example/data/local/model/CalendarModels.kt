package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CalendarViewMode(val titleEn: String, val titleAr: String) {
    DAY("Day", "يوم"),
    WEEK("Week", "أسبوع"),
    MONTH("Month", "شهر"),
    AGENDA("Agenda", "أجندة")
}

enum class CalendarEventCategory(
    val titleEn: String,
    val titleAr: String,
    val colorHex: String,
    val iconName: String
) {
    WORK("Work", "عمل", "#2563EB", "work"),
    STUDY("Study", "دراسة", "#7C3AED", "school"),
    PERSONAL("Personal", "شخصي", "#059669", "person"),
    HEALTH("Health", "صحة", "#DC2626", "fitness_center"),
    FINANCE("Finance", "مالي", "#D97706", "payments"),
    FOCUS("Focus Block", "جلسة تركيز", "#8B5CF6", "timer"),
    HABIT("Habit", "عادة", "#06B6D4", "repeat"),
    GENERAL("General", "عام", "#64748B", "event")
}

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val startMillis: Long,
    val endMillis: Long,
    val isAllDay: Boolean = false,
    val category: String = "GENERAL",
    val colorHex: String = "#2563EB",
    val location: String = "",
    val recurrence: Recurrence = Recurrence.NONE,
    val reminderMinutesBefore: Int = 15,
    val linkedTaskId: Long? = null,
    val linkedGoalId: Long? = null,
    val linkedHabitId: Long? = null,
    val isDeviceCalendarSynced: Boolean = false,
    val deviceCalendarEventId: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class SmartTimeSlotProposal(
    val taskId: Long,
    val taskTitle: String,
    val startMillis: Long,
    val endMillis: Long,
    val durationMinutes: Int,
    val priority: Priority,
    val reasonEn: String,
    val reasonAr: String
)
