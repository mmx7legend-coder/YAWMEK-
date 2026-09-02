package com.example.domain.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.model.NotificationPreferencesEntity
import java.time.LocalTime

data class InAppNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val channelId: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

object SmartNotificationManager {

    const val CHANNEL_TASKS = "yawmek_tasks"
    const val CHANNEL_HABITS = "yawmek_habits"
    const val CHANNEL_BUDGET = "yawmek_budget"
    const val CHANNEL_BRIEFING = "yawmek_briefing"
    const val CHANNEL_FOCUS = "yawmek_focus"
    const val CHANNEL_GAMES = "yawmek_games"

    private val inAppNotificationHistory = mutableListOf<InAppNotification>()

    fun getHistory(): List<InAppNotification> = inAppNotificationHistory.toList()

    fun clearHistory() {
        inAppNotificationHistory.clear()
    }

    fun initializeChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_TASKS,
                    "Tasks & Deadlines",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Reminders for urgent tasks and upcoming deadlines" },

                NotificationChannel(
                    CHANNEL_HABITS,
                    "Habit Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Daily prompts to keep your positive streaks alive" },

                NotificationChannel(
                    CHANNEL_BUDGET,
                    "Budget & Spending Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Warnings when approaching daily or monthly budget thresholds" },

                NotificationChannel(
                    CHANNEL_BRIEFING,
                    "Morning & Evening Reviews",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Daily executive briefings and evening reflection summaries" },

                NotificationChannel(
                    CHANNEL_FOCUS,
                    "Focus Sessions",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Alerts when your Pomodoro or deep focus timer completes" },

                NotificationChannel(
                    CHANNEL_GAMES,
                    "Brain Games & Challenges",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Daily 5-minute brain challenges and achievement notifications" }
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun isQuietHours(preferences: NotificationPreferencesEntity?): Boolean {
        if (preferences == null || !preferences.quietHoursEnabled) return false
        val now = LocalTime.now()
        val currentMinutes = now.hour * 60 + now.minute
        val start = preferences.quietHoursStartMinute
        val end = preferences.quietHoursEndMinute

        return if (start > end) {
            // Over midnight (e.g. 22:00 to 07:00)
            currentMinutes >= start || currentMinutes < end
        } else {
            currentMinutes in start until end
        }
    }

    fun postNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        message: String,
        preferences: NotificationPreferencesEntity? = null,
        forceBypassQuietHours: Boolean = false
    ) {
        // Record in in-app history regardless of system mute
        inAppNotificationHistory.add(
            0,
            InAppNotification(
                title = title,
                message = message,
                channelId = channelId
            )
        )
        if (inAppNotificationHistory.size > 50) {
            inAppNotificationHistory.removeLastOrNull()
        }

        // Check user preferences per category
        if (preferences != null) {
            val isEnabled = when (channelId) {
                CHANNEL_TASKS -> preferences.taskRemindersEnabled
                CHANNEL_HABITS -> preferences.habitRemindersEnabled
                CHANNEL_BUDGET -> preferences.budgetWarningsEnabled
                CHANNEL_BRIEFING -> preferences.morningBriefingEnabled || preferences.eveningReviewEnabled
                CHANNEL_FOCUS -> preferences.focusAlertsEnabled
                CHANNEL_GAMES -> preferences.dailyGameChallengeEnabled
                else -> true
            }
            if (!isEnabled) return
        }

        // Check quiet hours (unless emergency focus or bypass)
        if (!forceBypassQuietHours && isQuietHours(preferences)) {
            return
        }

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(
                    if (channelId == CHANNEL_TASKS || channelId == CHANNEL_FOCUS || channelId == CHANNEL_BUDGET)
                        NotificationCompat.PRIORITY_HIGH
                    else NotificationCompat.PRIORITY_DEFAULT
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Android 13+ permission not granted
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Convenience triggers
    fun notifyTaskReminder(context: Context, taskTitle: String, dueInMinutes: Int, prefs: NotificationPreferencesEntity? = null) {
        postNotification(
            context = context,
            notificationId = 1001 + taskTitle.hashCode() % 1000,
            channelId = CHANNEL_TASKS,
            title = "Task Reminder: $taskTitle",
            message = "Due in $dueInMinutes minutes. Tap to focus or complete.",
            preferences = prefs
        )
    }

    fun notifyBudgetWarning(context: Context, percent: Int, spentFormatted: String, prefs: NotificationPreferencesEntity? = null) {
        postNotification(
            context = context,
            notificationId = 2001,
            channelId = CHANNEL_BUDGET,
            title = "⚠️ Budget Alert ($percent%)",
            message = "You have spent $spentFormatted, reaching $percent% of your budget.",
            preferences = prefs
        )
    }

    fun notifyFocusComplete(context: Context, taskTitle: String?, durationMinutes: Int, prefs: NotificationPreferencesEntity? = null) {
        val target = if (!taskTitle.isNullOrBlank()) "on \"$taskTitle\"" else ""
        postNotification(
            context = context,
            notificationId = 3001,
            channelId = CHANNEL_FOCUS,
            title = "Focus Session Complete 🎉",
            message = "Great work! You completed $durationMinutes minutes of deep focus $target.",
            preferences = prefs,
            forceBypassQuietHours = true
        )
    }

    fun notifyDailyGameChallenge(context: Context, gameName: String, prefs: NotificationPreferencesEntity? = null) {
        postNotification(
            context = context,
            notificationId = 4001,
            channelId = CHANNEL_GAMES,
            title = "Daily Brain Workout Ready 🧠",
            message = "Challenge yourself with today's game: $gameName. Beat your top score!",
            preferences = prefs
        )
    }
}
