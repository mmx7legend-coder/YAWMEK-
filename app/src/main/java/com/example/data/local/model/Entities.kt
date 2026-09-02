package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority {
    HIGH, MEDIUM, LOW
}

enum class TaskCategory {
    WORK, STUDY, PERSONAL, HEALTH, FINANCE, OTHER
}

enum class Recurrence {
    NONE, DAILY, WEEKLY
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDateMillis: Long? = null, // Epoch millis at start of day or specific time
    val dueTimeMinutes: Int? = null, // Minute of day (e.g. 17 * 60 = 1020 for 5:00 PM)
    val durationMinutes: Int = 30, // Estimated duration
    val priority: Priority = Priority.MEDIUM,
    val category: TaskCategory = TaskCategory.PERSONAL,
    val isCompleted: Boolean = false,
    val completedAtMillis: Long? = null,
    val recurrence: Recurrence = Recurrence.NONE,
    val reminderEnabled: Boolean = false,
    val tags: String = "", // Comma separated
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "subtasks")
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val title: String,
    val isCompleted: Boolean = false
)

enum class ExpenseCategory {
    FOOD, TRANSPORT, EDUCATION, SHOPPING, BILLS, ENTERTAINMENT, HEALTH, OTHER
}

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val currency: String = "EGP",
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val note: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val createdAtMillis: Long = System.currentTimeMillis()
)

enum class BudgetPeriod {
    DAILY, WEEKLY, MONTHLY
}

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val amount: Double = 0.0,
    val currency: String = "EGP",
    val warningThresholdPercent: Int = 80
)

enum class HabitFrequency {
    DAILY, WEEKLY
}

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "Personal",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val targetDaysPerWeek: Int = 7,
    val colorHex: String = "#3B82F6",
    val iconKey: String = "check",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val isCompleted: Boolean = true,
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "General",
    val targetDateMillis: Long? = null,
    val colorHex: String = "#8B5CF6",
    val isCompleted: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String = "",
    val tag: String = "",
    val isPinned: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val createdAtMillis: Long = System.currentTimeMillis()
)

enum class AppLanguage {
    SYSTEM, ENGLISH, ARABIC
}

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val userName: String = "",
    val hasCompletedOnboarding: Boolean = false,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currency: String = "EGP",
    val selectedPriorities: String = "", // Comma separated: Productivity, Study, Work, etc.
    val workStartHour: Int = 9,
    val workEndHour: Int = 18,
    val defaultTaskDurationMinutes: Int = 30,
    val morningBriefingEnabled: Boolean = true,
    val eveningSummaryEnabled: Boolean = true
)
