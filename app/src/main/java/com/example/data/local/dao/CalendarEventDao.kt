package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.model.CalendarEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY startMillis ASC")
    fun getAllEvents(): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE startMillis >= :startOfDay AND startMillis <= :endOfDay ORDER BY startMillis ASC")
    fun getEventsForDay(startOfDay: Long, endOfDay: Long): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE (startMillis <= :endRange AND endMillis >= :startRange) ORDER BY startMillis ASC")
    suspend fun getEventsInRange(startRange: Long, endRange: Long): List<CalendarEventEntity>

    @Query("SELECT * FROM calendar_events WHERE id = :id")
    suspend fun getEventById(id: Long): CalendarEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity): Long

    @Update
    suspend fun updateEvent(event: CalendarEventEntity)

    @Delete
    suspend fun deleteEvent(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("DELETE FROM calendar_events WHERE linkedTaskId = :taskId")
    suspend fun deleteEventsByTaskId(taskId: Long)
}
