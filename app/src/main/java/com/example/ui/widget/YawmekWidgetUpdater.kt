package com.example.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object YawmekWidgetUpdater {
    fun requestUpdate(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)

            // Update Next Action Widget
            val nextActionComponent = ComponentName(context, YawmekNextActionWidgetProvider::class.java)
            val nextActionIds = appWidgetManager.getAppWidgetIds(nextActionComponent)
            if (nextActionIds != null && nextActionIds.isNotEmpty()) {
                val intent = Intent(context, YawmekNextActionWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, nextActionIds)
                }
                context.sendBroadcast(intent)
            }

            // Update Progress Widget
            val progressComponent = ComponentName(context, YawmekProgressWidgetProvider::class.java)
            val progressIds = appWidgetManager.getAppWidgetIds(progressComponent)
            if (progressIds != null && progressIds.isNotEmpty()) {
                val intent = Intent(context, YawmekProgressWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, progressIds)
                }
                context.sendBroadcast(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
