package com.example.domain

import com.example.data.local.model.Priority
import com.example.data.local.model.TaskCategory
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.regex.Pattern

data class ParsedTaskDraft(
    val title: String,
    val dueDateMillis: Long?,
    val dueTimeMinutes: Int?,
    val durationMinutes: Int,
    val priority: Priority,
    val category: TaskCategory,
    val originalText: String
)

object NaturalLanguageParser {

    fun parse(input: String): ParsedTaskDraft {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return ParsedTaskDraft(
                title = "",
                dueDateMillis = null,
                dueTimeMinutes = null,
                durationMinutes = 30,
                priority = Priority.MEDIUM,
                category = TaskCategory.PERSONAL,
                originalText = input
            )
        }

        var workingText = trimmed
        var targetDate = LocalDate.now()
        var hasExplicitDate = false
        var dueTimeMinutes: Int? = null
        var durationMinutes = 30
        var priority = Priority.MEDIUM
        var category = TaskCategory.PERSONAL

        val lower = workingText.lowercase()

        // 1. Priority Detection
        if (lower.contains("urgent") || lower.contains("high priority") || lower.contains("priority high") ||
            workingText.contains("مهم جدا") || workingText.contains("أولوية عالية") || workingText.contains("عاجل") || workingText.contains("طارئ")) {
            priority = Priority.HIGH
            workingText = workingText.replace(Regex("(?i)\\b(urgent|high priority|priority high)\\b"), "")
                .replace("مهم جدا", "")
                .replace("أولوية عالية", "")
                .replace("عاجل", "")
                .replace("طارئ", "")
        } else if (lower.contains("low priority") || lower.contains("priority low") || workingText.contains("أولوية منخفضة") || workingText.contains("غير عاجل")) {
            priority = Priority.LOW
            workingText = workingText.replace(Regex("(?i)\\b(low priority|priority low)\\b"), "")
                .replace("أولوية منخفضة", "")
                .replace("غير عاجل", "")
        }

        // 2. Date Detection
        if (lower.contains("tomorrow") || workingText.contains("غدا") || workingText.contains("غداً") || workingText.contains("بكرة")) {
            targetDate = targetDate.plusDays(1)
            hasExplicitDate = true
            workingText = workingText.replace(Regex("(?i)\\btomorrow\\b"), "")
                .replace("غداً", "")
                .replace("غدا", "")
                .replace("بكرة", "")
        } else if (lower.contains("today") || workingText.contains("اليوم") || workingText.contains("النهاردة")) {
            targetDate = LocalDate.now()
            hasExplicitDate = true
            workingText = workingText.replace(Regex("(?i)\\btoday\\b"), "")
                .replace("اليوم", "")
                .replace("النهاردة", "")
        }

        // 3. Duration Detection (e.g., "for 45 minutes", "for 1 hour", "45 mins", "لمدة ساعة", "لمدة 30 دقيقة")
        val durationRegexEn = Regex("(?i)\\b(?:for\\s+)?(\\d+)\\s*(?:min|mins|minutes)\\b")
        val durationMatchEn = durationRegexEn.find(workingText)
        if (durationMatchEn != null) {
            durationMinutes = durationMatchEn.groupValues[1].toIntOrNull() ?: 30
            workingText = workingText.replace(durationMatchEn.value, "")
        } else {
            val hourRegexEn = Regex("(?i)\\b(?:for\\s+)?(\\d+)\\s*(?:hr|hrs|hour|hours)\\b")
            val hourMatchEn = hourRegexEn.find(workingText)
            if (hourMatchEn != null) {
                durationMinutes = (hourMatchEn.groupValues[1].toIntOrNull() ?: 1) * 60
                workingText = workingText.replace(hourMatchEn.value, "")
            } else if (workingText.contains("لمدة ساعة")) {
                durationMinutes = 60
                workingText = workingText.replace("لمدة ساعة", "")
            } else {
                val durRegexAr = Regex("لمدة\\s*(\\d+)\\s*دقيقة")
                val durMatchAr = durRegexAr.find(workingText)
                if (durMatchAr != null) {
                    durationMinutes = durMatchAr.groupValues[1].toIntOrNull() ?: 30
                    workingText = workingText.replace(durMatchAr.value, "")
                }
            }
        }

