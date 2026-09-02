package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    // Stats
    @Query("SELECT * FROM game_stats")
    fun getAllGameStats(): Flow<List<GameStatsEntity>>

    @Query("SELECT * FROM game_stats WHERE gameId = :gameId")
    suspend fun getGameStats(gameId: String): GameStatsEntity?

    @Query("SELECT * FROM game_stats WHERE gameId = :gameId")
    fun getGameStatsFlow(gameId: String): Flow<GameStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGameStats(stats: GameStatsEntity)

    // Sessions & Local Leaderboard
    @Query("SELECT * FROM game_sessions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 10): Flow<List<GameSessionEntity>>

    @Query("SELECT * FROM game_sessions WHERE gameId = :gameId ORDER BY timestamp DESC")
    fun getSessionsForGame(gameId: String): Flow<List<GameSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameSessionEntity): Long

    @Query("SELECT * FROM game_sessions WHERE timestamp >= :startOfDayMillis ORDER BY score DESC LIMIT :limit")
    fun getTopScoresToday(startOfDayMillis: Long, limit: Int = 10): Flow<List<GameSessionEntity>>

    @Query("SELECT * FROM game_sessions WHERE timestamp >= :startOfWeekMillis ORDER BY score DESC LIMIT :limit")
    fun getTopScoresThisWeek(startOfWeekMillis: Long, limit: Int = 10): Flow<List<GameSessionEntity>>

    @Query("SELECT * FROM game_sessions ORDER BY score DESC LIMIT :limit")
    fun getTopScoresAllTime(limit: Int = 10): Flow<List<GameSessionEntity>>

    // Achievements
    @Query("SELECT * FROM game_achievements")
    fun getAllUnlockedAchievements(): Flow<List<GameAchievementEntity>>

    @Query("SELECT * FROM game_achievements")
    suspend fun getUnlockedAchievementsList(): List<GameAchievementEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockAchievement(achievement: GameAchievementEntity)

    @Query("SELECT COUNT(*) FROM game_achievements WHERE achievementId = :achievementId")
    suspend fun isAchievementUnlocked(achievementId: String): Int

    // Daily Challenge
    @Query("SELECT * FROM game_daily_challenges WHERE dateEpochDay = :dateEpochDay")
    fun getDailyChallenge(dateEpochDay: Long): Flow<GameDailyChallengeEntity?>

    @Query("SELECT * FROM game_daily_challenges WHERE dateEpochDay = :dateEpochDay")
    suspend fun getDailyChallengeDirect(dateEpochDay: Long): GameDailyChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyChallenge(challenge: GameDailyChallengeEntity)

    @Update
    suspend fun updateDailyChallenge(challenge: GameDailyChallengeEntity)

    // Preferences & Progression
    @Query("SELECT * FROM game_preferences WHERE id = 1")
    fun getGamePreferences(): Flow<GamePreferencesEntity?>

    @Query("SELECT * FROM game_preferences WHERE id = 1")
    suspend fun getGamePreferencesDirect(): GamePreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreferences(prefs: GamePreferencesEntity)
}
