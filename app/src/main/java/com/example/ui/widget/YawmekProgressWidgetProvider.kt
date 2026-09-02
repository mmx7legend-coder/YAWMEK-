package com.example.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class YawmekProgressWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val tasks = db.taskDao().getAllTasks().first()
                val habits = db.habitDao().getAllActiveHabits().first()
                val todayEpochDay = LocalDate.now().toEpochDay()
                val habitLogs = db.habitDao().getHabitLogsSince(todayEpochDay).first()
                val expenses = db.expenseDao().getAllExpenses().first()
                val todayStartMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val settings = db.userSettingsDao().getUserSettings()
                val currency = settings?.currency ?: "EGP"

                val completedTasks = tasks.count { it.isCompleted }
                val totalTasks = tasks.size
                val completedHabits = habitLogs.count { it.dateEpochDay == todayEpochDay }
                val todaySpent = expenses.filter { it.dateMillis >= todayStartMillis }.sumOf { it.amount }

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_progress)

                    views.setTextViewText(R.id.widget_stat_tasks, "$completedTasks/$totalTasks")
                    views.setTextViewText(R.id.widget_stat_habits, "$completedHabits/${habits.size}")
                    views.setTextViewText(R.id.widget_stat_money, String.format("%.0f %s", todaySpent, currency))

                    val score = if (totalTasks + habits.size > 0) {
                        ((completedTasks + completedHabits).toFloat() / (totalTasks + habits.size) * 100).toInt()
                    } else 100
                    views.setTextViewText(R.id.widget_score_badge, "$score% Score")

                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_progress_root, pendingIntent)
                    views.setOnClickPendingIntent(R.id.widget_progress_launch, pendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
