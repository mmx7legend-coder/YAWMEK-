package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FocusTimerMode(val defaultMinutes: Int, val titleEn: String, val titleAr: String) {
    POMODORO_25(25, "Pomodoro (25m)", "بومودورو (25 دقيقة)"),
    DEEP_50(50, "Deep Focus (50m)", "تركيز عميق (50 دقيقة)"),
    BREAK_5(5, "Short Break (5m)", "استراحة قصيرة (5د)"),
    BREAK_15(15, "Long Break (15m)", "استراحة طويلة (15د)"),
    POMODORO(25, "Pomodoro (25m)", "بومودورو (25 دقيقة)"),
    PRESET_15(15, "Sprint (15m)", "تركيز سريع (15 دقيقة)"),
    PRESET_25(25, "Standard (25m)", "تركيز قياسي (25 دقيقة)"),
    PRESET_30(30, "Balanced (30m)", "تركيز متوازن (30 دقيقة)"),
    PRESET_45(45, "Deep Sprint (45m)", "تركيز عميق (45 دقيقة)"),
    PRESET_60(60, "Power Hour (60m)", "ساعة القوة (60 دقيقة)"),
    PRESET_90(90, "Ultradian Flow (90m)", "جلسة تدفق قصوى (90 دقيقة)"),
    SHORT_BREAK(5, "Short Break (5m)", "استراحة قصيرة (5د)"),
    LONG_BREAK(15, "Long Break (15m)", "استراحة طويلة (15د)"),
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
    val soundMode: String = "SILENT", // "SILENT", "RAIN", "FOREST", "BROWN_NOISE", "WHITE_NOISE", "SOFT_AMBIENCE"
    val notes: String = "",
    val xpAwarded: Int = 0
)

@Entity(tableName = "focus_preferences")
data class FocusPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val dailyTargetMinutes: Int = 120,
    val weeklyTargetMinutes: Int = 600,
    val defaultPreset: FocusTimerMode = FocusTimerMode.POMODORO_25,
    val selectedSound: String = "RAIN",
    val soundVolume: Float = 0.7f,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val focusShieldEnabled: Boolean = true
) {
    val volume: Float get() = soundVolume
}
