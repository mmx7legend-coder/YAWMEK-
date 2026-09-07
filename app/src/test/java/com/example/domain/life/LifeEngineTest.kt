package com.example.domain.life

import com.example.data.local.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class LifeEngineTest {

    @Test
    fun testPriorityEngineScoringAndRanking() {
        val now = System.currentTimeMillis()
        val overdueHighTask = TaskEntity(
            id = 1,
            title = "Overdue Client Report",
            priority = Priority.HIGH,
            dueDateMillis = now - 86400000L,
            durationMinutes = 45
        )
        val lowFutureTask = TaskEntity(
            id = 2,
            title = "Read hobby article",
            priority = Priority.LOW,
            dueDateMillis = now + (7 * 86400000L),
            durationMinutes = 15
        )

        val overdueScore = PriorityEngine.scoreTask(overdueHighTask, emptyList(), 120, 600, now)
        val futureScore = PriorityEngine.scoreTask(lowFutureTask, emptyList(), 120, 600, now)

        assertTrue("Overdue high priority task must score higher", overdueScore > futureScore)

        val ranked = PriorityEngine.rankTasks(listOf(lowFutureTask, overdueHighTask), emptyList(), 120, 600, now)
        assertEquals(overdueHighTask.id, ranked.first().first.id)
    }

    @Test
    fun testLifeEngineRecommendationWithTimeConstraint() {
        val now = System.currentTimeMillis()
        val longTask = TaskEntity(
            id = 1,
            title = "Write 20-page thesis chapter",
            priority = Priority.HIGH,
            durationMinutes = 90
        )
        val shortTask = TaskEntity(
            id = 2,
            title = "Quick email response",
            priority = Priority.MEDIUM,
            durationMinutes = 20
        )

        // When user asks "I only have 30 minutes", it should recommend the task that fits within 30 min
        val rec = LifeEngine.determineRecommendation(
            tasks = listOf(longTask, shortTask),
            habits = emptyList(),
            habitLogs = emptyList(),
            timeConstraintMinutes = 30
        )

        assertEquals("Quick email response", rec.title)
        assertEquals(20, rec.estimatedDurationMinutes)
        assertTrue(rec.expectedImpactEnglish.isNotBlank())
    }

    @Test
    fun testAdaptiveDayPlanGenerationAndUndo() {
        val task1 = TaskEntity(id = 1, title = "Task 1", durationMinutes = 45, priority = Priority.HIGH)
        val task2 = TaskEntity(id = 2, title = "Task 2", durationMinutes = 30, priority = Priority.MEDIUM)

        AdaptiveDayEngine.clearHistory()
        val plan1 = AdaptiveDayEngine.generateAdaptivePlan(
            tasks = listOf(task1, task2),
            habits = emptyList(),
            habitLogs = emptyList(),
            nowMinuteOfDay = 540 // 9:00 AM
        )

        assertEquals(2, plan1.plannedItems.size)
        assertEquals(75, plan1.totalScheduledMinutes)
        assertFalse(plan1.canUndo)

        // Recalculate with new task added
        val task3 = TaskEntity(id = 3, title = "Task 3", durationMinutes = 60, priority = Priority.HIGH)
        val plan2 = AdaptiveDayEngine.recalculateRemainingDay(
            tasks = listOf(task1, task2, task3),
            habits = emptyList(),
            habitLogs = emptyList(),
            currentPlan = plan1,
            nowMinuteOfDay = 600
        )

        assertEquals(3, plan2.plannedItems.size)
        assertTrue(plan2.canUndo)

        // Test Undo rollback
        val restoredPlan = AdaptiveDayEngine.undoPlanRecalculation()
        assertNotNull(restoredPlan)
        assertEquals(plan1.totalScheduledMinutes, restoredPlan?.totalScheduledMinutes)
    }

    @Test
    fun testRescueModeEmergencyTriage() {
        val now = System.currentTimeMillis()
        val urgentOverdue = TaskEntity(
            id = 1,
            title = "Urgent Tax Filing",
            priority = Priority.HIGH,
            dueDateMillis = now - 86400000L,
            durationMinutes = 60
        )
        val optionalTask = TaskEntity(
            id = 2,
            title = "Organize bookshelf",
            priority = Priority.LOW,
            durationMinutes = 90
        )

        val rescue = RescueModeEngine.generateRescuePlan(
            tasks = listOf(urgentOverdue, optionalTask),
            workEndHour = 18,
            nowMinuteOfDay = 1000 // late afternoon
        )

        assertTrue(rescue.mustDoTasks.any { it.task.id == 1L })
        assertTrue(rescue.optionalTasks.any { it.task.id == 2L })
        assertEquals(RescueTier.MUST_DO, rescue.mustDoTasks.first().tier)
        assertEquals(RescueTier.OPTIONAL, rescue.optionalTasks.first().tier)
        assertTrue(rescue.minutesSavedByPostponing >= 90)
    }

    @Test
    fun testGoalHabitIntelligenceAtRiskAndRestructuring() {
        val now = System.currentTimeMillis()
        val atRiskGoal = GoalEntity(
            id = 1,
            title = "Launch SaaS",
            targetDateMillis = now + (2 * 86400000L) // 2 days left
        )
        val milestone1 = MilestoneEntity(id = 1, goalId = 1, title = "Finish backend", isCompleted = false)
        val milestone2 = MilestoneEntity(id = 2, goalId = 1, title = "Deploy frontend", isCompleted = false)

        val failingHabit = HabitEntity(id = 10, title = "Gym Workout", category = "Health")
        val anchorHabit = HabitEntity(id = 11, title = "Morning Coffee", category = "Personal")

        val todayEpoch = LocalDate.now().toEpochDay()
        // Anchor habit has 20 completed logs in past 30 days
        val anchorLogs = (1..20).map { offset ->
            HabitLogEntity(id = offset.toLong(), habitId = 11, dateEpochDay = todayEpoch - offset, isCompleted = true)
        }

        val report = GoalHabitIntelligence.analyze(
            goals = listOf(atRiskGoal),
            milestones = listOf(milestone1, milestone2),
            habits = listOf(failingHabit, anchorHabit),
            habitLogs = anchorLogs,
            tasks = emptyList()
        )

        assertEquals(1, report.atRiskGoals.size)
        assertEquals("Launch SaaS", report.atRiskGoals.first().goal.title)
        assertTrue(report.failingHabits.any { it.habit.id == 10L })
        val restructuring = report.failingHabits.first { it.habit.id == 10L }.restructuringSuggestion
        assertNotNull(restructuring)
        assertTrue(restructuring.suggestedAnchorHabitTitle?.contains("Morning Coffee") == true || restructuring.type == RestructuringType.REDUCE_DURATION)
    }

    @Test
    fun testPersonalizationInsightsAndVelocityRatio() {
        val sessions = listOf(
            FocusSessionEntity(id = 1, durationMinutes = 25, actualSeconds = 1800), // 30 min actual (20% over)
            FocusSessionEntity(id = 2, durationMinutes = 25, actualSeconds = 1800),
            FocusSessionEntity(id = 3, durationMinutes = 50, actualSeconds = 3600)  // 60 min actual (20% over)
        )

        val insights = PersonalizationEngine.analyzePatterns(
            tasks = emptyList(),
            focusSessions = sessions,
            habits = emptyList(),
            habitLogs = emptyList()
        )

        assertTrue(insights.actualVsEstimatedRatio >= 1.1)
        assertTrue(insights.smartInsights.isNotEmpty())
        assertTrue(insights.smartInsights.any { it.category == InsightCategory.VELOCITY })
    }

    @Test
    fun testDailyScoreCalculatorTiersAndSignals() {
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // User completed all their tasks today and reached focus target
        val doneTasks = listOf(
            TaskEntity(id = 1, title = "Task 1", dueDateMillis = todayStart + 1000, isCompleted = true, completedAtMillis = todayStart + 2000),
            TaskEntity(id = 2, title = "Task 2", dueDateMillis = todayStart + 1000, isCompleted = true, completedAtMillis = todayStart + 3000)
        )
        val focusSessions = listOf(
            FocusSessionEntity(id = 1, startedAtMillis = todayStart + 1000, actualSeconds = 3600) // 60 min
        )

        val scoreResult = DailyScoreCalculator.calculateDailyScore(
            tasks = doneTasks,
            habits = emptyList(),
            habitLogs = emptyList(),
            focusSessions = focusSessions,
            dailyFocusTargetMinutes = 60
        )

        assertTrue("Total score should be high for 100% completion & focus target", scoreResult.totalScore >= 75)
        assertTrue(scoreResult.tier == DailyScoreTier.MASTERY || scoreResult.tier == DailyScoreTier.MOMENTUM)
        assertEquals(35, scoreResult.taskCompletionPoints)
        assertEquals(25, scoreResult.focusMinutesPoints)
        assertTrue(scoreResult.feedbackEnglish.isNotBlank())
    }
}
