package com.example.data.remote

import com.example.BuildConfig
import com.example.data.local.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
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
                    Today's Spending: ${userContext.todaySpending} ${userContext.currency}
                    Active Habits: ${userContext.habitsSummary}
                    
                    Respond with empathy, clarity, high structure, and practical daily planning guidance.
                    Never invent fake personal data. Be inspiring, calm, and actionable.
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
                    return@withContext AiResponseResult(
                        messageText = text.trim(),
                        suggestedPlan = extractSmartPlanFromTasks(userContext.tasks),
                        isFromRemoteAi = true
                    )
                }
            } catch (e: Exception) {
                // Graceful fallback to smart local engine
            }
        }

        // Offline / Fallback Intelligent Rule Engine
        return@withContext generateLocalEngineResponse(prompt, userContext)
    }

    private fun generateLocalEngineResponse(prompt: String, context: UserContextData): AiResponseResult {
        val lower = prompt.lowercase()
        val isArabic = prompt.any { it in '\u0600'..'\u06FF' } || context.language == AppLanguage.ARABIC

        if (lower.contains("plan") || prompt.contains("خطط") || prompt.contains("جدول") || prompt.contains("evening") || prompt.contains("مساء")) {
            val plan = extractSmartPlanFromTasks(context.tasks)
            val msg = if (isArabic) {
                "إليك جدول منظم ومقترح بناءً على أولوياتك وأوقات فراغك المتاحة:\n\n" +
                        "• قمنا بترتيب المهام الأكثر إلحاحاً أولاً لتقليل الضغط الذهني.\n" +
                        "• تم تخصيص فترات راحة بين الأنشطة للحفاظ على طاقتك.\n\n" +
                        "يمكنك الضغط على 'تطبيق الجدول' أدناه لحفظ المواعيد فوراً في جدولك."
            } else {
                "Here is an optimized schedule crafted from your priorities and realistic time blocks:\n\n" +
                        "• High-leverage tasks are scheduled first to build immediate momentum.\n" +
                        "• Focused 30-45 minute blocks with breathing room prevent fatigue.\n\n" +
                        "Tap 'Apply Plan to Today' below to lock in these time slots."
            }
            return AiResponseResult(messageText = msg, suggestedPlan = plan, isFromRemoteAi = false)
        }

        if (lower.contains("forget") || prompt.contains("ناسي") || prompt.contains("نسيت") || prompt.contains("فايتني")) {
            val overdueCount = context.tasks.count { it.dueDateMillis != null && it.dueDateMillis < System.currentTimeMillis() && !it.isCompleted }
            val msg = if (isArabic) {
                if (overdueCount > 0) {
                    "لديك $overdueCount مهام متأخرة أو تحتاج مراجعة. أنصحك بالبدء بإنجازها أو إعادة جدولتها لتصفية ذهنك، كما لا تنسَ تسجيل مصاريف اليوم ومتابعة عاداتك اليومية."
                } else {
                    "جدولك محدث ومهامك تحت السيطرة تماماً! تذكر فقط شرب الماء، والتحقق من عاداتك اليومية قبل نهاية اليوم."
                }
            } else {
                if (overdueCount > 0) {
                    "You have $overdueCount pending items that might need attention. Resolving or rescheduling them now will clear your mental backlog."
                } else {
                    "All your scheduled commitments are in order! Just remember to log today's expenses and check off your active habits before unwinding."
                }
            }
            return AiResponseResult(messageText = msg, isFromRemoteAi = false)
        }

        if (lower.contains("break down") || lower.contains("goal") || prompt.contains("هدف") || prompt.contains("قسم") || prompt.contains("خطة")) {
            val msg = if (isArabic) {
                "لتحقيق أي هدف كبير بنجاح، السر يكمن في تقسيمه إلى ٣ مراحل واضحة:\n\n" +
                        "١. الإعداد والأساسيات (الأسبوع الأول: جمع المصادر وتحديد نصف ساعة يومياً).\n" +
                        "٢. التطبيق العملي المستمر (الأسبوع الثاني إلى الرابع: التركيز على الاستمرارية وليس الكم).\n" +
                        "٣. المراجعة والتقييم (نهاية كل أسبوع).\n\n" +
                        "لقد أعددت لك خطة مبسطة يمكنك تحويلها إلى أهداف ومهام فرعية داخل تطبيق يومك."
            } else {
                "To turn any ambitious goal into concrete reality, structure it into 3 clear phases:\n\n" +
                        "1. Foundation (Week 1: Setup resources and protect a daily 30m focus slot).\n" +
                        "2. Consistent Execution (Weeks 2-4: Build streak momentum without burnout).\n" +
                        "3. Review & Refine (Weekly milestone check-ins).\n\n" +
                        "You can track these directly in your YAWMEK Goals tab."
            }
            return AiResponseResult(messageText = msg, isFromRemoteAi = false)
        }

        // Default friendly command center reply
        val msg = if (isArabic) {
            "أهلاً بك يا ${context.userName.ifBlank { "صديقي" }} في مساعد يومك الذكي. يمكنني مساعدتك في تنظيم جدول اليوم، اقتراح الخطوة التالية، تقسيم الأهداف، أو مراجعة ميزانيتك وعاداتك. كيف تحب أن نبدأ؟"
        } else {
            "Welcome back, ${context.userName.ifBlank { "there" }}! I'm YAWMEK AI. I can structure your day, calculate your best next action, break down complex goals, or balance your schedule. What would you like to focus on?"
        }
        return AiResponseResult(messageText = msg, isFromRemoteAi = false)
    }

    private fun extractSmartPlanFromTasks(tasks: List<TaskEntity>): List<AiDailyPlanItem> {
        val pending = tasks.filter { !it.isCompleted }.take(4)
        if (pending.isEmpty()) {
            return listOf(
                AiDailyPlanItem(
                    title = "Deep Focus Block",
                    timeFormatted = "04:00 PM",
                    durationMinutes = 45,
                    priority = Priority.HIGH,
                    reason = "Dedicated uninterrupted focus on key priorities"
                ),
                AiDailyPlanItem(
                    title = "Daily Review & Reset",
                    timeFormatted = "06:00 PM",
                    durationMinutes = 20,
                    priority = Priority.MEDIUM,
                    reason = "Log daily expenses and plan tomorrow"
                )
            )
        }

        var startHour = 16 // 4 PM
        return pending.mapIndexed { index, task ->
            val hour = (startHour + index) % 24
            val formatted = String.format("%02d:00", hour)
            AiDailyPlanItem(
                title = task.title,
                timeFormatted = formatted,
                durationMinutes = if (task.durationMinutes > 0) task.durationMinutes else 30,
                priority = task.priority,
                reason = "High leverage execution"
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
    val habitsSummary: String
)
