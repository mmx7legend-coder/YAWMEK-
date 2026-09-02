package com.example.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
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

class YawmekNextActionWidgetProvider : AppWidgetProvider() {

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
                val pending = tasks.filter { !it.isCompleted }
                val completedToday = tasks.count { it.isCompleted }
                val total = tasks.size

                val nextTask = pending.minByOrNull { task ->
                    // Prioritize HIGH priority, then earliest due time/date
                    var score = when (task.priority.name) {
                        "HIGH" -> 100
                        "MEDIUM" -> 500
                        else -> 1000
                    }
                    if (task.dueTimeMinutes != null) {
                        score += task.dueTimeMinutes
                    }
                    score
                }

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_next_action)

                    if (nextTask != null) {
                        views.setTextViewText(R.id.widget_task_title, nextTask.title)
                        val dueInfo = if (nextTask.dueTimeMinutes != null) {
                            val h = nextTask.dueTimeMinutes / 60
                            val m = nextTask.dueTimeMinutes % 60
                            val ampm = if (h >= 12) "PM" else "AM"
                            val displayH = if (h % 12 == 0) 12 else h % 12
                            String.format("Due %02d:%02d %s • %dm • %s", displayH, m, ampm, nextTask.durationMinutes, nextTask.priority.name)
                        } else {
                            "${nextTask.durationMinutes}m • ${nextTask.priority.name} Priority"
                        }
                        views.setTextViewText(R.id.widget_task_sub, dueInfo)
                        views.setTextViewText(R.id.widget_action_badge, "NEXT: ${nextTask.category.name}")
                    } else {
                        views.setTextViewText(R.id.widget_task_title, "All caught up for now! 🎉")
                        views.setTextViewText(R.id.widget_task_sub, "Tap + Task to add an item or plan ahead")
                        views.setTextViewText(R.id.widget_action_badge, "FREE TIME")
                    }

                    views.setTextViewText(R.id.widget_progress_text, "$completedToday/$total Done")

                    // Main open intent
                    val mainIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val mainPendingIntent = PendingIntent.getActivity(
                        context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)

                    // "What Should I Do Now" intent
                    val whatToDoIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(MainActivity.EXTRA_NAV_DESTINATION, "WHAT_TO_DO")
                    }
                    val whatToDoPending = PendingIntent.getActivity(
                        context, 1, whatToDoIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_btn_action, whatToDoPending)

                    // "+ Task" intent
                    val addTaskIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(MainActivity.EXTRA_NAV_DESTINATION, "QUICK_ADD")
                    }
                    val addTaskPending = PendingIntent.getActivity(
                        context, 2, addTaskIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_btn_add, addTaskPending)

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
