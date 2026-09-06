package com.example.domain.ai

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object AiMemoryManager {
    private const val PREFS_NAME = "yawmek_ai_memory_prefs"
    private const val KEY_MEMORIES_JSON = "key_memories_json"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, AiUserMemoryItem::class.java)
    private val adapter = moshi.adapter<List<AiUserMemoryItem>>(listType)

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getAllMemories(context: Context): List<AiUserMemoryItem> {
        val json = getPrefs(context).getString(KEY_MEMORIES_JSON, null)
        if (json.isNullOrBlank()) {
            val defaults = getInitialDefaultMemories()
            saveAllMemories(context, defaults)
            return defaults
        }
        return try {
            adapter.fromJson(json) ?: getInitialDefaultMemories()
        } catch (e: Exception) {
            getInitialDefaultMemories()
        }
    }

    fun saveMemory(context: Context, item: AiUserMemoryItem) {
        val current = getAllMemories(context).toMutableList()
        val index = current.indexOfFirst { it.id == item.id || it.key.equals(item.key, ignoreCase = true) }
        if (index != -1) {
            current[index] = item
        } else {
            current.add(item)
        }
        saveAllMemories(context, current)
    }

    fun deleteMemory(context: Context, id: String) {
        val current = getAllMemories(context).toMutableList()
        current.removeAll { it.id == id }
        saveAllMemories(context, current)
    }

    fun clearAllMemories(context: Context) {
        getPrefs(context).edit().remove(KEY_MEMORIES_JSON).apply()
    }

    private fun saveAllMemories(context: Context, list: List<AiUserMemoryItem>) {
        try {
            val json = adapter.toJson(list)
            getPrefs(context).edit().putString(KEY_MEMORIES_JSON, json).apply()
        } catch (_: Exception) {}
    }

    private fun getInitialDefaultMemories(): List<AiUserMemoryItem> {
        return listOf(
            AiUserMemoryItem(
                id = "mem_peak_hours",
                key = "Peak Energy Window",
                value = "Morning (09:00 AM - 12:30 PM)",
                category = "Productivity Rhythms"
            ),
            AiUserMemoryItem(
                id = "mem_focus_duration",
                key = "Preferred Focus Block",
                value = "45 minutes sprint with 10 min break",
                category = "Focus Style"
            ),
            AiUserMemoryItem(
                id = "mem_planning_style",
                key = "Planning Methodology",
                value = "Time-blocking with 15-min buffers between tasks",
                category = "Scheduling Strategy"
            ),
            AiUserMemoryItem(
                id = "mem_evening_winddown",
                key = "Evening Review Time",
                value = "09:00 PM for daily reflection and task roll-over",
                category = "Habit Routines"
            )
        )
    }
}
