package com.example.domain.life

import com.example.data.local.model.*
import java.time.LocalDate
import java.time.ZoneId

object GoalHabitIntelligence {

    fun analyze(
        goals: List<GoalEntity>,
        milestones: List<MilestoneEntity>,
        habits: List<HabitEntity>,
        habitLogs: List<HabitLogEntity>,
        tasks: List<TaskEntity>,
        isArabic: Boolean = false
    ): GoalHabitIntelligenceReport {
        val today = LocalDate.now()
        val todayEpochDay = today.toEpochDay()
        val nowMillis = System.currentTimeMillis()

        // 1. Goal Analysis: On-track vs At-risk
        val onTrackGoals = mutableListOf<GoalEntity>()
        val atRiskGoals = mutableListOf<GoalRiskAssessment>()

        val activeGoals = goals.filter { !it.isCompleted }
        for (goal in activeGoals) {
            val goalMilestones = milestones.filter { it.goalId == goal.id }
            val completedCount = goalMilestones.count { it.isCompleted }
            val totalMilestones = goalMilestones.size
            val pendingCount = totalMilestones - completedCount

            val targetDate = goal.targetDateMillis
            val daysRemaining = if (targetDate != null) {
                ((targetDate - nowMillis) / (86400000L)).toInt()
            } else {
                30
            }

            // A goal is at risk if deadline is passed OR pending milestones exceed available days (assuming ~3 days min per milestone)
            val isAtRisk = (daysRemaining <= 0 && pendingCount > 0) || (daysRemaining < (pendingCount * 2) && pendingCount > 1)

            if (isAtRisk) {
                val riskReasonEn = if (daysRemaining <= 0) {
                    "Target date reached with $pendingCount pending milestones remaining."
                } else {
                    "Only $daysRemaining days left for $pendingCount milestones (velocity deficit detected)."
                }

                val riskReasonAr = if (daysRemaining <= 0) {
                    "حل موعد الهدف وما زالت هناك $pendingCount مراحل غير مكتملة."
                } else {
                    "متبقي $daysRemaining يوماً فقط لإنجاز $pendingCount مراحل (معدل الإنجاز أقل من المطلوب)."
                }

                val actionEn = "Schedule a dedicated 45-min milestone sprint today for '${goalMilestones.firstOrNull { !it.isCompleted }?.title ?: goal.title}'."
                val actionAr = "خصص جلسة تركيز ٤٥ دقيقة اليوم لإنجاز مرحلة '${goalMilestones.firstOrNull { !it.isCompleted }?.title ?: goal.title}'."

                atRiskGoals.add(
                    GoalRiskAssessment(
                        goal = goal,
                        daysRemaining = daysRemaining,
                        totalMilestones = totalMilestones,
                        completedMilestones = completedCount,
                        riskReasonEnglish = riskReasonEn,
                        riskReasonArabic = riskReasonAr,
                        suggestedActionEnglish = actionEn,
                        suggestedActionArabic = actionAr
                    )
                )
            } else {
                onTrackGoals.add(goal)
            }
        }

        // 2. Habit Intelligence & Restructuring
        val past30DaysEpoch = todayEpochDay - 30
        val completedTodayHabitIds = habitLogs
            .filter { it.dateEpochDay == todayEpochDay && it.isCompleted }
            .map { it.habitId }
            .toSet()

        val missedHabitsToday = habits.filter { !completedTodayHabitIds.contains(it.id) }
        val failingHabits = mutableListOf<FailingHabitInsight>()

        // Find best anchor habit (highest consistency in past 30 days)
        val anchorHabitCandidate = habits.maxByOrNull { habit ->
            habitLogs.count { it.habitId == habit.id && it.isCompleted && it.dateEpochDay >= past30DaysEpoch }
        }

        for (habit in habits) {
            val logs = habitLogs.filter { it.habitId == habit.id && it.dateEpochDay >= past30DaysEpoch }
            val completedDays = logs.filter { it.isCompleted }.map { it.dateEpochDay }.toSet()

            // Calculate consecutive missed days
            var missedDaysStreak = 0
            var checkDay = todayEpochDay - 1
            while (!completedDays.contains(checkDay) && checkDay >= todayEpochDay - 14) {
                missedDaysStreak++
                checkDay--
            }

            val consistency = if (logs.isNotEmpty()) {
                ((completedDays.size.toDouble() / 30.0) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }

            // Failing habit criteria: missed >= 3 days in a row OR consistency < 35%
            if (missedDaysStreak >= 3 || (logs.isNotEmpty() && consistency < 35)) {
                val restructuring = when {
                    // Stacking opportunity
                    anchorHabitCandidate != null && anchorHabitCandidate.id != habit.id -> {
                        HabitRestructuringSuggestion(
                            habitId = habit.id,
                            habitTitle = habit.title,
                            type = RestructuringType.ATOMIC_STACK,
                            titleEnglish = "Atomic Stacking: Anchor to '${anchorHabitCandidate.title}'",
                            titleArabic = "تكديس العادات: اربطها بعادة '${anchorHabitCandidate.title}'",
                            descriptionEnglish = "Anchor '${habit.title}' immediately after '${anchorHabitCandidate.title}' to utilize existing neural momentum.",
                            descriptionArabic = "نفّذ '${habit.title}' مباشرة بعد '${anchorHabitCandidate.title}' للاستفادة من الزخم العصبي الراسخ.",
                            suggestedAnchorHabitTitle = anchorHabitCandidate.title
                        )
                    }
                    // 2-Minute Rule micro-habit reduction
                    else -> {
                        HabitRestructuringSuggestion(
                            habitId = habit.id,
                            habitTitle = habit.title,
                            type = RestructuringType.REDUCE_DURATION,
                            titleEnglish = "Micro-Habit Reset: 5-Minute Starter Rule",
                            titleArabic = "قاعدة الدقائق الخمس: تقليص العادة لتثبيتها",
                            descriptionEnglish = "Reduce target duration to just 5 minutes for the next 7 days to remove friction and restore daily identity.",
                            descriptionArabic = "قلّص وقت العادة إلى ٥ دقائق فقط لمدة أسبوع لإلغاء المقاومة الذهنية واستعادة الاستمرارية.",
                            suggestedNewDurationMinutes = 5
                        )
                    }
                }

                failingHabits.add(
                    FailingHabitInsight(
                        habit = habit,
                        consecutiveMissedDays = missedDaysStreak,
                        consistencyPercent = consistency,
                        restructuringSuggestion = restructuring
                    )
                )
            }
        }

        // 3. Repeatedly Postponed Tasks & Cognitive Friction
        val repeatedlyPostponed = mutableListOf<PostponedTaskInsight>()
        val pendingTasks = tasks.filter { !it.isCompleted }
        for (task in pendingTasks) {
            val ageDays = ((nowMillis - task.createdAtMillis) / (86400000L)).toInt()
            val isOverdueDays = if (task.dueDateMillis != null && task.dueDateMillis < nowMillis) {
                ((nowMillis - task.dueDateMillis) / (86400000L)).toInt()
            } else 0

            if (ageDays >= 4 || isOverdueDays >= 2) {
                val recEn = "Break '${task.title}' into a 15-min starter subtask or delete if no longer aligned with goals."
                val recAr = "قسّم '${task.title}' إلى خطوة افتتاحية لمدة ١٥ دقيقة أو احذفها إذا لم تعد مرتبطة بأهدافك."
                repeatedlyPostponed.add(
                    PostponedTaskInsight(
                        task = task,
                        ageDays = ageDays.coerceAtLeast(isOverdueDays),
                        recommendationEnglish = recEn,
                        recommendationArabic = recAr
                    )
                )
            }
        }

        // 4. Unrealistic Workload Warning
        val totalPendingMinutes = pendingTasks.sumOf { if (it.durationMinutes > 0) it.durationMinutes else 30 }
        val totalPendingHours = totalPendingMinutes / 60.0
        val availableHoursToday = 8.0

        val workloadWarning = if (totalPendingHours > availableHoursToday + 1.5) {
            WorkloadWarning(
                totalPendingHours = totalPendingHours,
                availableHours = availableHoursToday,
                warningEnglish = "Unrealistic load: ${String.format("%.1f", totalPendingHours)} hours of pending tasks exceed standard daily cognitive limit (8 hrs). Risk of decision fatigue is extreme.",
                warningArabic = "حمل غير واقعي: ${String.format("%.1f", totalPendingHours)} ساعات من المهام تتجاوز الحد اليومي المعقول (٨ ساعات). خطر الإرهاق الذهني مرتفع جداً."
            )
        } else null

        // 5. Next Best Action Generation
        val nextActions = mutableListOf<NextBestAction>()

        // Highest priority: At-risk goal milestone
        atRiskGoals.firstOrNull()?.let { risk ->
            nextActions.add(
                NextBestAction(
                    titleEnglish = "Protect Milestone: ${risk.goal.title}",
                    titleArabic = "حماية مرحلة: ${risk.goal.title}",
                    descriptionEnglish = risk.suggestedActionEnglish,
                    descriptionArabic = risk.suggestedActionArabic,
                    actionType = RecommendationActionType.TASK
                )
            )
        }

        // Failing habit restructuring action
        failingHabits.firstOrNull()?.let { failing ->
            nextActions.add(
                NextBestAction(
                    titleEnglish = failing.restructuringSuggestion.titleEnglish,
                    titleArabic = failing.restructuringSuggestion.titleArabic,
                    descriptionEnglish = failing.restructuringSuggestion.descriptionEnglish,
                    descriptionArabic = failing.restructuringSuggestion.descriptionArabic,
                    actionType = RecommendationActionType.HABIT,
                    targetHabitId = failing.habit.id
                )
            )
        }

        return GoalHabitIntelligenceReport(
            onTrackGoals = onTrackGoals,
            atRiskGoals = atRiskGoals,
            missedHabitsToday = missedHabitsToday,
            failingHabits = failingHabits,
            repeatedlyPostponedTasks = repeatedlyPostponed,
            workloadWarning = workloadWarning,
            nextBestActions = nextActions
        )
    }
}
