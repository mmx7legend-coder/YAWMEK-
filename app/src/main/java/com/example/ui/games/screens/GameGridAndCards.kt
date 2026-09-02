package com.example.ui.games.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.local.model.GameDifficulty
import com.example.domain.games.engine.*
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.SemanticError
import com.example.ui.theme.SemanticSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// -------------------------------------------------------------
// GAME 1 — MEMORY GRID (شبكة الذاكرة)
// -------------------------------------------------------------
@Composable
fun MemoryGridGame(
    difficulty: GameDifficulty,
    isArabic: Boolean,
    onPause: () -> Unit,
    onCorrectSound: () -> Unit,
    onWrongSound: () -> Unit,
    onFinish: (score: Int, timeMillis: Long, accuracy: Int, combo: Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val config = remember(difficulty) { MemoryGridEngine.getConfig(difficulty) }
    val gridSize = config.gridSize
    val totalTiles = gridSize * gridSize

    var currentRound by remember { mutableStateOf(1) }
    var sequenceLength by remember { mutableStateOf(config.initialSequenceLength) }
    var activeSequence by remember { mutableStateOf(listOf<Int>()) }
    var userIndex by remember { mutableStateOf(0) }
    var activeFlashingTile by remember { mutableStateOf<Int?>(null) }
    var isShowingSequence by remember { mutableStateOf(true) }
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var totalAttempts by remember { mutableStateOf(0) }
    var successfulRounds by remember { mutableStateOf(0) }
    val startTime = remember { System.currentTimeMillis() }

    // Start round animation
    fun startNewRound(newLength: Int) {
        coroutineScope.launch {
            isShowingSequence = true
            userIndex = 0
            val seq = MemoryGridEngine.generateSequence(gridSize, newLength)
            activeSequence = seq
            delay(500)
            for (tile in seq) {
                activeFlashingTile = tile
                delay(config.flashDurationMs)
                activeFlashingTile = null
                delay(config.pauseBetweenFlashMs)
            }
            isShowingSequence = false
        }
    }

    LaunchedEffect(Unit) {
        startNewRound(sequenceLength)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("memory_grid_game"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPause) {
                Icon(Icons.Filled.Pause, contentDescription = "Pause")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${if (isArabic) "الجولة" else "Round"} $currentRound",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${if (isArabic) "النقاط:" else "Score:"} $score",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandAmber
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = BrandBlue.copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (isArabic) difficulty.labelAr else difficulty.labelEn,
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandBlue,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Instructional Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isShowingSequence) BrandAmber.copy(alpha = 0.15f) else SemanticSuccess.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isShowingSequence) {
                    if (isArabic) "👀 راقب تسلسل وميض المربعات..." else "👀 Watch the sequence light up..."
                } else {
                    if (isArabic) "👈 اضغط على المربعات بنفس الترتيب!" else "👈 Reproduce the sequence now!"
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // The Grid of Tiles
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridSize),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                items(totalTiles) { index ->
                    val isFlashing = activeFlashingTile == index
                    val tileColor = when {
                        isFlashing -> BrandAmber
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = tileColor,
                        border = if (isFlashing) null else CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = !isShowingSequence) {
                                totalAttempts++
                                if (activeSequence.isNotEmpty() && userIndex < activeSequence.size) {
                                    if (activeSequence[userIndex] == index) {
                                        // Correct tile
                                        userIndex++
                                        onCorrectSound()
                                        if (userIndex == activeSequence.size) {
                                            // Round completed successfully!
                                            successfulRounds++
                                            streak++
                                            val roundScore = (sequenceLength * 150 * difficulty.multiplier).toInt()
                                            score += roundScore
                                            if (currentRound >= 5) {
                                                // Game completed
                                                val totalTime = System.currentTimeMillis() - startTime
                                                val acc = ((successfulRounds.toFloat() / currentRound) * 100).toInt()
                                                onFinish(score, totalTime, acc, streak)
                                            } else {
                                                currentRound++
                                                sequenceLength++
                                                startNewRound(sequenceLength)
                                            }
                                        }
                                    } else {
                                        // Wrong tile
                                        onWrongSound()
                                        streak = 0
                                        if (currentRound >= 5) {
                                            val totalTime = System.currentTimeMillis() - startTime
                                            val acc = ((successfulRounds.toFloat() / currentRound) * 100).toInt()
                                            onFinish(score, totalTime, acc, streak)
                                        } else {
                                            currentRound++
                                            startNewRound(sequenceLength)
                                        }
                                    }
                                }
                            }
                    ) {}
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sequence Progress Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(activeSequence.size) { i ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (i < userIndex) SemanticSuccess else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

// -------------------------------------------------------------
// GAME 5 — MEMORY MATCH (تطابق الذاكرة)
// -------------------------------------------------------------
@Composable
fun MemoryMatchGame(
    difficulty: GameDifficulty,
    isArabic: Boolean,
    onPause: () -> Unit,
    onCorrectSound: () -> Unit,
    onWrongSound: () -> Unit,
    onFinish: (score: Int, timeMillis: Long, accuracy: Int, combo: Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var cards by remember { mutableStateOf(MemoryMatchEngine.generateCards(difficulty)) }
    var firstFlippedIndex by remember { mutableStateOf<Int?>(null) }
    var isProcessingFlip by remember { mutableStateOf(false) }
    var moves by remember { mutableStateOf(0) }
    var matchedPairs by remember { mutableStateOf(0) }
    val totalPairs = remember(difficulty) { MemoryMatchEngine.getPairsCount(difficulty) }
    val startTime = remember { System.currentTimeMillis() }
    var elapsedSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (matchedPairs < totalPairs) {
            delay(1000)
            elapsedSeconds++
        }
    }

    val columns = when (difficulty) {
        GameDifficulty.EASY -> 3 // 8 cards (or 2x4)
        GameDifficulty.MEDIUM -> 3 // 12 cards (3x4)
        GameDifficulty.HARD -> 4 // 20 cards (4x5)
        GameDifficulty.EXPERT -> 4 // 24 cards (4x6)
        GameDifficulty.MASTER -> 5 // 30 cards (5x6)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("memory_match_game"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPause) {
                Icon(Icons.Filled.Pause, contentDescription = "Pause")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "${if (isArabic) "الأزواج:" else "Pairs:"} $matchedPairs/$totalPairs",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${if (isArabic) "الحركات:" else "Moves:"} $moves",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandAmber
                )
                Text(
                    text = "${elapsedSeconds}s",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = BrandBlue.copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (isArabic) difficulty.labelAr else difficulty.labelEn,
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandBlue,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card Grid
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                items(cards.size) { index ->
                    val card = cards[index]
                    val isRevealed = card.isFaceUp || card.isMatched

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            card.isMatched -> SemanticSuccess.copy(alpha = 0.2f)
                            isRevealed -> BrandBlue.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = if (card.isMatched) CardDefaults.outlinedCardBorder() else null,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isProcessingFlip && !isRevealed) {
                                moves++
                                val newCards = cards.toMutableList()
                                newCards[index] = card.copy(isFaceUp = true)
                                cards = newCards

                                if (firstFlippedIndex == null) {
                                    firstFlippedIndex = index
                                } else {
                                    val firstIdx = firstFlippedIndex!!
                                    firstFlippedIndex = null
                                    if (cards[firstIdx].pairKey == card.pairKey) {
                                        // Match!
                                        onCorrectSound()
                                        val matchedCards = cards.toMutableList()
                                        matchedCards[firstIdx] = matchedCards[firstIdx].copy(isMatched = true)
                                        matchedCards[index] = matchedCards[index].copy(isMatched = true)
                                        cards = matchedCards
                                        matchedPairs++

                                        if (matchedPairs == totalPairs) {
                                            val totalTime = System.currentTimeMillis() - startTime
                                            val finalScore = MemoryMatchEngine.calculateScore(
                                                totalPairs,
                                                moves,
                                                elapsedSeconds,
                                                difficulty
                                            )
                                            val accuracy = ((totalPairs.toFloat() * 2 / moves.coerceAtLeast(1)) * 100)
                                                .toInt()
                                                .coerceIn(20, 100)
                                            onFinish(finalScore, totalTime, accuracy, totalPairs)
                                        }
                                    } else {
                                        // No match, flip back
                                        onWrongSound()
                                        isProcessingFlip = true
                                        coroutineScope.launch {
                                            delay(700)
                                            val resetCards = cards.toMutableList()
                                            resetCards[firstIdx] = resetCards[firstIdx].copy(isFaceUp = false)
                                            resetCards[index] = resetCards[index].copy(isFaceUp = false)
                                            cards = resetCards
                                            isProcessingFlip = false
                                        }
                                    }
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isRevealed) {
                                val vector = when (card.iconName) {
                                    "psychology" -> Icons.Filled.Psychology
                                    "star" -> Icons.Filled.Star
                                    "bolt" -> Icons.Filled.Bolt
                                    "favorite" -> Icons.Filled.Favorite
                                    "rocket_launch" -> Icons.Filled.RocketLaunch
                                    "lightbulb" -> Icons.Filled.Lightbulb
                                    "local_fire_department" -> Icons.Filled.LocalFireDepartment
                                    "explore" -> Icons.Filled.Explore
                                    "flag" -> Icons.Filled.Flag
                                    "shield" -> Icons.Filled.Shield
                                    "extension" -> Icons.Filled.Extension
                                    "auto_awesome" -> Icons.Filled.AutoAwesome
                                    "diamond" -> Icons.Filled.Diamond
                                    "lock" -> Icons.Filled.Lock
                                    else -> Icons.Filled.Pets
                                }
                                Icon(
                                    imageVector = vector,
                                    contentDescription = null,
                                    tint = if (card.isMatched) SemanticSuccess else BrandBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            } else {
                                Text(
                                    text = "?",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
