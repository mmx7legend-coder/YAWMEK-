package com.example

import com.example.data.local.model.*
import com.example.domain.ai.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class YawmekAiIntelligenceTest {

    @Test
    fun testPriorityScoringAndRanking() {
        val now = System.currentTimeMillis()
        val overdueTask = TaskEntity(
            id = 1,
            title = "Submit Report",
            priority = Priority.HIGH,
            dueDateMillis = now - 86400000L,
            durationMinutes = 60
        )
        val lowTask = TaskEntity(
            id = 2,
            title = "Read article",
            priority = Priority.LOW,
            dueDateMillis = now + 5 * 86400000L,
            durationMinutes = 20
        )

        val overdueScore = PriorityAndReschedulingEngine.calculatePriorityScore(overdueTask)
        val lowScore = PriorityAndReschedulingEngine.calculatePriorityScore(lowTask)

        // Urgent overdue task should have higher priority score
        assertTrue(overdueScore > lowScore)
    }

    @Test
    fun testReschedulingDistribution() {
        val now = System.currentTimeMillis()
        val overdueTasks = (1..3).map { idx ->
            TaskEntity(
                id = idx.toLong(),
                title = "Overdue $idx",
                priority = Priority.HIGH,
                dueDateMillis = now - 86400000L * idx,
                durationMinutes = 45
            )
        }

        val proposal = PriorityAndReschedulingEngine.generateIntelligentReschedule(
            tasks = overdueTasks,
            workStartHour = 9,
            workEndHour = 18,
            isArabic = false
        )

        assertEquals(3, proposal.items.size)
        // Check that new due dates are today or in the future
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        proposal.items.forEach { item ->
            assertTrue(item.newDueDateMillis >= todayStart)
            assertTrue(item.newDueTimeMinutes in 540..1080)
        }
    }

    @Test
    fun testGoalRoadmapGeneration() {
        val roadmap = GoalRoadmapGenerator.generateRoadmap(
            prompt = "Learn Python in 60 days",
            isArabic = false
        )

        assertTrue(roadmap.goalTitle.contains("Python", ignoreCase = true))
        assertEquals(60, roadmap.totalDays)
        assertTrue(roadmap.phases.isNotEmpty())
        assertTrue(roadmap.milestones.isNotEmpty())
        assertTrue(roadmap.tasks.isNotEmpty())
        assertTrue(roadmap.recommendedHabitTitle.isNotBlank())
        assertTrue(roadmap.recoveryStrategy.isNotBlank())
    }

    @Test
    fun testHabitStackingIntelligence() {
        val anchorHabit = HabitEntity(
            id = 10,
            title = "Morning Coffee",
            category = "Personal",
            frequency = HabitFrequency.DAILY
        )

        val insights = HabitIntelligenceEngine.analyzeHabits(
            habits = listOf(anchorHabit),
            logs = emptyList(),
            isArabic = false
        )

        assertEquals(1, insights.size)
        assertEquals("Morning Coffee", insights[0].habitTitle)
        assertTrue(insights[0].recommendation.isNotBlank())
    }

    @Test
    fun testProductivityReportGeneration() {
        val now = System.currentTimeMillis()
        val completedTask = TaskEntity(id = 1, title = "Done Task", isCompleted = true, completedAtMillis = now)
        val pendingTask = TaskEntity(id = 2, title = "Pending Task", isCompleted = false)

        val report = ProductivityAnalystEngine.generateProductivityReport(
            tasks = listOf(completedTask, pendingTask),
            habits = emptyList(),
            habitLogs = emptyList(),
            goals = emptyList(),
            milestones = emptyList(),
            focusSessions = emptyList(),
            isArabic = false
        )

        assertEquals(50, report.completionRatePercent)
        assertTrue(report.rootCauseBottleneck.isNotBlank())
        assertTrue(report.correctiveActions.isNotEmpty())
    }
}
