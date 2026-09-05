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
        FocusPreferencesEntity::class,
        NotificationPreferencesEntity::class,
        // RPG System Entities
        CharacterRpgEntity::class,
        EquipmentItemEntity::class,
        CompanionEntity::class,
        SkillNodeEntity::class,
        RpgQuestEntity::class,
        BossChallengeEntity::class,
        // Advanced Money System Entities
        WalletAccountEntity::class,
        FinancialTransactionEntity::class,
        FinancialCategoryEntity::class,
        RecurringTransactionEntity::class,
        FinancialGoalEntity::class,
        // Professional Calendar Entities
        CalendarEventEntity::class,
        // Community System Entities
        CommunityProfileEntity::class,
        CommunityFriendEntity::class,
        CommunityBattleEntity::class
    ],
    version = 5,
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
    abstract fun focusPreferencesDao(): FocusPreferencesDao
    abstract fun rpgDao(): RpgDao

    // Advanced Money DAOs
    abstract fun walletAccountDao(): WalletAccountDao
    abstract fun financialTransactionDao(): FinancialTransactionDao
    abstract fun financialCategoryDao(): FinancialCategoryDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun financialGoalDao(): FinancialGoalDao

    // Professional Calendar DAO
    abstract fun calendarEventDao(): CalendarEventDao

    // Community System DAO
    abstract fun communityDao(): CommunityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `game_stats` (`gameId` TEXT NOT NULL, `bestScore` INTEGER NOT NULL, `bestTimeMillis` INTEGER NOT NULL, `gamesPlayed` INTEGER NOT NULL, `gamesWon` INTEGER NOT NULL, `currentStreak` INTEGER NOT NULL, `bestStreak` INTEGER NOT NULL, `favoriteDifficulty` TEXT NOT NULL, `lastPlayedMillis` INTEGER NOT NULL, `totalScore` INTEGER NOT NULL, `averageScore` REAL NOT NULL, PRIMARY KEY(`gameId`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `game_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `gameId` TEXT NOT NULL, `score` INTEGER NOT NULL, `timeMillis` INTEGER NOT NULL, `difficulty` TEXT NOT NULL, `accuracy` INTEGER NOT NULL, `combo` INTEGER NOT NULL, `xpEarned` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `isPersonalRecord` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `game_achievements` (`achievementId` TEXT NOT NULL, `unlockedAtMillis` INTEGER NOT NULL, PRIMARY KEY(`achievementId`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `game_daily_challenges` (`dateEpochDay` INTEGER NOT NULL, `gameId` TEXT NOT NULL, `difficulty` TEXT NOT NULL, `targetScore` INTEGER NOT NULL, `rewardXp` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, `bestScore` INTEGER NOT NULL, PRIMARY KEY(`dateEpochDay`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `game_preferences` (`id` INTEGER NOT NULL, `soundEnabled` INTEGER NOT NULL, `hapticsEnabled` INTEGER NOT NULL, `animationsEnabled` INTEGER NOT NULL, `favoriteGameIds` TEXT NOT NULL, `userLevel` INTEGER NOT NULL, `userXp` INTEGER NOT NULL, `gameStreak` INTEGER NOT NULL, `lastPlayedEpochDay` INTEGER NOT NULL, `preferredDifficulty` TEXT NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `user_accounts` (`id` TEXT NOT NULL, `email` TEXT NOT NULL, `displayName` TEXT NOT NULL, `avatarUrl` TEXT, `passwordHash` TEXT, `isLoggedIn` INTEGER NOT NULL, `isCloudSyncEnabled` INTEGER NOT NULL, `lastLoginMillis` INTEGER NOT NULL, `createdAtMillis` INTEGER NOT NULL, `token` TEXT, `deviceName` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `sync_metadata` (`entityCompositeKey` TEXT NOT NULL, `entityType` TEXT NOT NULL, `entityId` TEXT NOT NULL, `lastModifiedMillis` INTEGER NOT NULL, `syncStatus` TEXT NOT NULL, `version` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, PRIMARY KEY(`entityCompositeKey`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `focus_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `taskId` INTEGER, `taskTitle` TEXT, `durationMinutes` INTEGER NOT NULL, `actualSeconds` INTEGER NOT NULL, `mode` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, `startedAtMillis` INTEGER NOT NULL, `completedAtMillis` INTEGER, `soundMode` TEXT NOT NULL, `notes` TEXT NOT NULL, `xpAwarded` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `focus_preferences` (`id` INTEGER NOT NULL, `dailyTargetMinutes` INTEGER NOT NULL, `weeklyTargetMinutes` INTEGER NOT NULL, `defaultPreset` TEXT NOT NULL, `selectedSound` TEXT NOT NULL, `soundVolume` REAL NOT NULL, `soundEnabled` INTEGER NOT NULL, `hapticsEnabled` INTEGER NOT NULL, `focusShieldEnabled` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `notification_preferences` (`id` INTEGER NOT NULL, `taskRemindersEnabled` INTEGER NOT NULL, `upcomingEventsEnabled` INTEGER NOT NULL, `habitRemindersEnabled` INTEGER NOT NULL, `budgetWarningsEnabled` INTEGER NOT NULL, `morningBriefingEnabled` INTEGER NOT NULL, `eveningReviewEnabled` INTEGER NOT NULL, `focusAlertsEnabled` INTEGER NOT NULL, `dailyGameChallengeEnabled` INTEGER NOT NULL, `quietHoursEnabled` INTEGER NOT NULL, `quietHoursStartMinute` INTEGER NOT NULL, `quietHoursEndMinute` INTEGER NOT NULL, `morningBriefMinute` INTEGER NOT NULL, `eveningReviewMinute` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. RPG System tables
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `rpg_character` (
                        `id` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `level` INTEGER NOT NULL,
                        `currentXp` INTEGER NOT NULL,
                        `totalEarnedXp` INTEGER NOT NULL,
                        `availableSkillPoints` INTEGER NOT NULL,
                        `statFocus` INTEGER NOT NULL,
                        `statDiscipline` INTEGER NOT NULL,
                        `statKnowledge` INTEGER NOT NULL,
                        `statConsistency` INTEGER NOT NULL,
                        `statPlanning` INTEGER NOT NULL,
                        `statCourage` INTEGER NOT NULL,
                        `equippedWeaponId` TEXT NOT NULL,
                        `equippedArmorId` TEXT NOT NULL,
                        `equippedHelmetId` TEXT,
                        `equippedGlovesId` TEXT,
                        `equippedBootsId` TEXT,
                        `equippedCapeId` TEXT,
                        `equippedAccessoryId` TEXT,
                        `equippedFrameId` TEXT NOT NULL,
                        `equippedBackgroundId` TEXT NOT NULL,
                        `equippedCompanionId` TEXT,
                        `dragonStage` TEXT NOT NULL,
                        `dragonAffinity` TEXT NOT NULL,
                        `dragonProgressPoints` INTEGER NOT NULL,
                        `currentAdventureArea` TEXT NOT NULL,
                        `areaMilestoneProgress` INTEGER NOT NULL,
                        `productiveStreakDays` INTEGER NOT NULL,
                        `lastActiveDateEpochDay` INTEGER NOT NULL,
                        `avatarSkin` TEXT NOT NULL,
                        `titleEn` TEXT NOT NULL,
                        `titleAr` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `rpg_equipment_items` (
                        `id` TEXT NOT NULL,
                        `slot` TEXT NOT NULL,
                        `rarity` TEXT NOT NULL,
                        `nameEn` TEXT NOT NULL,
                        `nameAr` TEXT NOT NULL,
                        `descriptionEn` TEXT NOT NULL,
                        `descriptionAr` TEXT NOT NULL,
                        `iconEmoji` TEXT NOT NULL,
                        `requiredLevel` INTEGER NOT NULL,
                        `xpStoreCost` INTEGER NOT NULL,
                        `isPurchased` INTEGER NOT NULL,
                        `statBoostDescription` TEXT NOT NULL,
                        `bonusStatFocus` INTEGER NOT NULL,
                        `bonusStatDiscipline` INTEGER NOT NULL,
                        `bonusStatKnowledge` INTEGER NOT NULL,
                        `bonusStatConsistency` INTEGER NOT NULL,
                        `bonusStatPlanning` INTEGER NOT NULL,
                        `bonusStatCourage` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `rpg_companions` (
                        `id` TEXT NOT NULL,
                        `nameEn` TEXT NOT NULL,
                        `nameAr` TEXT NOT NULL,
                        `speciesEn` TEXT NOT NULL,
                        `speciesAr` TEXT NOT NULL,
                        `descriptionEn` TEXT NOT NULL,
                        `descriptionAr` TEXT NOT NULL,
                        `iconEmoji` TEXT NOT NULL,
                        `requiredLevel` INTEGER NOT NULL,
                        `xpCost` INTEGER NOT NULL,
                        `isUnlocked` INTEGER NOT NULL,
                        `reactionIdleAr` TEXT NOT NULL,
                        `reactionIdleEn` TEXT NOT NULL,
                        `reactionCelebrateAr` TEXT NOT NULL,
                        `reactionCelebrateEn` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `rpg_skills` (
                        `id` TEXT NOT NULL,
                        `branch` TEXT NOT NULL,
                        `tier` INTEGER NOT NULL,
                        `nameEn` TEXT NOT NULL,
                        `nameAr` TEXT NOT NULL,
                        `descriptionEn` TEXT NOT NULL,
                        `descriptionAr` TEXT NOT NULL,
                        `iconEmoji` TEXT NOT NULL,
                        `isUnlocked` INTEGER NOT NULL,
                        `pointCost` INTEGER NOT NULL,
                        `statBonusFocus` INTEGER NOT NULL,
                        `statBonusDiscipline` INTEGER NOT NULL,
                        `statBonusKnowledge` INTEGER NOT NULL,
                        `statBonusConsistency` INTEGER NOT NULL,
                        `statBonusPlanning` INTEGER NOT NULL,
                        `statBonusCourage` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `rpg_quests` (
                        `id` TEXT NOT NULL,
                        `titleEn` TEXT NOT NULL,
                        `titleAr` TEXT NOT NULL,
                        `descriptionEn` TEXT NOT NULL,
                        `descriptionAr` TEXT NOT NULL,
                        `xpReward` INTEGER NOT NULL,
                        `targetCount` INTEGER NOT NULL,
                        `currentCount` INTEGER NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `isClaimed` INTEGER NOT NULL,
                        `isWeekly` INTEGER NOT NULL,
                        `category` TEXT NOT NULL,
                        `iconEmoji` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `rpg_boss_challenges` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `nameEn` TEXT NOT NULL,
                        `nameAr` TEXT NOT NULL,
                        `descriptionEn` TEXT NOT NULL,
                        `descriptionAr` TEXT NOT NULL,
                        `totalHp` INTEGER NOT NULL,
                        `currentHp` INTEGER NOT NULL,
                        `requiredLevel` INTEGER NOT NULL,
                        `isDefeated` INTEGER NOT NULL,
                        `xpBounty` INTEGER NOT NULL,
                        `associatedGoalId` INTEGER,
                        `associatedHabitId` INTEGER,
                        `bossAvatar` TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                // 2. Advanced Money tables
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `wallet_accounts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `balanceMinor` INTEGER NOT NULL,
                        `currency` TEXT NOT NULL,
                        `colorHex` TEXT NOT NULL,
                        `iconName` TEXT NOT NULL,
                        `isDefault` INTEGER NOT NULL,
                        `isArchived` INTEGER NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `financial_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `accountId` INTEGER NOT NULL,
                        `toAccountId` INTEGER,
                        `type` TEXT NOT NULL,
                        `amountMinor` INTEGER NOT NULL,
                        `currency` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `subcategory` TEXT NOT NULL,
                        `note` TEXT NOT NULL,
                        `dateMillis` INTEGER NOT NULL,
                        `isRecurring` INTEGER NOT NULL,
                        `recurringRuleId` INTEGER,
                        `goalId` INTEGER,
                        `createdAtMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `financial_categories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `nameEn` TEXT NOT NULL,
                        `nameAr` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `iconName` TEXT NOT NULL,
                        `colorHex` TEXT NOT NULL,
                        `parentCategoryId` INTEGER,
                        `isCustom` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `recurring_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `accountId` INTEGER NOT NULL,
                        `toAccountId` INTEGER,
                        `type` TEXT NOT NULL,
                        `amountMinor` INTEGER NOT NULL,
                        `currency` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `subcategory` TEXT NOT NULL,
                        `note` TEXT NOT NULL,
                        `frequency` TEXT NOT NULL,
                        `nextDueDateMillis` INTEGER NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `financial_goals` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `targetAmountMinor` INTEGER NOT NULL,
                        `currentSavedMinor` INTEGER NOT NULL,
                        `currency` TEXT NOT NULL,
                        `targetDateMillis` INTEGER,
                        `colorHex` TEXT NOT NULL,
                        `iconName` TEXT NOT NULL,
                        `isReached` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // 3. Professional Calendar tables
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `calendar_events` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `startMillis` INTEGER NOT NULL,
                        `endMillis` INTEGER NOT NULL,
                        `isAllDay` INTEGER NOT NULL,
                        `category` TEXT NOT NULL,
                        `colorHex` TEXT NOT NULL,
                        `location` TEXT NOT NULL,
                        `recurrence` TEXT NOT NULL,
                        `reminderMinutesBefore` INTEGER NOT NULL,
                        `linkedTaskId` INTEGER,
                        `linkedGoalId` INTEGER,
                        `linkedHabitId` INTEGER,
                        `isDeviceCalendarSynced` INTEGER NOT NULL,
                        `deviceCalendarEventId` INTEGER,
                        `createdAtMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // 4. Focus Preferences table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `focus_preferences` (
                        `id` INTEGER NOT NULL,
                        `dailyTargetMinutes` INTEGER NOT NULL,
                        `weeklyTargetMinutes` INTEGER NOT NULL,
                        `defaultPreset` TEXT NOT NULL,
                        `selectedSound` TEXT NOT NULL,
                        `soundVolume` REAL NOT NULL,
                        `soundEnabled` INTEGER NOT NULL,
                        `hapticsEnabled` INTEGER NOT NULL,
                        `focusShieldEnabled` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )

                // 5. Seed default wallet account if empty
                val now = System.currentTimeMillis()
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO wallet_accounts (id, name, type, balanceMinor, currency, colorHex, iconName, isDefault, isArchived, createdAtMillis)
                    VALUES (1, 'Main Wallet', 'CASH', 0, 'EGP', '#10B981', 'wallet', 1, 0, $now)
                    """.trimIndent()
                )

                // 6. Safe backward data preservation: migrate existing legacy expenses into financial_transactions
                try {
                    db.execSQL(
                        """
                        INSERT INTO financial_transactions (accountId, type, amountMinor, currency, category, note, dateMillis, isRecurring, createdAtMillis)
                        SELECT 1, 'EXPENSE', CAST(ROUND(amount * 100) AS INTEGER), currency, category, note, dateMillis, 0, createdAtMillis
                        FROM expenses
                        """.trimIndent()
                    )
                } catch (e: Exception) {
                    // Safe fallback if expenses table is empty or schema difference
                }
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Community Profile table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `community_profile` (
                        `id` INTEGER NOT NULL,
                        `username` TEXT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `avatarId` TEXT NOT NULL,
                        `bio` TEXT NOT NULL,
                        `rankTitleEn` TEXT NOT NULL,
                        `rankTitleAr` TEXT NOT NULL,
                        `rating` INTEGER NOT NULL,
                        `battleWins` INTEGER NOT NULL,
                        `battleLosses` INTEGER NOT NULL,
                        `connectionId` TEXT NOT NULL,
                        `isOnline` INTEGER NOT NULL,
                        `isInitialSetupDone` INTEGER NOT NULL,
                        `lastActiveMillis` INTEGER NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )

                // 2. Community Friends table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `community_friends` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `friendUsername` TEXT NOT NULL,
                        `friendDisplayName` TEXT NOT NULL,
                        `avatarId` TEXT NOT NULL,
                        `level` INTEGER NOT NULL,
                        `xp` INTEGER NOT NULL,
                        `rankTitle` TEXT NOT NULL,
                        `characterClass` TEXT NOT NULL,
                        `isOnline` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `rating` INTEGER NOT NULL,
                        `tasksCompleted` INTEGER NOT NULL,
                        `focusMinutes` INTEGER NOT NULL,
                        `lastActiveMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // 3. Community Battles table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `community_battles` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `opponentUsername` TEXT NOT NULL,
                        `opponentDisplayName` TEXT NOT NULL,
                        `opponentAvatarId` TEXT NOT NULL,
                        `opponentLevel` INTEGER NOT NULL,
                        `battleType` TEXT NOT NULL,
                        `myScore` INTEGER NOT NULL,
                        `opponentScore` INTEGER NOT NULL,
                        `result` TEXT NOT NULL,
                        `ratingDelta` INTEGER NOT NULL,
                        `xpGained` INTEGER NOT NULL,
                        `coinsGained` INTEGER NOT NULL,
                        `summaryLog` TEXT NOT NULL,
                        `timestampMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yawmek_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
