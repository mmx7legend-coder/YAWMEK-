package com.example.domain.ai

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.model.*
import com.example.data.remote.*
import com.example.domain.SmartRecommendationEngine
import com.example.domain.calendar.CalendarEventItem
import com.example.domain.money.FinancialOverview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class OrchestratorContext(
    val context: Context,
    val userName: String,
    val language: AppLanguage,
    val currency: String,
    val tasks: List<TaskEntity>,
    val habits: List<HabitEntity>,
    val habitLogs: List<HabitLogEntity>,
    val goals: List<GoalEntity>,
    val milestones: List<MilestoneEntity>,
    val focusSessions: List<FocusSessionEntity>,
    val calendarEvents: List<CalendarEventItem> = emptyList(),
    val financialOverview: FinancialOverview? = null,
    val workStartHour: Int = 9,
    val workEndHour: Int = 18
)

object YawmekAiOrchestrator {

    suspend fun processPrompt(
        prompt: String,
        ctx: OrchestratorContext
    ): AiEngineResult = withContext(Dispatchers.IO) {
        val isArabic = ctx.language == AppLanguage.ARABIC || containsArabic(prompt)
        val lower = prompt.lowercase().trim()

        // 1. Goal / Roadmap generation check
        if (isGoalRoadmapQuery(lower, prompt)) {
            val roadmap = GoalRoadmapGenerator.generateRoadmap(prompt, isArabic)
            val msg = buildRoadmapMessage(roadmap, isArabic)
            return@withContext AiEngineResult(
                messageText = msg,
                structuredAction = StructuredAiAction.GoalRoadmapAction(roadmap),
                isFromRemoteAi = false
            )
        }

        // 2. Rescheduling check
        if (isRescheduleQuery(lower, prompt)) {
            val proposal = PriorityAndReschedulingEngine.generateIntelligentReschedule(
                tasks = ctx.tasks,
                workStartHour = ctx.workStartHour,
                workEndHour = ctx.workEndHour,
                isArabic = isArabic
            )
            return@withContext AiEngineResult(
                messageText = proposal.explanation,
                structuredAction = if (proposal.items.isNotEmpty()) StructuredAiAction.RescheduleAction(proposal) else null,
                isFromRemoteAi = false
            )
        }

        // 3. Productivity analysis / "Why am I falling behind?" check
        if (isProductivityAnalysisQuery(lower, prompt)) {
            val report = ProductivityAnalystEngine.generateProductivityReport(
                tasks = ctx.tasks,
                habits = ctx.habits,
                habitLogs = ctx.habitLogs,
                goals = ctx.goals,
                milestones = ctx.milestones,
                focusSessions = ctx.focusSessions,
                isArabic = isArabic
            )
            val msg = buildProductivityReportMessage(report, isArabic)
            return@withContext AiEngineResult(
                messageText = msg,
                productivityReport = report,
                planHealth = report.planHealth,
                isFromRemoteAi = false
            )
        }

        // 4. Daily planning check ("Plan my day", "خططلي يومي", "عندي ساعتين و 5 مهام")
        if (isDailyPlanQuery(lower, prompt)) {
            val (planText, items) = SmartDailyPlanner.planDay(
                tasks = ctx.tasks,
                habits = ctx.habits,
                habitLogs = ctx.habitLogs,
                calendarEvents = ctx.calendarEvents,
                workStartHour = ctx.workStartHour,
                workEndHour = ctx.workEndHour,
                isArabic = isArabic
            )
            return@withContext AiEngineResult(
                messageText = planText,
                suggestedPlan = items,
                structuredAction = StructuredAiAction.DailyScheduleAction(items),
                isFromRemoteAi = false
            )
        }

        // 5. "What should I do now?" check
        if (isWhatShouldIDoNowQuery(lower, prompt)) {
            val rec = SmartRecommendationEngine.recommendNextAction(
                tasks = ctx.tasks,
                habits = ctx.habits,
                habitLogs = ctx.habitLogs,
                calendarEvents = ctx.calendarEvents,
                workStartHour = ctx.workStartHour,
                workEndHour = ctx.workEndHour
            )
            val msg = if (isArabic) {
                "🎯 التوصية الفورية الذكية ليومك الآن:\n\n" +
                        "• ${rec.title}: ${rec.subtitle}\n" +
                        "• السبب والجدوى: ${rec.reasonArabic}\n" +
                        "• الوقت المقدر: ${rec.estimatedDurationMinutes} دقيقة.\n" +
                        "• وضع التقويم: ${rec.freeTimeMinutesAvailable} دقيقة متاحة قبل الموعد القادم.\n\n" +
                        "💡 نصيحة مدرب يومك: اضغط على زر (درع التركيز) وابدأ بجلسة بومودورو سريعة لإتمامها بدون مقاطعة."
            } else {
                "🎯 Smart Immediate Next Action:\n\n" +
                        "• ${rec.title}: ${rec.subtitle}\n" +
                        "• Reasoning: ${rec.reasonEnglish}\n" +
                        "• Estimated duration: ${rec.estimatedDurationMinutes} mins.\n" +
                        "• Schedule context: ${rec.freeTimeMinutesAvailable} mins open before your next appointment.\n\n" +
                        "💡 Coach Tip: Launch Focus Shield now with a 25-min Pomodoro to knock this out with full focus."
            }
            val focusAction = if (rec.task != null) {
                StructuredAiAction.FocusSessionAction(
                    taskTitle = rec.task.title,
                    durationMinutes = rec.estimatedDurationMinutes,
                    reason = if (isArabic) rec.reasonArabic else rec.reasonEnglish
                )
            } else null
            return@withContext AiEngineResult(
                messageText = msg,
                structuredAction = focusAction,
                isFromRemoteAi = false
            )
        }

        // 6. Habit Intelligence check
        if (isHabitQuery(lower, prompt)) {
            val insights = HabitIntelligenceEngine.analyzeHabits(ctx.habits, ctx.habitLogs, isArabic)
            val summary = HabitIntelligenceEngine.generateOverallHabitSummary(insights, isArabic)
            val stackCandidate = insights.firstOrNull { it.habitStackAnchor != null }
            val habitAction = if (stackCandidate != null) {
                StructuredAiAction.HabitStackAction(
                    newHabitTitle = stackCandidate.habitTitle,
                    anchorHabitTitle = stackCandidate.habitStackAnchor ?: "",
                    explanation = stackCandidate.recommendation
                )
            } else null
            return@withContext AiEngineResult(
                messageText = summary,
                structuredAction = habitAction,
                isFromRemoteAi = false
            )
        }

        // 7. Try Remote Gemini if API key available
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val memories = AiMemoryManager.getAllMemories(ctx.context).joinToString("; ") { "${it.key}: ${it.value}" }
                val systemPrompt = """
                    You are YAWMEK AI (مساعد يومك), an elite personal productivity intelligence engine.
                    User: ${ctx.userName.ifBlank { "User" }} | Language: ${if (isArabic) "Arabic" else "English"}
                    Pending Tasks (${ctx.tasks.count { !it.isCompleted }}): ${ctx.tasks.filter { !it.isCompleted }.take(5).joinToString { it.title }}
                    Habits: ${ctx.habits.take(5).joinToString { it.title }}
                    Financial: ${ctx.financialOverview?.let { "Net Worth: ${it.totalNetWorthMinor / 100}, Health: ${it.budgetHealthScore}/100" } ?: "Balanced"}
                    Learned Preferences: $memories
                    
                    Provide concrete, deeply practical, empathetic productivity guidance.
                    Never invent fake data. Speak with professional, empowering clarity.
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
                )
                val response = GeminiClient.api.generateContent(apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    return@withContext AiEngineResult(
                        messageText = text.trim(),
                        isFromRemoteAi = true
                    )
                }
            } catch (e: Exception) {
                Log.w("YawmekAiOrchestrator", "Remote Gemini fallback triggered", e)
            }
        }

        // 8. Default Local Fallback Engine (Personalized command center response)
        val defaultMsg = if (isArabic) {
            "أهلاً بك يا ${ctx.userName.ifBlank { "صديقي" }}! أنا محرك ذكاء يومك للإنتاجية الشخصية. 🧠✨\n\n" +
                    "أنا متصل مباشرة بكل مهامك (${ctx.tasks.count { !it.isCompleted }} معلقة)، عاداتك (${ctx.habits.size})، وأهدافك المسجلة.\n\n" +
                    "يمكنك طلبي للقيام بما يلي:\n" +
                    "• «خططلي يومي» — لإنشاء جدول زمني متوازن بفترات راحة وتركيز.\n" +
                    "• «أعمل إيه دلوقتي؟» — لأفضل قرار فوري بناءً على تقويمك وطاقتك.\n" +
                    "• «عايز أتعلم بايثون في 60 يوم» — لتفكيك هدفك إلى مراحل ومهام وعادات.\n" +
                    "• «حلل إنتاجيتي» أو «ليه أنا متأخر؟» — لكشف الاختناقات وتصحيح المسار.\n" +
                    "• «أعد جدولة المهام المتأخرة» — لتوزيع المهام دون إرهاق."
        } else {
            "Welcome back, ${ctx.userName.ifBlank { "friend" }}! I am your YAWMEK Personal Productivity Intelligence Engine. 🧠✨\n\n" +
                    "I am directly synced with your live workspace: ${ctx.tasks.count { !it.isCompleted }} pending tasks, ${ctx.habits.size} habits, and your target goals.\n\n" +
                    "Try asking me:\n" +
                    "• \"Plan my day\" — to generate a balanced time-blocked schedule with buffers.\n" +
                    "• \"What should I do now?\" — for the highest ROI immediate action.\n" +
                    "• \"I want to learn Python in 60 days\" — to transform goals into roadmaps & habits.\n" +
                    "• \"Analyze my productivity\" / \"Why am I falling behind?\" — for root-cause diagnosis.\n" +
                    "• \"Reschedule missed tasks\" — to intelligently redistribute overdue items."
        }

        AiEngineResult(messageText = defaultMsg, isFromRemoteAi = false)
    }

    private fun containsArabic(text: String): Boolean {
        return text.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' }
    }

    private fun isGoalRoadmapQuery(lower: String, raw: String): Boolean {
        return lower.contains("goal") || lower.contains("roadmap") || lower.contains("learn") ||
                lower.contains("python") || lower.contains("fitness") || lower.contains("business") ||
                raw.contains("هدف") || raw.contains("خطة") || raw.contains("أتعلم") || raw.contains("عايز") ||
                raw.contains("أريد") || raw.contains("مشروع") || raw.contains("بايثون")
    }

    private fun isRescheduleQuery(lower: String, raw: String): Boolean {
        return lower.contains("reschedule") || lower.contains("postpone") || lower.contains("move my") ||
                lower.contains("unfinished") || lower.contains("overdue") || raw.contains("جدولة") ||
                raw.contains("أجل") || raw.contains("متأخر") || raw.contains("تأخرت") || raw.contains("وزع")
    }

    private fun isProductivityAnalysisQuery(lower: String, raw: String): Boolean {
        return lower.contains("analy") || lower.contains("productivity") || lower.contains("why am i falling behind") ||
                lower.contains("behind") || lower.contains("performance") || lower.contains("weekly review") ||
                raw.contains("حلل") || raw.contains("إنتاجيتي") || raw.contains("تقرير") || raw.contains("ليه أنا متأخر") ||
                raw.contains("متأخر ليه") || raw.contains("تقييم")
    }

    private fun isDailyPlanQuery(lower: String, raw: String): Boolean {
        return lower.contains("plan my day") || lower.contains("daily plan") || lower.contains("plan my evening") ||
                lower.contains("schedule today") || lower.contains("organize my day") ||
                raw.contains("خططلي") || raw.contains("خطط ليومي") || raw.contains("جدول اليوم") ||
                raw.contains("رتب يومي") || raw.contains("ساعتين و")
    }

    private fun isWhatShouldIDoNowQuery(lower: String, raw: String): Boolean {
        return lower.contains("what should i do now") || lower.contains("what to do now") || lower.contains("what next") ||
                raw.contains("أعمل إيه دلوقتي") || raw.contains("إيه أعمل دلوقتي") || raw.contains("أعمل ايه دلوقتي") ||
                raw.contains("ماذا أفعل الآن") || raw.contains("ابدأ بإيه")
    }

    private fun isHabitQuery(lower: String, raw: String): Boolean {
        return lower.contains("habit") || lower.contains("streak") || raw.contains("عادة") || raw.contains("عاداتي") || raw.contains("سلسلة")
    }

    private fun buildRoadmapMessage(roadmap: GoalRoadmapProposal, isArabic: Boolean): String {
        return if (isArabic) {
            "🚀 خارطة الطريق الذكية للهدف: «${roadmap.goalTitle}»\n\n" +
                    "• المدة المقترحة: ${roadmap.totalDays} يوماً مقسمة على ٣ مراحل استراتيجية.\n" +
                    "• العادة الموصى بها لضمان الإنجاز: [${roadmap.recommendedHabitTitle}].\n" +
                    "• خطة الطوارئ والتعافي: ${roadmap.recoveryStrategy}\n\n" +
                    "المراحل والمحطات الرئيسية:\n" +
                    roadmap.phases.joinToString("\n") { p -> "📌 ${p.title} (${p.dayRange}): ${p.description}" } +
                    "\n\nيمكنك الضغط على الزر بالأسفل لإنشاء الهدف والمراحل والمهام والعادة التلقائية بنقرة واحدة!"
        } else {
            "🚀 Intelligent Goal Roadmap: «${roadmap.goalTitle}»\n\n" +
                    "• Target Timeline: ${roadmap.totalDays} days across 3 strategic phases.\n" +
                    "• Recommended Anchor Habit: [${roadmap.recommendedHabitTitle}].\n" +
                    "• Risk Recovery Strategy: ${roadmap.recoveryStrategy}\n\n" +
                    "Phases & Milestones:\n" +
                    roadmap.phases.joinToString("\n") { p -> "📌 ${p.title} (${p.dayRange}): ${p.description}" } +
                    "\n\nTap the action card below to generate the Goal, Milestones, starter Tasks, and daily Habit with 1 click!"
        }
    }

    private fun buildProductivityReportMessage(report: ProductivityReport, isArabic: Boolean): String {
        return if (isArabic) {
            "📊 تحليل الأداء الشامل وتشخيص الإنتاجية:\n\n" +
                    "• حالة الخطة الحالية: ${report.planHealth.name} ✨\n" +
                    "• نسبة إنجاز المهام: ${report.completionRatePercent}% (${report.completedTasks} من إجمالي ${report.totalTasks})\n" +
                    "• دقائق التركيز العميق (آخر ٧ أيام): ${report.focusMinutesTotal} دقيقة\n" +
                    "• مؤشر التزام العادات: ${report.habitConsistencyScore}%\n\n" +
                    "🔍 التشخيص الجذري:\n${report.rootCauseBottleneck}\n\n" +
                    "⚡ الخطوات التصحيحية الفورية:\n" +
                    report.correctiveActions.joinToString("\n") { "1. $it" }
        } else {
            "📊 Comprehensive Productivity Audit & Diagnosis:\n\n" +
                    "• Plan Health Status: ${report.planHealth.name} ✨\n" +
                    "• Task Completion Velocity: ${report.completionRatePercent}% (${report.completedTasks} of ${report.totalTasks} tasks)\n" +
                    "• Deep Focus Volume (past 7 days): ${report.focusMinutesTotal} mins\n" +
                    "• Habit Consistency Score: ${report.habitConsistencyScore}%\n\n" +
                    "🔍 Root Cause Analysis:\n${report.rootCauseBottleneck}\n\n" +
                    "⚡ Recommended Corrective Actions:\n" +
                    report.correctiveActions.joinToString("\n") { "• $it" }
        }
    }
}
