package com.example.domain.calendar

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class CalendarEventItem(
    val id: Long,
    val title: String,
    val description: String? = null,
    val startMillis: Long,
    val endMillis: Long,
    val durationMinutes: Int,
    val location: String? = null,
    val calendarName: String? = null,
    val color: Int? = null,
    val isAllDay: Boolean = false
)

object CalendarManager {

    suspend fun fetchEventsForDay(
        context: Context,
        date: LocalDate = LocalDate.now()
    ): List<CalendarEventItem> = withContext(Dispatchers.IO) {
        val events = mutableListOf<CalendarEventItem>()
        try {
            val contentResolver: ContentResolver = context.contentResolver
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, startOfDay)
            ContentUris.appendId(builder, endOfDay)

            val projection = arrayOf(
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.DESCRIPTION,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.EVENT_LOCATION,
                CalendarContract.Instances.CALENDAR_DISPLAY_NAME,
                CalendarContract.Instances.DISPLAY_COLOR,
                CalendarContract.Instances.ALL_DAY
            )

            val cursor = contentResolver.query(
                builder.build(),
                projection,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            cursor?.use {
                val idCol = it.getColumnIndex(CalendarContract.Instances.EVENT_ID)
                val titleCol = it.getColumnIndex(CalendarContract.Instances.TITLE)
                val descCol = it.getColumnIndex(CalendarContract.Instances.DESCRIPTION)
                val beginCol = it.getColumnIndex(CalendarContract.Instances.BEGIN)
                val endCol = it.getColumnIndex(CalendarContract.Instances.END)
                val locCol = it.getColumnIndex(CalendarContract.Instances.EVENT_LOCATION)
                val calCol = it.getColumnIndex(CalendarContract.Instances.CALENDAR_DISPLAY_NAME)
                val colorCol = it.getColumnIndex(CalendarContract.Instances.DISPLAY_COLOR)
                val allDayCol = it.getColumnIndex(CalendarContract.Instances.ALL_DAY)

                while (it.moveToNext()) {
                    val id = if (idCol >= 0) it.getLong(idCol) else 0L
                    val title = if (titleCol >= 0) it.getString(titleCol) ?: "Calendar Event" else "Calendar Event"
                    val desc = if (descCol >= 0) it.getString(descCol) else null
                    val begin = if (beginCol >= 0) it.getLong(beginCol) else startOfDay
                    val end = if (endCol >= 0) it.getLong(endCol) else begin + 30 * 60 * 1000L
                    val loc = if (locCol >= 0) it.getString(locCol) else null
                    val cal = if (calCol >= 0) it.getString(calCol) else null
                    val color = if (colorCol >= 0) it.getInt(colorCol) else null
                    val allDay = if (allDayCol >= 0) it.getInt(allDayCol) == 1 else false

                    val durationMinutes = ((end - begin) / (1000 * 60)).coerceAtLeast(15).toInt()

                    events.add(
                        CalendarEventItem(
                            id = id,
                            title = title,
                            description = desc,
                            startMillis = begin,
                            endMillis = end,
                            durationMinutes = durationMinutes,
                            location = loc,
                            calendarName = cal,
                            color = color,
                            isAllDay = allDay
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted yet
        } catch (e: Exception) {
            e.printStackTrace()
        }
        events
    }

    suspend fun addEventToCalendar(
        context: Context,
        title: String,
        description: String,
        startMillis: Long,
        durationMinutes: Int
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val endMillis = startMillis + (durationMinutes * 60 * 1000L)

            // Find primary calendar id
            var calendarId: Long = 1
            val calCursor = contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY),
                null,
                null,
                null
            )
            calCursor?.use {
                val idIdx = it.getColumnIndex(CalendarContract.Calendars._ID)
                val primIdx = it.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
                while (it.moveToNext()) {
                    val id = it.getLong(idIdx)
                    val isPrim = if (primIdx >= 0) it.getInt(primIdx) == 1 else false
                    if (isPrim) {
                        calendarId = id
                        break
                    }
                    calendarId = id
                }
            }

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, ZoneId.systemDefault().id)
            }

            val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            uri != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
