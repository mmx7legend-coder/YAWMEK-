package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.model.*

@Database(
    entities = [
        TaskEntity::class,
        SubtaskEntity::class,
        ExpenseEntity::class,
        BudgetEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        GoalEntity::class,
        MilestoneEntity::class,
        NoteEntity::class,
        UserSettingsEntity::class,
        GameStatsEntity::class,
        GameSessionEntity::class,
        GameAchievementEntity::class,
        GameDailyChallengeEntity::class,
        GamePreferencesEntity::class,
        UserAccountEntity::class,
        SyncMetadataEntity::class,
        FocusSessionEntity::class,
        NotificationPreferencesEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun habitDao(): HabitDao
    abstract fun goalDao(): GoalDao
    abstract fun noteDao(): NoteDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun gameDao(): GameDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun syncMetadataDao(): SyncMetadataDao
    abstract fun notificationPreferencesDao(): NotificationPreferencesDao
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yawmek_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
