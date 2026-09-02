package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncStatus {
    SYNCED,
    PENDING,
    FAILED
}

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val id: String = "local_user",
    val email: String = "",
    val displayName: String = "Guest User",
    val avatarUrl: String? = null,
    val passwordHash: String? = null,
    val isLoggedIn: Boolean = false,
    val isCloudSyncEnabled: Boolean = true,
    val lastLoginMillis: Long = System.currentTimeMillis(),
    val createdAtMillis: Long = System.currentTimeMillis(),
    val token: String? = null,
    val deviceName: String = "Android Device"
)

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val entityCompositeKey: String, // e.g. "task_1", "expense_4"
    val entityType: String,
    val entityId: String,
    val lastModifiedMillis: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val version: Long = 1,
    val isDeleted: Boolean = false
)

@Entity(tableName = "notification_preferences")
data class NotificationPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val taskRemindersEnabled: Boolean = true,
    val upcomingEventsEnabled: Boolean = true,
    val habitRemindersEnabled: Boolean = true,
    val budgetWarningsEnabled: Boolean = true,
    val morningBriefingEnabled: Boolean = true,
    val eveningReviewEnabled: Boolean = true,
    val focusAlertsEnabled: Boolean = true,
    val dailyGameChallengeEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = true,
    val quietHoursStartMinute: Int = 22 * 60, // 10:00 PM (1320)
    val quietHoursEndMinute: Int = 7 * 60,    // 07:00 AM (420)
    val morningBriefMinute: Int = 8 * 60,     // 08:00 AM (480)
    val eveningReviewMinute: Int = 20 * 60    // 08:00 PM (1200)
)
