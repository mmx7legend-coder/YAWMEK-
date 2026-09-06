package com.example.domain.ai

import com.example.data.local.model.*
import com.example.data.remote.AiDailyPlanItem

enum class ScheduleHealth {
    BALANCED, TIGHT, OVERLOADED
}

enum class PlanHealthStatus {
    HEALTHY, AT_RISK, BEHIND, CRITICAL, COMPLETED
}

data class RoadmapPhase(
    val title: String,
    val dayRange: String,
    val description: String
)

data class RoadmapMilestone(
    val title: String,
    val targetDayOffset: Int,
    val phaseTitle: String
)

data class RoadmapTask(
    val title: String,
    val durationMinutes: Int,
    val priority: Priority,
    val subtasks: List<String>,
    val targetDayOffset: Int
)

data class GoalRoadmapProposal(
    val goalTitle: String,
    val description: String,
    val category: String,
    val totalDays: Int,
    val phases: List<RoadmapPhase>,
    val milestones: List<RoadmapMilestone>,
    val tasks: List<RoadmapTask>,
    val recommendedHabitTitle: String,
    val recommendedHabitFrequency: HabitFrequency,
    val recoveryStrategy: String
)

data class TaskRescheduleItem(
    val task: TaskEntity,
    val newDueDateMillis: Long,
    val newDueTimeMinutes: Int?,
    val reason: String
)

data class RescheduleProposal(
    val items: List<TaskRescheduleItem>,
    val explanation: String,
    val totalOverdueCount: Int,
    val resolvedTodayCount: Int,
    val scheduledTomorrowCount: Int
)

data class ProductivityReport(
    val periodName: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val completionRatePercent: Int,
    val focusMinutesTotal: Int,
    val habitConsistencyScore: Int,
    val planHealth: PlanHealthStatus,
    val healthExplanation: String,
    val rootCauseBottleneck: String,
    val correctiveActions: List<String>
)

data class HabitInsight(
    val habitTitle: String,
    val currentStreak: Int,
    val consistencyPercent: Int,
    val recommendation: String,
    val habitStackAnchor: String? = null
)

sealed class StructuredAiAction {
    data class DailyScheduleAction(
        val planItems: List<AiDailyPlanItem>
    ) : StructuredAiAction()

    data class GoalRoadmapAction(
        val roadmap: GoalRoadmapProposal
    ) : StructuredAiAction()

    data class RescheduleAction(
        val proposal: RescheduleProposal
    ) : StructuredAiAction()

    data class HabitStackAction(
        val newHabitTitle: String,
        val anchorHabitTitle: String,
        val explanation: String
    ) : StructuredAiAction()

    data class FocusSessionAction(
        val taskTitle: String,
        val durationMinutes: Int,
        val reason: String
    ) : StructuredAiAction()
}

data class AiUserMemoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val key: String,
    val value: String,
    val category: String, // e.g. "Work Habits", "Peak Hours", "Focus Style"
    val timestamp: Long = System.currentTimeMillis()
)

data class AiEngineResult(
    val messageText: String,
    val suggestedPlan: List<AiDailyPlanItem> = emptyList(),
    val structuredAction: StructuredAiAction? = null,
    val productivityReport: ProductivityReport? = null,
    val planHealth: PlanHealthStatus? = null,
    val isFromRemoteAi: Boolean = false
)
