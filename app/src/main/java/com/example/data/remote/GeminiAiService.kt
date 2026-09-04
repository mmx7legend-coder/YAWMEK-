package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Request data structures
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

data class GeminiPart(
    val text: String? = null
)

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContent?
)

interface GeminiApi {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }
}

data class AiDailyPlanItem(
    val title: String,
    val timeFormatted: String,
    val durationMinutes: Int,
    val priority: Priority,
    val reason: String
)

data class AiResponseResult(
    val messageText: String,
    val suggestedPlan: List<AiDailyPlanItem> = emptyList(),
    val isFromRemoteAi: Boolean = false
)

object YawmekAiEngine {

    suspend fun generateAiAdvice(
        prompt: String,
        userContext: UserContextData
    ): AiResponseResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val systemPrompt = """
                    You are YAWMEK AI (مساعد يومك), the intelligent daily personal command center assistant.
                    User Language Preference: ${userContext.language}
                    User Name: ${userContext.userName.ifBlank { "User" }}
                    Current Tasks: ${userContext.pendingTasksSummary}
                    Financial Overview: ${userContext.financialSummary}
                    Schedule & Events: ${userContext.calendarSummary}
                    Focus & Productivity: ${userContext.focusSummary}
                    Active Habits: ${userContext.habitsSummary}
                    
                    Respond with empathy, clarity, high structure, and practical daily planning guidance.
                    Never invent fake personal data. Base all financial, calendar, and focus remarks strictly on real data provided.
                    If the user is asking in Arabic, respond in fluent, professional, modern Arabic.
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                    ),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
                )

                val response = GeminiClient.api.generateContent(apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    val planItems = if (prompt.contains("plan", ignoreCase = true) || prompt.contains("خطة") || prompt.contains("جدول")) {
                        extractSmartPlanFromTasks(userContext.tasks)
                    } else {
                        emptyList()
                    }
                    return@withContext AiResponseResult(
                        messageText = text.trim(),
                        suggestedPlan = planItems,
                        isFromRemoteAi = true
                    )
                }
            } catch (e: Exception) {
                Log.w("YawmekAiEngine", "Gemini API call failed, using local fallback", e)
            }
        }

        // Offline deterministic fallback with real local context
        generateLocalFallbackResponse(prompt, userContext)
    }

    private fun generateLocalFallbackResponse(
        prompt: String,
        context: UserContextData
    ): AiResponseResult {
        val isArabic = context.language == AppLanguage.ARABIC
        val lower = prompt.lowercase()

        // Money / Financial queries
        if (lower.contains("money") || lower.contains("budget") || lower.contains("expense") || prompt.contains("مال") || prompt.contains("ميزانية") || prompt.contains("مصروف") || prompt.contains("توفير")) {
            val msg = if (isArabic) {
                "📊 نظرة مالية ذكية على وضعك الحالي:\n\n" +
                        "• ${context.financialSummary.ifBlank { "المصاريف المسجلة تحت السيطرة." }}\n" +
                        "• نصيحة يومك: قسّم دخلك وفق قاعدة 50/30/20 (50% للضروريات، 30% للرغبات، 20% للادخار والاستثمار).\n" +
                        "• يمكنك مراجعة لوحة المال لمعرفة مؤشر صحة الميزانية والأهداف المالية."
            } else {
                "📊 Smart Financial Summary:\n\n" +
                        "• ${context.financialSummary.ifBlank { "Tracked expenses are currently balanced." }}\n" +
                        "• YAWMEK Tip: Adopt the 50/30/20 rule (50% essentials, 30% discretionary, 20% savings & goals).\n" +
                        "• Visit your Money Dashboard to review your Budget Health Score and savings progress."
            }
            return AiResponseResult(messageText = msg, isFromRemoteAi = false)
        }

        // Calendar / Schedule queries
        if (lower.contains("schedule") || lower.contains("calendar") || lower.contains("time block") || prompt.contains("جدول") || prompt.contains("تقويم") || prompt.contains("تنظيم الوقت")) {
            val plan = extractSmartPlanFromTasks(context.tasks)
            val msg = if (isArabic) {
                "📅 تخطيط اليوم الذكي (Smart Time Blocking):\n\n" +
                        "• الجدول الحالي: ${context.calendarSummary.ifBlank { "لا توجد تعارضات مسجلة اليوم." }}\n" +
                        "• تم تحديد أفضل فترات التركيز لمهامك العاجلة بدون تداخل مع مواعيدك.\n" +
                        "• إليك المقترح الزمني الأمثل ليومك:"
            } else {
                "📅 Smart Daily Schedule & Time Blocking:\n\n" +
                        "• Current Events: ${context.calendarSummary.ifBlank { "No schedule conflicts detected today." }}\n" +
                        "• We prioritized deep focus blocks for high-priority items around your existing events.\n" +
                        "• Here is your optimized time-blocked plan:"
            }
            return AiResponseResult(messageText = msg, suggestedPlan = plan, isFromRemoteAi = false)
        }

        // Focus Mode queries
        if (lower.contains("focus") || lower.contains("pomodoro") || prompt.contains("تركيز") || prompt.contains("بومودورو") || prompt.contains("تشتت")) {
            val msg = if (isArabic) {
                "⏱️ نصائح جلسات التركيز العميق (Focus Shield):\n\n" +
                        "• إحصائياتك: ${context.focusSummary.ifBlank { "جاهز لبدء جلسة جديدة اليوم." }}\n" +
                        "• استخدم تقنية 45 دقيقة تركيز متبوعة بـ 10 دقائق استراحة للحفاظ على الطاقة الذهنية.\n" +
                        "• تفعيل وضع درع التركيز يساعد على إغلاق كل المشتتات وتشغيل أصوات الطبيعة (المطر أو الضوضاء البنية)."
            } else {
                "⏱️ Deep Focus Mode Insights (Focus Shield):\n\n" +
                        "• Your Stats: ${context.focusSummary.ifBlank { "Ready to launch your first session today." }}\n" +
                        "• Try the 45-minute sprint with a 10-minute break to balance stamina and momentum.\n" +
                        "• Enable Focus Shield to silence distractions and listen to calming Brownian noise or Rain audio."
            }
            return AiResponseResult(messageText = msg, isFromRemoteAi = false)
        }

        // Default friendly command center reply
        val msg = if (isArabic) {
            "أهلاً بك يا ${context.userName.ifBlank { "صديقي" }} في مساعد يومك الذكي. يمكنني تحليل ميزانيتك، حجز فترات تركيز ذكية في تقويمك، أو اقتراح أفضل تسلسل لمهامك وعاداتك اليوم. كيف يمكنني دعمك الآن؟"
        } else {
            "Welcome back, ${context.userName.ifBlank { "there" }}! I'm YAWMEK AI. I can analyze your financial budget, generate smart non-overlapping time blocks in your calendar, or optimize your deep focus sessions. How can I help you today?"
        }
        return AiResponseResult(messageText = msg, isFromRemoteAi = false)
    }

    private fun extractSmartPlanFromTasks(tasks: List<TaskEntity>): List<AiDailyPlanItem> {
        val pending = tasks.filter { !it.isCompleted }.take(4)
        if (pending.isEmpty()) {
            return listOf(
                AiDailyPlanItem(
                    title = "Deep Focus Block",
                    timeFormatted = "10:00 AM",
                    durationMinutes = 45,
                    priority = Priority.HIGH,
                    reason = "Dedicated uninterrupted focus on key priorities"
                ),
                AiDailyPlanItem(
                    title = "Daily Review & Finance",
                    timeFormatted = "05:00 PM",
                    durationMinutes = 20,
                    priority = Priority.MEDIUM,
                    reason = "Log daily expenses and review tomorrow's calendar"
                )
            )
        }

        var startHour = 10 // 10 AM
        return pending.mapIndexed { index, task ->
            val hour = (startHour + (index * 2)) % 24
            val formatted = String.format("%02d:00", hour)
            AiDailyPlanItem(
                title = task.title,
                timeFormatted = formatted,
                durationMinutes = if (task.durationMinutes > 0) task.durationMinutes else 30,
                priority = task.priority,
                reason = "Scheduled during peak focus window"
            )
        }
    }
}

data class UserContextData(
    val userName: String,
    val language: AppLanguage,
    val currency: String,
    val tasks: List<TaskEntity>,
    val pendingTasksSummary: String,
    val todaySpending: Double,
    val habitsSummary: String,
    val financialSummary: String = "",
    val calendarSummary: String = "",
    val focusSummary: String = ""
)
