package com.example.ui.games.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.GameDifficulty
import com.example.domain.games.engine.*
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.SemanticError
import com.example.ui.theme.SemanticSuccess
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// -------------------------------------------------------------
// GAME 4 — REACTION TEST (اختبار رد الفعل)
// -------------------------------------------------------------
enum class ReactionState { WAITING_FOR_GREEN, GREEN_TAP_NOW, TOO_EARLY, ROUND_RESULT }

@Composable
fun ReactionTestGame(
    difficulty: GameDifficulty,
    isArabic: Boolean,
    onPause: () -> Unit,
    onCorrectSound: () -> Unit,
    onWrongSound: () -> Unit,
    onFinish: (score: Int, timeMillis: Long, accuracy: Int, combo: Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var state by remember { mutableStateOf(ReactionState.WAITING_FOR_GREEN) }
    var currentRound by remember { mutableStateOf(1) }
    val totalRounds = 5
    val reactionTimes = remember { mutableStateListOf<Long>() }
    var greenTriggerTimestamp by remember { mutableStateOf(0L) }
    var lastReactionMs by remember { mutableStateOf(0L) }
    var waitJob by remember { mutableStateOf<Job?>(null) }
    val startTime = remember { System.currentTimeMillis() }

    fun startWaitingCycle() {
        state = ReactionState.WAITING_FOR_GREEN
        waitJob?.cancel()
        waitJob = coroutineScope.launch {
            val delayMs = ReactionTestEngine.getRandomWaitDelayMs()
            delay(delayMs)
            greenTriggerTimestamp = System.currentTimeMillis()
            state = ReactionState.GREEN_TAP_NOW
        }
    }

    LaunchedEffect(currentRound) {
        if (currentRound <= totalRounds) {
            startWaitingCycle()
        }
    }

    val backgroundColor = when (state) {
        ReactionState.WAITING_FOR_GREEN -> Color(0xFF1E293B) // Midnight slate
        ReactionState.GREEN_TAP_NOW -> Color(0xFF10B981) // Vibrant emerald green
        ReactionState.TOO_EARLY -> SemanticError
        ReactionState.ROUND_RESULT -> BrandBlue.copy(alpha = 0.8f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable {
                when (state) {
                    ReactionState.WAITING_FOR_GREEN -> {
                        // Tapped too early!
                        waitJob?.cancel()
                        state = ReactionState.TOO_EARLY
                        onWrongSound()
                    }
                    ReactionState.GREEN_TAP_NOW -> {
                        // Valid fast reaction!
                        val reactionMs = System.currentTimeMillis() - greenTriggerTimestamp
                        lastReactionMs = reactionMs
                        reactionTimes.add(reactionMs)
                        state = ReactionState.ROUND_RESULT
                        onCorrectSound()
                    }
                    ReactionState.TOO_EARLY -> {
                        // Restart this round attempt
                        startWaitingCycle()
                    }
                    ReactionState.ROUND_RESULT -> {
                        if (currentRound >= totalRounds) {
                            val best = reactionTimes.minOrNull() ?: 0L
                            val avg = if (reactionTimes.isNotEmpty()) reactionTimes.average().toLong() else 0L
                            val score = ReactionTestEngine.calculateScore(best, avg, difficulty)
                            val totalTime = System.currentTimeMillis() - startTime
                            onFinish(score, best, 100, reactionTimes.size)
                        } else {
                            currentRound++
                        }
                    }
                }
            }
            .testTag("reaction_test_game")
    ) {
        // Top Pause bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPause) {
                Icon(Icons.Filled.Pause, contentDescription = "Pause", tint = Color.White)
            }
            Text(
                text = "${if (isArabic) "المحاولة" else "Attempt"} $currentRound / $totalRounds",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        // Center Instruction
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (state) {
                ReactionState.WAITING_FOR_GREEN -> {
                    Icon(Icons.Filled.HourglassEmpty, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
                    Text(
                        text = if (isArabic) "انتظر حتى تتحول الشاشة للأخضر..." else "Wait for green...",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
                ReactionState.GREEN_TAP_NOW -> {
                    Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(72.dp))
                    Text(
                        text = if (isArabic) "اضغط الآن بأقصى سرعة!" else "TAP NOW!",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
                ReactionState.TOO_EARLY -> {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
                    Text(
                        text = if (isArabic) "تسرعت! ضغطت قبل اللون الأخضر" else "Too early! Tapped before green",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (isArabic) "اضغط في أي مكان للمحاولة ثانية" else "Tap anywhere to try again",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                ReactionState.ROUND_RESULT -> {
                    Text(
                        text = "$lastReactionMs ms",
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                    val grade = ReactionTestEngine.evaluateReaction(lastReactionMs)
                    Text(
                        text = if (isArabic) grade.labelAr else grade.labelEn,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BrandAmber
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isArabic) "اضغط لمتابعة الجولة التالية" else "Tap to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// GAME 6 — WORD SCRAMBLE (حروف وكلمات)
// -------------------------------------------------------------
@Composable
fun WordScrambleGame(
    difficulty: GameDifficulty,
    isArabic: Boolean,
    onPause: () -> Unit,
    onCorrectSound: () -> Unit,
    onWrongSound: () -> Unit,
    onFinish: (score: Int, timeMillis: Long, accuracy: Int, combo: Int) -> Unit
) {
    var roundIndex by remember { mutableStateOf(1) }
    val totalRounds = 5
    var currentWordObj by remember { mutableStateOf(WordScrambleEngine.getWord(isArabic, difficulty)) }
    var scrambledLetters by remember { mutableStateOf(WordScrambleEngine.scramble(currentWordObj.word).toList()) }
    var selectedIndices by remember { mutableStateOf(listOf<Int>()) }
    var hintsUsed by remember { mutableStateOf(0) }
    var showHint by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    val startTime = remember { System.currentTimeMillis() }

    val userWord = selectedIndices.map { scrambledLetters[it] }.joinToString("")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("word_scramble_game"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPause) {
                Icon(Icons.Filled.Pause, contentDescription = "Pause")
            }
            Text(
                text = "${if (isArabic) "الكلمة" else "Word"} $roundIndex / $totalRounds",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BrandAmber)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Hint Button / Box
        if (showHint) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BrandAmber.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💡 ${if (isArabic) currentWordObj.hintAr else currentWordObj.hintEn}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            TextButton(
                onClick = {
                    hintsUsed++
                    showHint = true
                }
            ) {
                Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = BrandAmber, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isArabic) "إظهار تلميح" else "Show Hint", color = BrandAmber)
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Answer Slots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val targetLength = currentWordObj.word.length
            for (i in 0 until targetLength) {
                val letter = if (i < selectedIndices.size) scrambledLetters[selectedIndices[i]].toString() else ""
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (letter.isNotEmpty()) BrandBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (letter.isNotEmpty()) CardDefaults.outlinedCardBorder() else null,
                    modifier = Modifier
                        .size(50.dp)
                        .clickable(enabled = letter.isNotEmpty()) {
                            // Tap to remove last letter
                            if (selectedIndices.isNotEmpty()) {
                                selectedIndices = selectedIndices.dropLast(1)
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = letter,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Scrambled Letter Tiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            scrambledLetters.forEachIndexed { index, letter ->
                val isUsed = selectedIndices.contains(index)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isUsed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = !isUsed) {
                            selectedIndices = selectedIndices + index
                            // Check if word complete
                            if (selectedIndices.size == currentWordObj.word.length) {
                                val formed = (selectedIndices.map { scrambledLetters[it] }).joinToString("")
                                if (formed.equals(currentWordObj.word, ignoreCase = true)) {
                                    correctCount++
                                    val pts = WordScrambleEngine.calculateScore(
                                        currentWordObj.word.length,
                                        15,
                                        hintsUsed,
                                        difficulty
                                    )
                                    score += pts
                                    onCorrectSound()

                                    if (roundIndex >= totalRounds) {
                                        val totalTime = System.currentTimeMillis() - startTime
                                        val acc = ((correctCount.toFloat() / totalRounds) * 100).toInt()
                                        onFinish(score, totalTime, acc, correctCount)
                                    } else {
                                        roundIndex++
                                        currentWordObj = WordScrambleEngine.getWord(isArabic, difficulty)
                                        scrambledLetters = WordScrambleEngine.scramble(currentWordObj.word).toList()
                                        selectedIndices = emptyList()
                                        showHint = false
                                    }
                                } else {
                                    onWrongSound()
                                }
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = letter.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isUsed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Clear button
        OutlinedButton(
            onClick = { selectedIndices = emptyList() },
            enabled = selectedIndices.isNotEmpty(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Backspace, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isArabic) "مسح" else "Clear")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// -------------------------------------------------------------
// GAME 7 — ODD ONE OUT (العنصر المختلف)
// -------------------------------------------------------------
@Composable
fun OddOneOutGame(
    difficulty: GameDifficulty,
    isArabic: Boolean,
    onPause: () -> Unit,
    onCorrectSound: () -> Unit,
    onWrongSound: () -> Unit,
    onFinish: (score: Int, timeMillis: Long, accuracy: Int, combo: Int) -> Unit
) {
    var roundIndex by remember { mutableStateOf(1) }
    val totalRounds = 8
    var roundData by remember { mutableStateOf(OddOneOutEngine.generateRound(difficulty)) }
    var score by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    val startTime = remember { System.currentTimeMillis() }

    val columns = when (difficulty) {
        GameDifficulty.EASY -> 3
        GameDifficulty.MEDIUM, GameDifficulty.HARD -> 4
        else -> 5
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("odd_one_out_game"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPause) {
                Icon(Icons.Filled.Pause, contentDescription = "Pause")
            }
            Text(
                text = "${if (isArabic) "الجولة" else "Round"} $roundIndex / $totalRounds",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BrandAmber)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isArabic) roundData.promptAr else roundData.promptEn,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Matrix Grid
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
                items(roundData.items.size) { index ->
                    val item = roundData.items[index]
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (item.color != Color.Unspecified) item.color else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (index == roundData.correctIndex) {
                                    correctCount++
                                    score += (150 * difficulty.multiplier).toInt()
                                    onCorrectSound()
                                } else {
                                    onWrongSound()
                                }

                                if (roundIndex >= totalRounds) {
                                    val totalTime = System.currentTimeMillis() - startTime
                                    val acc = ((correctCount.toFloat() / totalRounds) * 100).toInt()
                                    onFinish(score, totalTime, acc, correctCount)
                                } else {
                                    roundIndex++
                                    roundData = OddOneOutEngine.generateRound(difficulty)
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (item.text.isNotEmpty()) {
                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            } else if (item.iconKey.isNotEmpty()) {
                                val vec = when (item.iconKey) {
                                    "star" -> Icons.Filled.Star
                                    "favorite" -> Icons.Filled.Favorite
                                    "circle" -> Icons.Filled.Circle
                                    "square" -> Icons.Filled.Square
                                    "bolt" -> Icons.Filled.Bolt
                                    "diamond" -> Icons.Filled.Diamond
                                    "change_history" -> Icons.Filled.ChangeHistory
                                    else -> Icons.Filled.Emergency
                                }
                                Icon(
                                    imageVector = vec,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// GAME 9 — COLOR & WORD (STROOP CHALLENGE)
// -------------------------------------------------------------
@Composable
fun ColorAndWordGame(
    difficulty: GameDifficulty,
    isArabic: Boolean,
    onPause: () -> Unit,
    onCorrectSound: () -> Unit,
    onWrongSound: () -> Unit,
    onFinish: (score: Int, timeMillis: Long, accuracy: Int, combo: Int) -> Unit
) {
    var roundIndex by remember { mutableStateOf(1) }
    val totalRounds = 12
    var roundData by remember { mutableStateOf(ColorAndWordEngine.generateRound(difficulty)) }
    var score by remember { mutableStateOf(0) }
    var combo by remember { mutableStateOf(0) }
    var maxCombo by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    val startTime = remember { System.currentTimeMillis() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("color_and_word_game"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPause) {
                Icon(Icons.Filled.Pause, contentDescription = "Pause")
            }
            Text(
                text = "${if (isArabic) "الجولة" else "Round"} $roundIndex / $totalRounds",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BrandAmber)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dynamic Rule Prompt (Crucial for Stroop task)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = BrandAmber.copy(alpha = 0.2f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "⚠️ ${if (isArabic) roundData.instructionMode.promptAr else roundData.instructionMode.promptEn}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Large Stroop Word Display with Ink Color
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isArabic) roundData.item.wordTextAr else roundData.item.wordTextEn,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = roundData.item.inkColor
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.8f))

        // 4 Options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val options = if (isArabic) roundData.optionsAr else roundData.optionsEn
            val chunked = options.chunked(2)
            chunked.forEach { rowOpts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowOpts.forEach { opt ->
                        val isCorrect = options.indexOf(opt) == roundData.correctIndex
                        Button(
                            onClick = {
                                if (isCorrect) {
                                    correctCount++
                                    combo++
                                    maxCombo = maxOf(maxCombo, combo)
                                    score += (120 * difficulty.multiplier * (1.0f + combo * 0.1f)).toInt()
                                    onCorrectSound()
                                } else {
                                    combo = 0
                                    onWrongSound()
                                }

                                if (roundIndex >= totalRounds) {
                                    val totalTime = System.currentTimeMillis() - startTime
                                    val acc = ((correctCount.toFloat() / totalRounds) * 100).toInt()
                                    onFinish(score, totalTime, acc, maxCombo)
                                } else {
                                    roundIndex++
                                    roundData = ColorAndWordEngine.generateRound(difficulty)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                        ) {
                            Text(
                                text = opt,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
