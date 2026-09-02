package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FocusTimerMode(val defaultMinutes: Int, val titleEn: String, val titleAr: String) {
    POMODORO_25(25, "Pomodoro (25m)", "بومودورو (25 دقيقة)"),
    DEEP_50(50, "Deep Focus (50m)", "تركيز عميق (50 دقيقة)"),
    BREAK_5(5, "Short Break (5m)", "استراحة قصيرة (5د)"),
    BREAK_15(15, "Long Break (15m)", "استراحة طويلة (15د)"),
    CUSTOM(15, "Custom Timer", "مؤقت مخصص")
}

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val durationMinutes: Int = 25,
    val actualSeconds: Int = 0,
    val mode: FocusTimerMode = FocusTimerMode.POMODORO_25,
    val isCompleted: Boolean = true,
    val startedAtMillis: Long = System.currentTimeMillis(),
    val completedAtMillis: Long? = System.currentTimeMillis(),
    val soundMode: String = "SILENT", // "SILENT", "WHITE_NOISE", "RAIN", "BINAURAL"
    val notes: String = ""
)
