package com.example.domain.life

import com.example.data.local.model.*
import com.example.domain.calendar.CalendarEventItem

enum class RecommendationActionType {
    TASK,
    HABIT,
    MINDFUL_BREAK,
    RESCUE_SESSION
}

enum class RescueTier {
    MUST_DO,
    IMPORTANT,
    OPTIONAL
}

enum class PlannedItemType {
    TASK,
    HABIT,
    FOCUS_BLOCK,
    CALENDAR_EVENT,
    BUFFER_REST
}

enum class DailyScoreTier {
    MASTERY,     // 90 - 100
    MOMENTUM,    // 75 - 89
    FOCUSED,     // 60 - 74
    BUILDING,    // 40 - 59
    RESTART      // 0 - 39
}

enum class InsightCategory {
    VELOCITY,
    PATTERN,
    HABIT,
    WARNING
}

enum class RestructuringType {
    REDUCE_DURATION,
    MOVE_TIME,
    ATOMIC_STACK,
    FREQUENCY_DROP
}

data class LifeRecommendation(
    val actionType: RecommendationActionType,
    val task: TaskEntity? = null,
    val habit: HabitEntity? = null,
    val title: String,
    val subtitle: String,
    val reasonEnglish: String,
    val reasonArabic: String,
    val estimatedDurationMinutes: Int,
    val priority: Priority,
    val expectedImpactEnglish: String,
    val expectedImpactArabic: String,
    val freeTimeMinutesAvailable: Int,
    val canStart: Boolean = true,
    val canComplete: Boolean = true,
    val canSnooze: Boolean = true,
    val canReschedule: Boolean = true,
    val alternatives: List<TaskEntity> = emptyList(),
    val nextCalendarEvent: CalendarEventItem? = null
)

data class PlannedDayItem(
    val id: String,
    val task: TaskEntity? = null,
    val habit: HabitEntity? = null,
    val calendarEvent: CalendarEventItem? = null,
    val title: String,
    val startMinuteOfDay: Int,
    val durationMinutes: Int,
    val itemType: PlannedItemType,
    val priority: Priority,
    val tier: RescueTier,
    val isCompleted: Boolean = false,
    val reasonEnglish: String,
    val reasonArabic: String
) {
    val endMinuteOfDay: Int
        get() = startMinuteOfDay + durationMinutes
}

data class AdaptiveDayPlan(
    val plannedItems: List<PlannedDayItem>,
    val totalScheduledMinutes: Int,
    val remainingFreeMinutesToday: Int,
    val isOverloaded: Boolean,
    val delayMinutesDetected: Int,
    val isRescueModeActive: Boolean,
    val canUndo: Boolean,
    val explanationEnglish: String,
    val explanationArabic: String,
    val generatedAtMillis: Long = System.currentTimeMillis()
)

data class RescueTaskItem(
    val task: TaskEntity,
    val tier: RescueTier,
    val selectionReasonEnglish: String,
    val selectionReasonArabic: String,
    val suggestedNewDueDateMillis: Long? = null,
    val suggestedNewDueTimeMinutes: Int? = null
)

data class RescuePlan(
    val isTriggered: Boolean,
    val mustDoTasks: List<RescueTaskItem>,
    val importantTasks: List<RescueTaskItem>,
    val optionalTasks: List<RescueTaskItem>,
    val totalMinutesRequired: Int,
    val availableMinutesRemaining: Int,
    val minutesSavedByPostponing: Int,
    val summaryEnglish: String,
    val summaryArabic: String
)

data class GoalRiskAssessment(
    val goal: GoalEntity,
    val daysRemaining: Int,
    val totalMilestones: Int,
    val completedMilestones: Int,
    val riskReasonEnglish: String,
    val riskReasonArabic: String,
    val suggestedActionEnglish: String,
    val suggestedActionArabic: String
)

data class HabitRestructuringSuggestion(
    val habitId: Long,
    val habitTitle: String,
    val type: RestructuringType,
    val titleEnglish: String,
    val titleArabic: String,
    val descriptionEnglish: String,
    val descriptionArabic: String,
    val suggestedAnchorHabitTitle: String? = null,
    val suggestedNewDurationMinutes: Int? = null,
    val suggestedNewReminderMinutes: Int? = null
)

data class FailingHabitInsight(
    val habit: HabitEntity,
    val consecutiveMissedDays: Int,
    val consistencyPercent: Int,
    val restructuringSuggestion: HabitRestructuringSuggestion
)

data class PostponedTaskInsight(
    val task: TaskEntity,
    val ageDays: Int,
    val recommendationEnglish: String,
    val recommendationArabic: String
)

data class WorkloadWarning(
    val totalPendingHours: Double,
    val availableHours: Double,
    val warningEnglish: String,
    val warningArabic: String
)

data class NextBestAction(
    val titleEnglish: String,
    val titleArabic: String,
    val descriptionEnglish: String,
    val descriptionArabic: String,
    val actionType: RecommendationActionType,
    val targetTaskId: Long? = null,
    val targetHabitId: Long? = null
)

data class GoalHabitIntelligenceReport(
    val onTrackGoals: List<GoalEntity>,
    val atRiskGoals: List<GoalRiskAssessment>,
    val missedHabitsToday: List<HabitEntity>,
    val failingHabits: List<FailingHabitInsight>,
    val repeatedlyPostponedTasks: List<PostponedTaskInsight>,
    val workloadWarning: WorkloadWarning?,
    val nextBestActions: List<NextBestAction>
)

data class SmartInsightItem(
    val iconName: String,
    val titleEnglish: String,
    val titleArabic: String,
    val detailEnglish: String,
    val detailArabic: String,
    val category: InsightCategory
)

data class PersonalizationInsights(
    val actualVsEstimatedRatio: Double,
    val peakProductivityWindow: String,
    val completionRateSevenDays: Int,
    val planningAccuracyPercent: Int,
    val topStallingCategory: String?,
    val smartInsights: List<SmartInsightItem>
)

data class DailyScoreResult(
    val totalScore: Int,
    val tier: DailyScoreTier,
    val taskCompletionPoints: Int,
    val habitConsistencyPoints: Int,
    val focusMinutesPoints: Int,
    val scheduleDisciplinePoints: Int,
    val feedbackEnglish: String,
    val feedbackArabic: String
)

data class LifeEngineState(
    val recommendation: LifeRecommendation,
    val adaptivePlan: AdaptiveDayPlan,
    val rescuePlan: RescuePlan,
    val goalHabitReport: GoalHabitIntelligenceReport,
    val personalization: PersonalizationInsights,
    val dailyScore: DailyScoreResult,
    val isRescueModeActive: Boolean = false,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)
