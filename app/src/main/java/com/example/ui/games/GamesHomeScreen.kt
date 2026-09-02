package com.example.ui.games

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.model.*
import com.example.ui.games.components.*
import com.example.ui.games.screens.*
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.SemanticSuccess

@Composable
fun GamesHomeScreen(
    isArabic: Boolean,
    onBackToMyDay: () -> Unit,
    viewModel: GamesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedGameForStart by remember { mutableStateOf<GameId?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }
    var showLeaderboardDialog by remember { mutableStateOf(false) }

    // Floating Achievement Unlock Banner
    uiState.latestUnlockedAchievement?.let { ach ->
        LaunchedEffect(ach) {
            kotlinx.coroutines.delay(3500)
            viewModel.clearAchievementNotification()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // If a game is active and playing, show that game screen
        if (uiState.isPlaying && uiState.activeGame != null) {
            val gameId = uiState.activeGame!!
            val diff = uiState.activeDifficulty

            when (gameId) {
                GameId.MEMORY_GRID -> MemoryGridGame(
                    difficulty = diff,
                    isArabic = isArabic,
                    onPause = { viewModel.pauseGame() },
                    onCorrectSound = { viewModel.triggerCorrectSound() },
                    onWrongSound = { viewModel.triggerWrongSound() },
                    onFinish = { score, time, acc, combo ->
                        viewModel.submitGameResult(gameId, score, time, acc, combo, diff)
                    }
                )
                GameId.NUMBER_SPRINT -> NumberSprintGame(
                    difficulty = diff,
                    isArabic = isArabic,
                    onPause = { viewModel.pauseGame() },
                    onCorrectSound = { viewModel.triggerCorrectSound() },
                    onWrongSound = { viewModel.triggerWrongSound() },
                    onFinish = { score, time, acc, combo ->
                        viewModel.submitGameResult(gameId, score, time, acc, combo, diff)
                    }
                )
                GameId.PATTERN_MASTER -> PatternMasterGame(
                    difficulty = diff,
                    isArabic = isArabic,
                    onPause = { viewModel.pauseGame() },
                    onCorrectSound = { viewModel.triggerCorrectSound() },
                    onWrongSound = { viewModel.triggerWrongSound() },
                    onFinish = { score, time, acc, combo ->
                        viewModel.submitGameResult(gameId, score, time, acc, combo, diff)
                    }
                )
                GameId.REACTION_TEST -> ReactionTestGame(
                    difficulty = diff,
                    isArabic = isArabic,
                    onPause = { viewModel.pauseGame() },
                    onCorrectSound = { viewModel.triggerCorrectSound() },
                    onWrongSound = { viewModel.triggerWrongSound() },
                    onFinish = { score, time, acc, combo ->
                        viewModel.submitGameResult(gameId, score, time, acc, combo, diff)
                    }
                )
                GameId.MEMORY_MATCH -> MemoryMatchGame(
                    difficulty = diff,
                    isArabic = isArabic,
                    onPause = { viewModel.pauseGame() },
                    onCorrectSound = { viewModel.triggerCorrectSound() },
                    onWrongSound = { viewModel.triggerWrongSound() },
                    onFinish = { score, time, acc, combo ->
                        viewModel.submitGameResult(gameId, score, time, acc, combo, diff)
                    }
                )
                GameId.WORD_SCRAMBLE -> WordScrambleGame(
                    difficulty = diff,
                    isArabic = isArabic,
                    onPause = { viewModel.pauseGame() },
                    onCorrectSound = { viewModel.triggerCorrectSound() },
                    onWrongSound = { viewModel.triggerWrongSound() },
                    onFinish = { score, time, acc, combo ->
                        viewModel.submitGameResult(gameId, score, time, acc, combo, diff)
                    }
                )
                GameId.ODD_ONE_OUT -> OddOneOutGame(
                    difficulty = diff,
                    isArabic = isArabic,
                    onPause = { viewModel.pauseGame() },
                    onCorrectSound = { viewModel.triggerCorrectSound() },
                    onWrongSound = { viewModel.triggerWrongSound() },
                    onFinish = { score, time, acc, combo ->
                        viewModel.submitGameResult(gameId, score, time, acc, combo, diff)
                    }
                )
                GameId.LOGIC_LOCK -> LogicLockGame(
                    difficulty = diff,
                    isArabic = isArabic,
                    onPause = { viewModel.pauseGame() },
                    onCorrectSound = { viewModel.triggerCorrectSound() },
                    onWrongSound = { viewModel.triggerWrongSound() },
                    onFinish = { score, time, acc, combo ->
                        viewModel.submitGameResult(gameId, score, time, acc, combo, diff)
                    }
                )
                GameId.COLOR_AND_WORD -> ColorAndWordGame(
                    difficulty = diff,
                    isArabic = isArabic,
                    onPause = { viewModel.pauseGame() },
                    onCorrectSound = { viewModel.triggerCorrectSound() },
                    onWrongSound = { viewModel.triggerWrongSound() },
                    onFinish = { score, time, acc, combo ->
                        viewModel.submitGameResult(gameId, score, time, acc, combo, diff)
                    }
                )
                GameId.QUICK_LOGIC -> QuickLogicGame(
                    difficulty = diff,
                    isArabic = isArabic,
                    onPause = { viewModel.pauseGame() },
                    onCorrectSound = { viewModel.triggerCorrectSound() },
                    onWrongSound = { viewModel.triggerWrongSound() },
                    onFinish = { score, time, acc, combo ->
                        viewModel.submitGameResult(gameId, score, time, acc, combo, diff)
                    }
                )
            }
        } else {
            // Main Games Dashboard
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("games_home_screen"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "ألعاب يومك" else "YAWMEK GAMES",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isArabic) "شغّل دماغك. وحط رقمًا قياسيًا جديدًا." else "Train your brain. Beat your best.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { showLeaderboardDialog = true }) {
                                Icon(Icons.Filled.Leaderboard, contentDescription = "Leaderboard", tint = BrandAmber)
                            }
                            IconButton(onClick = { showAchievementsDialog = true }) {
                                Icon(Icons.Filled.EmojiEvents, contentDescription = "Achievements", tint = BrandAmber)
                            }
                            IconButton(onClick = { showSettingsDialog = true }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            }
                        }
                    }
                }

                // Today's Daily Challenge Card
                uiState.todayChallenge?.let { challenge ->
                    item {
                        val gameDef = GameId.fromId(challenge.gameId)
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(text = "🔥", fontSize = 20.sp)
                                        Text(
                                            text = if (isArabic) "تحدي اليوم" else "Today's Challenge",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    if (challenge.isCompleted) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = SemanticSuccess.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = if (isArabic) "مكتمل ✓" else "Completed ✓",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = SemanticSuccess,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (isArabic) gameDef.titleAr else gameDef.titleEn,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${if (isArabic) "المستوى المطلوب: " else "Target: "}${challenge.targetScore} ${if (isArabic) "نقطة" else "pts"} • +${challenge.rewardXp} XP",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = { selectedGameForStart = gameDef },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                                    ) {
                                        Text(if (isArabic) "ابدأ" else "Play", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Stats Row
                item {
                    val prefs = uiState.preferences
                    val totalPlayed = uiState.allStats.values.sumOf { it.gamesPlayed }
                    val highestBest = uiState.allStats.values.maxOfOrNull { it.bestScore } ?: 0

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🔥 ${prefs.gameStreak}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BrandAmber
                                )
                                Text(
                                    text = if (isArabic) "التتابع" else "Streak",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "⭐ Lvl ${prefs.userLevel}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BrandBlue
                                )
                                Text(
                                    text = "${prefs.userXp} XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🎮 $totalPlayed",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (isArabic) "جولة" else "Played",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🏆 $highestBest",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BrandAmber
                                )
                                Text(
                                    text = if (isArabic) "أعلى رقم" else "Best Score",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Category Filter Chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(GameCategory.entries) { cat ->
                            val isSelected = uiState.selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectCategory(cat) },
                                label = {
                                    Text(
                                        text = if (isArabic) cat.labelAr else cat.labelEn,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Games List
                val favoriteIds = uiState.preferences.favoriteGameIds.split(",").filter { it.isNotBlank() }.toSet()
                val filteredGames = GameId.entries.filter {
                    uiState.selectedCategory == GameCategory.ALL || it.category == uiState.selectedCategory
                }

                items(filteredGames) { game ->
                    val stat = uiState.allStats[game.idString]
                    val isFavorite = favoriteIds.contains(game.idString)

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedGameForStart = game }
                            .testTag("game_card_${game.idString}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Category & Game Icon Badge
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(BrandBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (game.iconKey) {
                                        "grid_view" -> Icons.Filled.GridView
                                        "calculate" -> Icons.Filled.Calculate
                                        "schema" -> Icons.Filled.Schema
                                        "bolt" -> Icons.Filled.Bolt
                                        "style" -> Icons.Filled.Style
                                        "spellcheck" -> Icons.Filled.Spellcheck
                                        "visibility" -> Icons.Filled.Visibility
                                        "lock" -> Icons.Filled.Lock
                                        "palette" -> Icons.Filled.Palette
                                        else -> Icons.Filled.DynamicFeed
                                    },
                                    contentDescription = null,
                                    tint = BrandBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) game.titleAr else game.titleEn,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                    ) {
                                        Text(
                                            text = if (isArabic) game.category.labelAr else game.category.labelEn,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = if (isArabic) game.descriptionAr else game.descriptionEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "${if (isArabic) "أفضل رقم:" else "Best:"} ${stat?.bestScore ?: 0}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = BrandAmber
                                    )
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "~${game.estimatedMinutes} ${if (isArabic) "د" else "min"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Favorite Toggle & Play Action
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.toggleFavorite(game.idString) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFavorite) SemanticSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Button(
                                    onClick = { selectedGameForStart = game },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                                ) {
                                    Text(if (isArabic) "العب" else "Play", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Achievement Notification Popup Banner
        AnimatedVisibility(
            visible = uiState.latestUnlockedAchievement != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            uiState.latestUnlockedAchievement?.let { ach ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BrandAmber,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = ach.iconEmoji, fontSize = 28.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "🎉 وسام جديد مفتوح!" else "🎉 Achievement Unlocked!",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black
                            )
                            Text(
                                text = if (isArabic) ach.titleAr else ach.titleEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            text = "+${ach.xpBonus} XP",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Start / Details Dialog
        selectedGameForStart?.let { game ->
            val stat = uiState.allStats[game.idString]
            GameStartDialog(
                game = game,
                stats = stat,
                isArabic = isArabic,
                initialDifficulty = stat?.favoriteDifficulty ?: uiState.preferences.preferredDifficulty,
                onDismiss = { selectedGameForStart = null },
                onStartGame = { diff ->
                    selectedGameForStart = null
                    viewModel.prepareAndStartGame(game, diff)
                }
            )
        }

        // Result Dialog
        uiState.lastResult?.let { result ->
            GameResultDialog(
                result = result,
                isArabic = isArabic,
                onPlayAgain = {
                    viewModel.prepareAndStartGame(result.gameId, result.difficulty)
                },
                onChangeDifficulty = {
                    val nextDiff = when (result.difficulty) {
                        GameDifficulty.EASY -> GameDifficulty.MEDIUM
                        GameDifficulty.MEDIUM -> GameDifficulty.HARD
                        GameDifficulty.HARD -> GameDifficulty.EXPERT
                        GameDifficulty.EXPERT -> GameDifficulty.MASTER
                        GameDifficulty.MASTER -> GameDifficulty.EASY
                    }
                    viewModel.prepareAndStartGame(result.gameId, nextDiff)
                },
                onBackToGames = {
                    viewModel.dismissResult()
                },
                onBackToMyDay = {
                    viewModel.dismissResult()
                    onBackToMyDay()
                }
            )
        }

        // Pause Dialog
        if (uiState.isPaused) {
            GamePauseDialog(
                isArabic = isArabic,
                onResume = { viewModel.resumeGame() },
                onRestart = {
                    val active = uiState.activeGame
                    val diff = uiState.activeDifficulty
                    if (active != null) {
                        viewModel.prepareAndStartGame(active, diff)
                    }
                },
                onQuit = { viewModel.quitGame() }
            )
        }

        // Settings Dialog
        if (showSettingsDialog) {
            GameSettingsDialog(
                soundEnabled = uiState.preferences.soundEnabled,
                hapticsEnabled = uiState.preferences.hapticsEnabled,
                animationsEnabled = uiState.preferences.animationsEnabled,
                preferredDifficulty = uiState.preferences.preferredDifficulty,
                isArabic = isArabic,
                onDismiss = { showSettingsDialog = false },
                onSave = { s, h, a, d ->
                    viewModel.updateSettings(s, h, a, d)
                }
            )
        }

        // Achievements Dialog
        if (showAchievementsDialog) {
            AchievementsDialog(
                unlockedIds = uiState.unlockedAchievementIds,
                isArabic = isArabic,
                onDismiss = { showAchievementsDialog = false }
            )
        }

        // Leaderboard Dialog
        if (showLeaderboardDialog) {
            LeaderboardDialog(
                topToday = uiState.topScoresToday,
                topWeek = uiState.topScoresThisWeek,
                topAllTime = uiState.topScoresAllTime,
                isArabic = isArabic,
                onDismiss = { showLeaderboardDialog = false }
            )
        }
    }
}
