package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "community_profile")
data class CommunityProfileEntity(
    @PrimaryKey val id: Long = 1L,
    val username: String = "",
    val displayName: String = "",
    val avatarId: String = "warrior",
    val bio: String = "",
    val rankTitleEn: String = "Novice Adventurer",
    val rankTitleAr: String = "مغامر مبتدئ",
    val rating: Int = 1000,
    val battleWins: Int = 0,
    val battleLosses: Int = 0,
    val connectionId: String = "",
    val isOnline: Boolean = true,
    val isInitialSetupDone: Boolean = false,
    val lastActiveMillis: Long = System.currentTimeMillis(),
    val createdAtMillis: Long = System.currentTimeMillis()
)

enum class FriendshipStatus {
    FRIEND,
    PENDING_SENT,
    PENDING_RECEIVED,
    BLOCKED
}

@Entity(tableName = "community_friends")
data class CommunityFriendEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val friendUsername: String,
    val friendDisplayName: String,
    val avatarId: String = "warrior",
    val level: Int = 1,
    val xp: Long = 0L,
    val rankTitle: String = "Novice",
    val characterClass: String = "WARRIOR",
    val isOnline: Boolean = false,
    val status: FriendshipStatus = FriendshipStatus.FRIEND,
    val rating: Int = 1000,
    val tasksCompleted: Int = 0,
    val focusMinutes: Int = 0,
    val lastActiveMillis: Long = System.currentTimeMillis()
)

enum class BattleType(val titleEn: String, val titleAr: String, val iconName: String) {
    RPG_ARENA("RPG Arena Duel", "مبارزة حلبة RPG", "shield"),
    FOCUS_SHOWDOWN("Focus Showdown", "تحدي التركيز العميق", "timer"),
    TASK_RACE("Task Blitz Race", "سباق إنجاز المهام", "task_alt")
}

enum class BattleResult {
    VICTORY,
    DEFEAT,
    DRAW
}

@Entity(tableName = "community_battles")
data class CommunityBattleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val opponentUsername: String,
    val opponentDisplayName: String,
    val opponentAvatarId: String = "warrior",
    val opponentLevel: Int = 1,
    val battleType: BattleType = BattleType.RPG_ARENA,
    val myScore: Int = 0,
    val opponentScore: Int = 0,
    val result: BattleResult = BattleResult.VICTORY,
    val ratingDelta: Int = 25,
    val xpGained: Long = 50L,
    val coinsGained: Int = 30,
    val summaryLog: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
)

data class LeaderboardUserItem(
    val rank: Int,
    val username: String,
    val displayName: String,
    val avatarId: String,
    val level: Int,
    val rating: Int,
    val focusMinutes: Int,
    val battleWins: Int,
    val isCurrentUser: Boolean = false
)
