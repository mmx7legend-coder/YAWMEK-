package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class GameRepository(private val db: AppDatabase) {

    private val gameDao = db.gameDao()

    // Stats
    val allGameStats: Flow<List<GameStatsEntity>> = gameDao.getAllGameStats()
    suspend fun getGameStats(gameId: String): GameStatsEntity? = gameDao.getGameStats(gameId)
    fun getGameStatsFlow(gameId: String): Flow<GameStatsEntity?> = gameDao.getGameStatsFlow(gameId)
    suspend fun saveGameStats(stats: GameStatsEntity) = gameDao.insertOrUpdateGameStats(stats)

    // Sessions
    val recentSessions: Flow<List<GameSessionEntity>> = gameDao.getRecentSessions(15)
    fun getSessionsForGame(gameId: String): Flow<List<GameSessionEntity>> = gameDao.getSessionsForGame(gameId)
    suspend fun recordSession(session: GameSessionEntity): Long = gameDao.insertSession(session)

    // Local Leaderboard
    fun getTopScoresToday(startOfDayMillis: Long): Flow<List<GameSessionEntity>> = gameDao.getTopScoresToday(startOfDayMillis)
    fun getTopScoresThisWeek(startOfWeekMillis: Long): Flow<List<GameSessionEntity>> = gameDao.getTopScoresThisWeek(startOfWeekMillis)
    fun getTopScoresAllTime(): Flow<List<GameSessionEntity>> = gameDao.getTopScoresAllTime(10)

    // Achievements
    val unlockedAchievements: Flow<List<GameAchievementEntity>> = gameDao.getAllUnlockedAchievements()
    suspend fun getUnlockedAchievementsList(): List<GameAchievementEntity> = gameDao.getUnlockedAchievementsList()
    suspend fun unlockAchievement(achievementId: String) {
        gameDao.unlockAchievement(GameAchievementEntity(achievementId = achievementId))
    }
    suspend fun isAchievementUnlocked(achievementId: String): Boolean = gameDao.isAchievementUnlocked(achievementId) > 0

    // Daily Challenge
    fun getDailyChallengeFlow(epochDay: Long): Flow<GameDailyChallengeEntity?> = gameDao.getDailyChallenge(epochDay)
    suspend fun getDailyChallenge(epochDay: Long): GameDailyChallengeEntity? = gameDao.getDailyChallengeDirect(epochDay)
    suspend fun saveDailyChallenge(challenge: GameDailyChallengeEntity) = gameDao.insertDailyChallenge(challenge)
    suspend fun updateDailyChallenge(challenge: GameDailyChallengeEntity) = gameDao.updateDailyChallenge(challenge)

    // Preferences & Progression
    val gamePreferences: Flow<GamePreferencesEntity?> = gameDao.getGamePreferences()
    suspend fun getPreferencesDirect(): GamePreferencesEntity = gameDao.getGamePreferencesDirect() ?: GamePreferencesEntity()
    suspend fun savePreferences(prefs: GamePreferencesEntity) = gameDao.insertOrUpdatePreferences(prefs)
}
