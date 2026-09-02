package com.example.ui.games

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.model.*
import com.example.data.repository.GameRepository
import com.example.domain.games.GameAchievementDef
import com.example.domain.games.GameAchievementsCatalog
import com.example.domain.games.GameSoundHaptics
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class GameResultData(
    val gameId: GameId,
    val score: Int,
    val bestScore: Int,
    val isNewRecord: Boolean,
    val timeMillis: Long,
    val accuracy: Int,
    val combo: Int,
    val difficulty: GameDifficulty,
    val xpEarned: Int
)

data class GamesUiState(
    val allStats: Map<String, GameStatsEntity> = emptyMap(),
    val recentSessions: List<GameSessionEntity> = emptyList(),
    val unlockedAchievementIds: Set<String> = emptySet(),
    val preferences: GamePreferencesEntity = GamePreferencesEntity(),
    val todayChallenge: GameDailyChallengeEntity? = null,
    val selectedCategory: GameCategory = GameCategory.ALL,
    val activeGame: GameId? = null,
    val activeDifficulty: GameDifficulty = GameDifficulty.MEDIUM,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val currentScore: Int = 0,
    val currentCombo: Int = 0,
    val lastResult: GameResultData? = null,
    val latestUnlockedAchievement: GameAchievementDef? = null,
    val topScoresToday: List<GameSessionEntity> = emptyList(),
    val topScoresThisWeek: List<GameSessionEntity> = emptyList(),
    val topScoresAllTime: List<GameSessionEntity> = emptyList()
)

class GamesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    val soundHaptics: GameSoundHaptics = GameSoundHaptics(application)

    private val _uiState = MutableStateFlow(GamesUiState())
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = GameRepository(db)

        val todayEpochDay = LocalDate.now().toEpochDay()
        ensureDailyChallenge(todayEpochDay)

        viewModelScope.launch {
            repository.allGameStats.collect { statsList ->
                val map = statsList.associateBy { it.gameId }
                _uiState.update { it.copy(allStats = map) }
            }
        }

        viewModelScope.launch {
            repository.recentSessions.collect { sessions ->
                _uiState.update { it.copy(recentSessions = sessions) }
            }
        }

        viewModelScope.launch {
            repository.unlockedAchievements.collect { achList ->
                val ids = achList.map { it.achievementId }.toSet()
                _uiState.update { it.copy(unlockedAchievementIds = ids) }
            }
        }

        viewModelScope.launch {
            repository.gamePreferences.collect { prefs ->
                val p = prefs ?: GamePreferencesEntity()
                _uiState.update { it.copy(preferences = p) }
            }
        }

        viewModelScope.launch {
            repository.getDailyChallengeFlow(todayEpochDay).collect { challenge ->
                _uiState.update { it.copy(todayChallenge = challenge) }
            }
        }

        // Leaderboard queries
        val startOfDayMillis = LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val startOfWeekMillis = LocalDate.now().minusDays(7).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

        viewModelScope.launch {
            repository.getTopScoresToday(startOfDayMillis).collect { topToday ->
                _uiState.update { it.copy(topScoresToday = topToday) }
            }
        }

        viewModelScope.launch {
            repository.getTopScoresThisWeek(startOfWeekMillis).collect { topWeek ->
                _uiState.update { it.copy(topScoresThisWeek = topWeek) }
            }
        }

        viewModelScope.launch {
            repository.getTopScoresAllTime().collect { topAllTime ->
                _uiState.update { it.copy(topScoresAllTime = topAllTime) }
            }
        }
    }

    private fun ensureDailyChallenge(todayEpochDay: Long) {
        viewModelScope.launch {
            val existing = repository.getDailyChallenge(todayEpochDay)
            if (existing == null) {
                val gameEntries = GameId.entries
                val deterministicGameIndex = (todayEpochDay % gameEntries.size).toInt()
                val selectedGame = gameEntries[deterministicGameIndex]
                val diffEntries = listOf(GameDifficulty.MEDIUM, GameDifficulty.HARD)
                val selectedDiff = diffEntries[((todayEpochDay / 3) % diffEntries.size).toInt()]
                val target = when (selectedGame) {
                    GameId.MEMORY_GRID -> 600
                    GameId.NUMBER_SPRINT -> 850
                    GameId.PATTERN_MASTER -> 750
                    GameId.REACTION_TEST -> 500
                    GameId.MEMORY_MATCH -> 700
                    GameId.WORD_SCRAMBLE -> 650
                    GameId.ODD_ONE_OUT -> 600
                    GameId.LOGIC_LOCK -> 500
                    GameId.COLOR_AND_WORD -> 800
                    GameId.QUICK_LOGIC -> 750
                }
                repository.saveDailyChallenge(
                    GameDailyChallengeEntity(
                        dateEpochDay = todayEpochDay,
                        gameId = selectedGame.idString,
                        difficulty = selectedDiff,
                        targetScore = target,
                        rewardXp = 200,
                        isCompleted = false,
                        bestScore = 0
                    )
                )
            }
        }
    }

    fun selectCategory(category: GameCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun toggleFavorite(gameIdString: String) {
        viewModelScope.launch {
            val currentPrefs = _uiState.value.preferences
            val favs = currentPrefs.favoriteGameIds.split(",").filter { it.isNotBlank() }.toMutableSet()
            if (favs.contains(gameIdString)) {
                favs.remove(gameIdString)
            } else {
                favs.add(gameIdString)
            }
            val updated = currentPrefs.copy(favoriteGameIds = favs.joinToString(","))
            repository.savePreferences(updated)
        }
    }

    fun updateSettings(sound: Boolean, haptics: Boolean, animations: Boolean, preferredDiff: GameDifficulty) {
        viewModelScope.launch {
            val updated = _uiState.value.preferences.copy(
                soundEnabled = sound,
                hapticsEnabled = haptics,
                animationsEnabled = animations,
                preferredDifficulty = preferredDiff
            )
            repository.savePreferences(updated)
        }
    }

    fun prepareAndStartGame(gameId: GameId, difficulty: GameDifficulty) {
        _uiState.update {
            it.copy(
                activeGame = gameId,
                activeDifficulty = difficulty,
                isPlaying = true,
                isPaused = false,
                currentScore = 0,
                currentCombo = 0,
                lastResult = null
            )
        }
    }

    fun pauseGame() {
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resumeGame() {
        _uiState.update { it.copy(isPaused = false) }
    }

    fun quitGame() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                isPlaying = false,
                isPaused = false,
                activeGame = null,
                currentScore = 0,
                currentCombo = 0
            )
        }
    }

    fun triggerCorrectSound() {
        val prefs = _uiState.value.preferences
        soundHaptics.playCorrect(prefs.soundEnabled, prefs.hapticsEnabled)
    }

    fun triggerWrongSound() {
        val prefs = _uiState.value.preferences
        soundHaptics.playWrong(prefs.soundEnabled, prefs.hapticsEnabled)
    }

    fun triggerRecordSound() {
        val prefs = _uiState.value.preferences
        soundHaptics.playRecord(prefs.soundEnabled, prefs.hapticsEnabled)
    }

    fun triggerTapSound() {
        val prefs = _uiState.value.preferences
        soundHaptics.playTap(prefs.soundEnabled, prefs.hapticsEnabled)
    }

    fun submitGameResult(
        gameId: GameId,
        score: Int,
        timeMillis: Long,
        accuracy: Int,
        combo: Int,
        difficulty: GameDifficulty
    ) {
        viewModelScope.launch {
            val currentStats = repository.getGameStats(gameId.idString) ?: GameStatsEntity(gameId = gameId.idString)
            val isNewRecord = score > currentStats.bestScore && score > 0

            val currentPrefs = repository.getPreferencesDirect()

            // Calculate XP: base score XP + difficulty bonus + record bonus
            val baseScoreXp = (score / 12).coerceIn(10, 250)
            val diffXp = (difficulty.level * 20)
            val recordBonus = if (isNewRecord) 50 else 0
            val totalXpEarned = baseScoreXp + diffXp + recordBonus

            // Update stats
            val newPlayed = currentStats.gamesPlayed + 1
            val newWon = currentStats.gamesWon + (if (score > 100) 1 else 0)
            val newCurrentStreak = currentStats.currentStreak + 1
            val newBestStreak = maxOf(currentStats.bestStreak, newCurrentStreak)
            val newBestScore = maxOf(currentStats.bestScore, score)
            val newBestTime = if (currentStats.bestTimeMillis == 0L || (timeMillis in 1 until currentStats.bestTimeMillis)) {
                timeMillis
            } else currentStats.bestTimeMillis
            val newTotalScore = currentStats.totalScore + score
            val newAvgScore = newTotalScore.toDouble() / newPlayed.toDouble()

            val updatedStats = currentStats.copy(
                bestScore = newBestScore,
                bestTimeMillis = newBestTime,
                gamesPlayed = newPlayed,
                gamesWon = newWon,
                currentStreak = newCurrentStreak,
                bestStreak = newBestStreak,
                favoriteDifficulty = difficulty,
                lastPlayedMillis = System.currentTimeMillis(),
                totalScore = newTotalScore,
                averageScore = newAvgScore
            )
            repository.saveGameStats(updatedStats)

            // Record session in DB
            val session = GameSessionEntity(
                gameId = gameId.idString,
                score = score,
                timeMillis = timeMillis,
                difficulty = difficulty,
                accuracy = accuracy,
                combo = combo,
                xpEarned = totalXpEarned,
                isPersonalRecord = isNewRecord
            )
            repository.recordSession(session)

            // Update Daily Game Streak & Level / XP
            val todayEpoch = LocalDate.now().toEpochDay()
            val streak = when (todayEpoch - currentPrefs.lastPlayedEpochDay) {
                0L -> currentPrefs.gameStreak.coerceAtLeast(1)
                1L -> currentPrefs.gameStreak + 1
                else -> 1
            }

            val newTotalXp = currentPrefs.userXp + totalXpEarned
            val newLevel = 1 + (newTotalXp / 300) // Level up every 300 XP

            val updatedPrefs = currentPrefs.copy(
                userXp = newTotalXp,
                userLevel = newLevel,
                gameStreak = streak,
                lastPlayedEpochDay = todayEpoch
            )
            repository.savePreferences(updatedPrefs)

            // Check Daily Challenge
            val daily = repository.getDailyChallenge(todayEpoch)
            if (daily != null && daily.gameId == gameId.idString && !daily.isCompleted) {
                if (score >= daily.targetScore) {
                    val updatedDaily = daily.copy(isCompleted = true, bestScore = maxOf(daily.bestScore, score))
                    repository.updateDailyChallenge(updatedDaily)
                    checkAndUnlockAchievement("daily_spark")
                }
            }

            // Sound feedback for victory / record
            if (isNewRecord) {
                triggerRecordSound()
            } else {
                triggerCorrectSound()
            }

            // Check Achievements
            evaluateAchievements(
                gameId = gameId,
                score = score,
                timeMillis = timeMillis,
                accuracy = accuracy,
                combo = combo,
                difficulty = difficulty,
                streak = streak,
                totalPlayed = newPlayed,
                level = newLevel,
                isRecord = isNewRecord
            )

            _uiState.update {
                it.copy(
                    isPlaying = false,
                    lastResult = GameResultData(
                        gameId = gameId,
                        score = score,
                        bestScore = newBestScore,
                        isNewRecord = isNewRecord,
                        timeMillis = timeMillis,
                        accuracy = accuracy,
                        combo = combo,
                        difficulty = difficulty,
                        xpEarned = totalXpEarned
                    )
                )
            }
        }
    }

    private suspend fun evaluateAchievements(
        gameId: GameId,
        score: Int,
        timeMillis: Long,
        accuracy: Int,
        combo: Int,
        difficulty: GameDifficulty,
        streak: Int,
        totalPlayed: Int,
        level: Int,
        isRecord: Boolean
    ) {
        checkAndUnlockAchievement("first_game")

        if (isRecord) checkAndUnlockAchievement("record_breaker")

        if (difficulty == GameDifficulty.EXPERT && score > 200) checkAndUnlockAchievement("expert_ascendant")
        if (difficulty == GameDifficulty.MASTER && score > 200) checkAndUnlockAchievement("master_of_all")

        if (streak >= 3) checkAndUnlockAchievement("streak_3")
        if (streak >= 7) checkAndUnlockAchievement("streak_7")
        if (streak >= 30) checkAndUnlockAchievement("streak_30")

        if (totalPlayed >= 10) checkAndUnlockAchievement("games_10")
        if (totalPlayed >= 50) checkAndUnlockAchievement("games_50")
        if (totalPlayed >= 100) checkAndUnlockAchievement("games_100")

        if (level >= 5) checkAndUnlockAchievement("level_5")
        if (level >= 10) checkAndUnlockAchievement("level_10")

        // Game specific
        when (gameId) {
            GameId.REACTION_TEST -> {
                if (timeMillis in 1..280) checkAndUnlockAchievement("lightning_reflex")
            }
            GameId.NUMBER_SPRINT -> {
                if (score >= 1000) checkAndUnlockAchievement("math_wizard")
            }
            GameId.PATTERN_MASTER -> {
                if (combo >= 8) checkAndUnlockAchievement("pattern_prodigy")
            }
            GameId.MEMORY_MATCH -> {
                if (accuracy >= 90 && score >= 600) checkAndUnlockAchievement("perfect_match")
            }
            GameId.WORD_SCRAMBLE -> {
                if (score >= 800) checkAndUnlockAchievement("word_smith")
            }
            GameId.ODD_ONE_OUT -> {
                if (score >= 800) checkAndUnlockAchievement("hawk_eye")
            }
            GameId.LOGIC_LOCK -> {
                if (difficulty >= GameDifficulty.EXPERT && score >= 400) checkAndUnlockAchievement("master_of_logic")
            }
            GameId.COLOR_AND_WORD -> {
                if (combo >= 12) checkAndUnlockAchievement("color_mastermind")
            }
            GameId.QUICK_LOGIC -> {
                if (score >= 1200) checkAndUnlockAchievement("quick_mind")
            }
            else -> {}
        }

        // Check Jack of All Trades (all 10 played)
        val allStats = repository.allGameStats.firstOrNull() ?: emptyList()
        val playedGameIds = allStats.filter { it.gamesPlayed > 0 }.map { it.gameId }.toSet()
        if (GameId.entries.all { playedGameIds.contains(it.idString) }) {
            checkAndUnlockAchievement("jack_of_all_trades")
        }
    }

    private suspend fun checkAndUnlockAchievement(achievementId: String) {
        if (!repository.isAchievementUnlocked(achievementId)) {
            repository.unlockAchievement(achievementId)
            val def = GameAchievementsCatalog.getDef(achievementId)
            if (def != null) {
                _uiState.update { it.copy(latestUnlockedAchievement = def) }
            }
        }
    }

    fun clearAchievementNotification() {
        _uiState.update { it.copy(latestUnlockedAchievement = null) }
    }

    fun dismissResult() {
        _uiState.update { it.copy(lastResult = null, activeGame = null) }
    }

    override fun onCleared() {
        super.onCleared()
        soundHaptics.release()
    }
}
