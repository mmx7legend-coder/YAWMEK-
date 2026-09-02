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
    exportSchema = true
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

        // ------------------------------------------------------------------
        // MIGRATIONS
        //
        // IMPORTANT: this database used to be built with
        // `.fallbackToDestructiveMigration()`, which silently DROPS AND
        // RECREATES every table (tasks, expenses, habits, goals, notes...)
        // whenever `version` above is bumped and no matching Migration is
        // registered. In other words: every schema change wiped every
        // user's local data with no warning.
        //
        // We don't have exported schema JSON files for versions 1 and 2
        // (exportSchema was `false`, so there is nothing to diff against),
        // so we cannot safely reconstruct real 1->2 and 2->3 migrations
        // after the fact. From version 3 onward, exportSchema is now
        // `true` (schemas are written to app/schemas/) so every future
        // change CAN and MUST ship a real Migration.
        //
        // Add one new Migration object per version bump, e.g.:
        //
        //   private val MIGRATION_3_4 = object : Migration(3, 4) {
        //       override fun migrate(db: SupportSQLiteDatabase) {
        //           db.execSQL(
        //               "ALTER TABLE tasks ADD COLUMN newColumn TEXT NOT NULL DEFAULT ''"
        //           )
        //       }
        //   }
        //
        // then register it below with `.addMigrations(MIGRATION_3_4)` and
        // bump `version` in the @Database annotation to 4.
        // ------------------------------------------------------------------

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yawmek_database"
                )
                    // No destructive fallback for normal upgrades anymore:
                    // if a future version bump ships without a matching
                    // Migration, Room throws IllegalStateException in debug/
                    // test builds instead of quietly deleting user data —
                    // that crash is the signal to go write the migration.
                    // .addMigrations(MIGRATION_3_4, ...)  // add future migrations here
                    .fallbackToDestructiveMigrationOnDowngrade() // only when installing an OLDER build over a newer DB (dev/testing edge case)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
