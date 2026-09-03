package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        NotificationPreferencesEntity::class,
        WalletEntity::class,
        TransactionEntity::class,
        SavingsGoalEntity::class,
        GoalContributionEntity::class,
        FinancialSummaryEntity::class,
        TransferEntity::class,
        CalendarEventEntity::class,
        FocusSessionV2Entity::class,
        FocusStatisticsEntity::class
    ],
    version = 4,
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
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun financialSummaryDao(): FinancialSummaryDao
    abstract fun transferDao(): TransferDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun focusSessionV2Dao(): FocusSessionV2Dao
    abstract fun focusStatisticsDao(): FocusStatisticsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS wallets (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, accountType TEXT NOT NULL DEFAULT 'CHECKING', currency TEXT NOT NULL DEFAULT 'EGP', balance INTEGER NOT NULL DEFAULT 0, isDefault INTEGER NOT NULL DEFAULT 0, colorHex TEXT NOT NULL DEFAULT '#3B82F6', createdAtMillis INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS transactions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, walletId INTEGER NOT NULL, type TEXT NOT NULL DEFAULT 'EXPENSE', amount INTEGER NOT NULL DEFAULT 0, currency TEXT NOT NULL DEFAULT 'EGP', category TEXT NOT NULL DEFAULT 'OTHER_EXPENSE', description TEXT NOT NULL DEFAULT '', dateMillis INTEGER NOT NULL, isRecurring INTEGER NOT NULL DEFAULT 0, recurringPattern TEXT NOT NULL DEFAULT '', recurringEndDate INTEGER, tags TEXT NOT NULL DEFAULT '', attachmentPath TEXT, createdAtMillis INTEGER NOT NULL, FOREIGN KEY(walletId) REFERENCES wallets(id) ON DELETE CASCADE)")
                db.execSQL("CREATE TABLE IF NOT EXISTS savings_goals (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, walletId INTEGER NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL DEFAULT '', targetAmount INTEGER NOT NULL DEFAULT 0, currentAmount INTEGER NOT NULL DEFAULT 0, currency TEXT NOT NULL DEFAULT 'EGP', targetDateMillis INTEGER, category TEXT NOT NULL DEFAULT 'General', colorHex TEXT NOT NULL DEFAULT '#8B5CF6', isCompleted INTEGER NOT NULL DEFAULT 0, createdAtMillis INTEGER NOT NULL, FOREIGN KEY(walletId) REFERENCES wallets(id) ON DELETE CASCADE)")
                db.execSQL("CREATE TABLE IF NOT EXISTS goal_contributions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, goalId INTEGER NOT NULL, amount INTEGER NOT NULL DEFAULT 0, dateMillis INTEGER NOT NULL, note TEXT NOT NULL DEFAULT '', createdAtMillis INTEGER NOT NULL, FOREIGN KEY(goalId) REFERENCES savings_goals(id) ON DELETE CASCADE)")
                db.execSQL("CREATE TABLE IF NOT EXISTS financial_summaries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, walletId INTEGER NOT NULL, period TEXT NOT NULL DEFAULT 'MONTHLY', periodStartMillis INTEGER NOT NULL, periodEndMillis INTEGER NOT NULL, totalIncome INTEGER NOT NULL DEFAULT 0, totalExpense INTEGER NOT NULL DEFAULT 0, netSavings INTEGER NOT NULL DEFAULT 0, savingsRate REAL NOT NULL DEFAULT 0.0, topExpenseCategory TEXT NOT NULL DEFAULT '', averageDailyExpense INTEGER NOT NULL DEFAULT 0, createdAtMillis INTEGER NOT NULL, FOREIGN KEY(walletId) REFERENCES wallets(id) ON DELETE CASCADE)")
                db.execSQL("CREATE TABLE IF NOT EXISTS transfers (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, fromWalletId INTEGER NOT NULL, toWalletId INTEGER NOT NULL, amount INTEGER NOT NULL DEFAULT 0, currency TEXT NOT NULL DEFAULT 'EGP', description TEXT NOT NULL DEFAULT '', dateMillis INTEGER NOT NULL, createdAtMillis INTEGER NOT NULL, FOREIGN KEY(fromWalletId) REFERENCES wallets(id) ON DELETE CASCADE, FOREIGN KEY(toWalletId) REFERENCES wallets(id) ON DELETE CASCADE)")
                db.execSQL("CREATE TABLE IF NOT EXISTS calendar_events (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL DEFAULT '', startMillis INTEGER NOT NULL, endMillis INTEGER NOT NULL, allDay INTEGER NOT NULL DEFAULT 0, location TEXT NOT NULL DEFAULT '', colorHex TEXT NOT NULL DEFAULT '#3B82F6', isRecurring INTEGER NOT NULL DEFAULT 0, recurringPattern TEXT NOT NULL DEFAULT '', reminderMinutesBefore INTEGER NOT NULL DEFAULT 15, category TEXT NOT NULL DEFAULT 'OTHER', notes TEXT NOT NULL DEFAULT '', createdAtMillis INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS focus_session_v2 (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, taskId INTEGER, taskTitle TEXT, durationMinutes INTEGER NOT NULL DEFAULT 25, actualSeconds INTEGER NOT NULL DEFAULT 0, mode TEXT NOT NULL DEFAULT 'POMODORO_25', isCompleted INTEGER NOT NULL DEFAULT 0, startedAtMillis INTEGER NOT NULL, completedAtMillis INTEGER, audioType TEXT NOT NULL DEFAULT 'SILENT', audioVolume REAL NOT NULL DEFAULT 0.5, hapticsFeedback INTEGER NOT NULL DEFAULT 1, notes TEXT NOT NULL DEFAULT '', xpEarned INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("CREATE TABLE IF NOT EXISTS focus_statistics (id INTEGER PRIMARY KEY NOT NULL DEFAULT 1, totalFocusMinutes INTEGER NOT NULL DEFAULT 0, totalSessions INTEGER NOT NULL DEFAULT 0, currentStreak INTEGER NOT NULL DEFAULT 0, bestStreak INTEGER NOT NULL DEFAULT 0, longestSession INTEGER NOT NULL DEFAULT 0, averageSessionLength INTEGER NOT NULL DEFAULT 0, focusGoalMinutesPerDay INTEGER NOT NULL DEFAULT 120, dailyFocusMinutesAchieved INTEGER NOT NULL DEFAULT 0, lastFocusDateMillis INTEGER NOT NULL DEFAULT 0, createdAtMillis INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_wallet ON transactions(walletId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(dateMillis)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_calendar_events_start ON calendar_events(startMillis)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_focus_sessions_date ON focus_session_v2(startedAtMillis)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yawmek_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
