package com.example.domain.ai

import com.example.data.local.model.*
import java.time.LocalDate
import java.time.ZoneId

object ProductivityAnalystEngine {

    fun generateProductivityReport(
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        habitLogs: List<HabitLogEntity>,
        goals: List<GoalEntity>,
        milestones: List<MilestoneEntity>,
        focusSessions: List<FocusSessionEntity>,
        isArabic: Boolean = false
    ): ProductivityReport {
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.isCompleted }
        val completionRate = if (totalTasks > 0) ((completedTasks.toDouble() / totalTasks) * 100).toInt() else 100

        val past7DaysMillis = System.currentTimeMillis() - (7 * 86400000L)
        val weeklyFocusMinutes = focusSessions
            .filter { it.startedAtMillis >= past7DaysMillis }
            .sumOf { it.actualSeconds / 60 }

        // Habit consistency
        val habitInsights = HabitIntelligenceEngine.analyzeHabits(habits, habitLogs, isArabic)
        val avgHabitScore = if (habitInsights.isNotEmpty()) habitInsights.sumOf { it.consistencyPercent } / habitInsights.size else 85

        // Frequently postponed tasks (overdue or age > 5 days still pending)
        val nowMillis = System.currentTimeMillis()
        val postponedTasks = tasks.filter {
            !it.isCompleted && (
                (it.dueDateMillis != null && it.dueDateMillis < nowMillis - 86400000L) ||
                (nowMillis - it.createdAtMillis > 5 * 86400000L)
            )
        }

        // Plan & Goal Health evaluation
        val planHealth = evaluateOverallPlanHealth(goals, milestones, tasks)

        val healthExplanation = when (planHealth) {
            PlanHealthStatus.HEALTHY -> {
                if (isArabic) "جميع أهدافك وجدولك في مسار إيجابي وصحي بدون أي اختناقات خطيرة."
                else "All active milestones and daily schedules are progressing on track with low risk."
            }
            PlanHealthStatus.AT_RISK -> {
                if (isArabic) "هناك انخفاض طفيف في معدل الإنجاز مقابل الوقت المنقضي، انتبه لتراكم المهام المؤجلة."
                else "Mild execution drag detected: remaining time is narrowing faster than current milestone velocity."
            }
            PlanHealthStatus.BEHIND -> {
                if (isArabic) "أنت متأخر عن المواعيد النهائية المحددة لبعض المراحل. تحتاج إلى تعديل سرعة العمل أو إعادة الجدولة."
                else "Falling behind target milestone dates. Active workload exceeds current weekly completion rate."
            }
            PlanHealthStatus.CRITICAL -> {
                if (isArabic) "حالة حرجة: تراكم كبير في المهام المتأخرة وتوقف في تقدم الأهداف الرئيسية. يتطلب تدخلاً فورياً!"
                else "Critical status: Multiple overdue deadlines and blocked milestones. Immediate triage required."
            }
            PlanHealthStatus.COMPLETED -> {
                if (isArabic) "ممتاز! تم إنجاز جميع الأهداف والمراحل المسجلة بنجاح باهر."
                else "Outstanding! All recorded objectives and milestones have been fully accomplished."
            }
        }

        val rootCause = when {
            postponedTasks.size >= 3 -> {
                if (isArabic) "العائق الرئيسي: تراكم ${postponedTasks.size} مهام قديمة غير محسومة يسبب عبئاً ذهنياً ومماطلة."
                else "Primary Bottleneck: Backlog of ${postponedTasks.size} postponed tasks creating cognitive friction and resistance."
            }
            weeklyFocusMinutes < 60 && tasks.any { !it.isCompleted && it.priority == Priority.HIGH } -> {
                if (isArabic) "العائق الرئيسي: نقص في فترات التركيز العميق (Focus Blocks) للمهام الكبيرة ذات الأولوية."
                else "Primary Bottleneck: Insufficient deep focus blocks allocated for high-impact priorities."
            }
            tasks.count { !it.isCompleted && it.durationMinutes > 90 } > 0 -> {
                if (isArabic) "العائق الرئيسي: وجود مهام ضخمة غير مجزأة (> ٩٠ دقيقة) تزيد من احتمالية التسويف."
                else "Primary Bottleneck: Oversized tasks (>90 mins) without subtasks triggering procrastination."
            }
            else -> {
                if (isArabic) "الوضع العام متزن، والتحدي الأكبر هو الحفاظ على الالتزام بالعادات اليومية في عطلة نهاية الأسبوع."
                else "System is balanced; primary leverage point is sustaining habit consistency through weekends."
            }
        }

        val correctiveActions = if (isArabic) {
            listOf(
                "ابدأ اليوم بجلسة تركيز واحدة مدتها ٤٥ دقيقة على أهم مهمة معلقة.",
                "قسّم أي مهمة تتجاوز ساعة إلى خطوتين أو ثلاث خطوات محددة.",
                "استخدم ميزة إعادة الجدولة الذكية لتوزيع المهام المؤجلة دون ضغط."
            )
        } else {
            listOf(
                "Launch a 45-minute deep focus session on your single highest-impact priority.",
                "Deconstruct any task over 60 minutes into 2-3 bite-sized subtasks.",
                "Apply intelligent rescheduling to disperse overdue items without overcrowding tomorrow."
            )
        }

        return ProductivityReport(
            periodName = if (isArabic) "التقرير الشامل للأداء" else "Comprehensive Productivity Audit",
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            completionRatePercent = completionRate,
            focusMinutesTotal = weeklyFocusMinutes,
            habitConsistencyScore = avgHabitScore,
            planHealth = planHealth,
            healthExplanation = healthExplanation,
            rootCauseBottleneck = rootCause,
            correctiveActions = correctiveActions
        )
    }

    private fun evaluateOverallPlanHealth(
        goals: List<GoalEntity>,
        milestones: List<MilestoneEntity>,
        tasks: List<TaskEntity>
    ): PlanHealthStatus {
        if (goals.isEmpty() && tasks.isEmpty()) return PlanHealthStatus.HEALTHY

        val overdueTasksCount = tasks.count {
            val due = it.dueDateMillis
            val now = System.currentTimeMillis()
            !it.isCompleted && due != null && due < (now - 86400000L)
        }

        val pendingGoals = goals.filter { !it.isCompleted }
        if (pendingGoals.isEmpty() && tasks.all { it.isCompleted }) {
            return PlanHealthStatus.COMPLETED
        }

        val now = System.currentTimeMillis()
        val overdueGoals = pendingGoals.count { it.targetDateMillis != null && it.targetDateMillis < now }

        return when {
            overdueGoals > 0 || overdueTasksCount >= 5 -> PlanHealthStatus.CRITICAL
            overdueTasksCount in 2..4 -> PlanHealthStatus.BEHIND
            overdueTasksCount == 1 -> PlanHealthStatus.AT_RISK
            else -> PlanHealthStatus.HEALTHY
        }
    }
}