        // 4. Time Detection (e.g., "at 5 PM", "at 17:00", "5:30 pm", "الساعة 5 مساء")
        val timeRegex12h = Regex("(?i)\\b(?:at\\s+)?(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)\\b")
        val match12h = timeRegex12h.find(workingText)
        if (match12h != null) {
            val hourRaw = match12h.groupValues[1].toIntOrNull() ?: 0
            val minRaw = match12h.groupValues[2].toIntOrNull() ?: 0
            val ampm = match12h.groupValues[3].lowercase()
            var hour = hourRaw
            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0
            dueTimeMinutes = hour * 60 + minRaw
            hasExplicitDate = true
            workingText = workingText.replace(match12h.value, "")
        } else {
            val timeRegex24h = Regex("(?i)\\b(?:at\\s+)?(\\d{1,2}):(\\d{2})\\b")
            val match24h = timeRegex24h.find(workingText)
            if (match24h != null) {
                val hour = match24h.groupValues[1].toIntOrNull() ?: 0
                val min = match24h.groupValues[2].toIntOrNull() ?: 0
                if (hour in 0..23 && min in 0..59) {
                    dueTimeMinutes = hour * 60 + min
                    hasExplicitDate = true
                    workingText = workingText.replace(match24h.value, "")
                }
            } else {
                // Arabic time: الساعة 5 مساء / الساعة 3 عصرا
                val arabicTimeRegex = Regex("الساعة\\s*(\\d{1,2})(?::(\\d{2}))?\\s*(مساءً|مساء|صباحاً|صباح|عصراً|عصر)?")
                val matchAr = arabicTimeRegex.find(workingText)
                if (matchAr != null) {
                    var hour = matchAr.groupValues[1].toIntOrNull() ?: 0
                    val min = matchAr.groupValues[2].toIntOrNull() ?: 0
                    val periodAr = matchAr.groupValues[3]
                    if ((periodAr.contains("مساء") || periodAr.contains("عصر")) && hour < 12) {
                        hour += 12
                    } else if (periodAr.contains("صباح") && hour == 12) {
                        hour = 0
                    }
                    dueTimeMinutes = hour * 60 + min
                    hasExplicitDate = true
                    workingText = workingText.replace(matchAr.value, "")
                }
            }
        }

        // 5. Category Detection
        val cleanLower = workingText.lowercase()
        category = when {
            cleanLower.contains("study") || cleanLower.contains("exam") || cleanLower.contains("homework") ||
                    cleanLower.contains("math") || cleanLower.contains("physics") || cleanLower.contains("دراسة") ||
                    cleanLower.contains("مذاكرة") || cleanLower.contains("امتحان") || cleanLower.contains("بحث") -> TaskCategory.STUDY

            cleanLower.contains("meeting") || cleanLower.contains("work") || cleanLower.contains("client") ||
                    cleanLower.contains("report") || cleanLower.contains("project") || cleanLower.contains("عمل") ||
                    cleanLower.contains("اجتماع") || cleanLower.contains("مشروع") || cleanLower.contains("تقرير") -> TaskCategory.WORK

            cleanLower.contains("gym") || cleanLower.contains("workout") || cleanLower.contains("run") ||
                    cleanLower.contains("doctor") || cleanLower.contains("medicine") || cleanLower.contains("رياضة") ||
                    cleanLower.contains("تمرين") || cleanLower.contains("طبيب") || cleanLower.contains("دواء") -> TaskCategory.HEALTH

            cleanLower.contains("buy") || cleanLower.contains("bill") || cleanLower.contains("pay") ||
                    cleanLower.contains("bank") || cleanLower.contains("شراء") || cleanLower.contains("فاتورة") ||
                    cleanLower.contains("دفع") || cleanLower.contains("تسوق") -> TaskCategory.FINANCE

            else -> TaskCategory.PERSONAL
        }

        // Clean up title
        var cleanTitle = workingText
            .replace(Regex("\\s+"), " ")
            .trim()
            .trim(',', '.', '-', '،')
            .trim()

        if (cleanTitle.isEmpty()) {
            cleanTitle = input.trim()
        }

        val dueDateMillis = if (hasExplicitDate) {
            targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } else {
            LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        return ParsedTaskDraft(
            title = cleanTitle,
            dueDateMillis = dueDateMillis,
            dueTimeMinutes = dueTimeMinutes,
            durationMinutes = durationMinutes,
            priority = priority,
            category = category,
            originalText = input
        )
    }
}
