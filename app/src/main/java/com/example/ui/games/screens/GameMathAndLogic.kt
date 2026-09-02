package com.example.ui.games.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.delay

// -------------------------------------------------------------
// GAME 2 — NUMBER SPRINT (سباق الأرقام)
// -------------------------------------------------------------
@Composable
fun NumberSprintGame(
    difficulty: GameDifficulty,
    isArabic: Boolean,
    onPause: () -> Unit,
    onCorrectSound: () -> Unit,
    onWrongSound: () -> Unit,
    onFinish: (score: Int, timeMillis: Long, accuracy: Int, combo: Int) -> Unit
) {
    var timeRemainingSeconds by remember { mutableStateOf(45) }
    var currentQuestion by remember { mutableStateOf(NumberSprintEngine.generateQuestion(difficulty)) }
    var score by remember { mutableStateOf(0) }
    var combo by remember { mutableStateOf(0) }
    var maxCombo by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    var totalQuestions by remember { mutableStateOf(0) }
    val startTime = remember { System.currentTimeMillis() }

    LaunchedEffect(Unit) {
        while (timeRemainingSeconds > 0) {
            delay(1000)
            timeRemainingSeconds--
        }
        val totalTime = System.currentTimeMillis() - startTime
        val acc = if (totalQuestions > 0) ((correctCount.toFloat() / totalQuestions) * 100).toInt() else 0
        onFinish(score, totalTime, acc, maxCombo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("number_sprint_game"),
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
            // Timer Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (timeRemainingSeconds <= 10) SemanticError.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Filled.Timer,
                        contentDescription = null,
                        tint = if (timeRemainingSeconds <= 10) SemanticError else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${timeRemainingSeconds}s",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (timeRemainingSeconds <= 10) SemanticError else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
            // Score
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandAmber
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Multiplier & Combo indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (combo >= 3) BrandAmber else BrandBlue.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${if (combo >= 5) "🔥 5x" else if (combo >= 3) "⚡ 3x" else if (combo >= 1) "1.5x" else "1x"}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (combo >= 3) Color.Black else BrandBlue
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Current Math Question Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentQuestion.text,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.8f))

        // 4 Answer Choice Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val chunked = currentQuestion.options.chunked(2)
            chunked.forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowOptions.forEach { opt ->
                        Button(
                            onClick = {
                                totalQuestions++
                                if (opt == currentQuestion.answer) {
                                    correctCount++
                                    combo++
                                    maxCombo = maxOf(maxCombo, combo)
                                    onCorrectSound()
                                    val pts = NumberSprintEngine.calculatePoints(
                                        combo,
                                        difficulty,
                                        timeRemainingSeconds / 45f
                                    )
                                    score += pts
                                } else {
                                    combo = 0
                                    onWrongSound()
                                }
                                currentQuestion = NumberSprintEngine.generateQuestion(difficulty)
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                        ) {
                            Text(
                                text = "$opt",
                                style = MaterialTheme.typography.titleLarge.copy(
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

// -------------------------------------------------------------
// GAME 3 — PATTERN MASTER (سيد الأنماط)
// -------------------------------------------------------------
@Composable
fun PatternMasterGame(
    difficulty: GameDifficulty,
    isArabic: Boolean,
    onPause: () -> Unit,
    onCorrectSound: () -> Unit,
    onWrongSound: () -> Unit,
    onFinish: (score: Int, timeMillis: Long, accuracy: Int, combo: Int) -> Unit
) {
    var roundIndex by remember { mutableStateOf(1) }
    val totalRounds = 8
    var puzzle by remember { mutableStateOf(PatternMasterEngine.generatePuzzle(difficulty)) }
    var score by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    var combo by remember { mutableStateOf(0) }
    var maxCombo by remember { mutableStateOf(0) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    val startTime = remember { System.currentTimeMillis() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("pattern_master_game"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPause) {
                Icon(Icons.Filled.Pause, contentDescription = "Pause")
            }
            Text(
                text = "${if (isArabic) "النمط" else "Pattern"} $roundIndex / $totalRounds",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BrandAmber)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Instructions
        Text(
            text = if (isArabic) "اكتشف القاعدة وأكمل العنصر المفقود (؟)" else "Identify the rule and choose the missing item (?)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sequence Display Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            puzzle.sequence.forEach { item ->
                val isQuestion = item == "?"
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isQuestion) BrandAmber.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isQuestion) CardDefaults.outlinedCardBorder() else null,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isQuestion) BrandAmber else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Rule feedback if answered
        feedbackMessage?.let { msg ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val chunked = puzzle.options.chunked(2)
            chunked.forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowOptions.forEach { opt ->
                        Button(
                            onClick = {
                                if (opt == puzzle.answer) {
                                    correctCount++
                                    combo++
                                    maxCombo = maxOf(maxCombo, combo)
                                    score += (150 * difficulty.multiplier * (1.0f + combo * 0.1f)).toInt()
                                    onCorrectSound()
                                } else {
                                    combo = 0
                                    onWrongSound()
                                }
                                feedbackMessage = if (isArabic) puzzle.ruleExplanationAr else puzzle.ruleExplanationEn

                                if (roundIndex >= totalRounds) {
                                    val totalTime = System.currentTimeMillis() - startTime
                                    val acc = ((correctCount.toFloat() / totalRounds) * 100).toInt()
                                    onFinish(score, totalTime, acc, maxCombo)
                                } else {
                                    roundIndex++
                                    puzzle = PatternMasterEngine.generatePuzzle(difficulty)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                        ) {
                            Text(
                                text = opt,
                                style = MaterialTheme.typography.titleLarge.copy(
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

// -------------------------------------------------------------
// GAME 8 — LOGIC LOCK (قفل المنطق)
// -------------------------------------------------------------
@Composable
fun LogicLockGame(
    difficulty: GameDifficulty,
    isArabic: Boolean,
    onPause: () -> Unit,
    onCorrectSound: () -> Unit,
    onWrongSound: () -> Unit,
    onFinish: (score: Int, timeMillis: Long, accuracy: Int, combo: Int) -> Unit
) {
    var roundIndex by remember { mutableStateOf(0) }
    val totalRounds = 5
    var puzzle by remember { mutableStateOf(LogicLockEngine.getPuzzle(difficulty, roundIndex)) }
    var score by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    var explanationToShow by remember { mutableStateOf<String?>(null) }
    val startTime = remember { System.currentTimeMillis() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("logic_lock_game"),
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
                text = "${if (isArabic) "اللغز" else "Puzzle"} ${roundIndex + 1} / $totalRounds",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BrandAmber)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Puzzle Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (isArabic) puzzle.questionAr else puzzle.questionEn,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        explanationToShow?.let { exp ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SemanticSuccess.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💡 $exp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 4 Options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val options = if (isArabic) puzzle.optionsAr else puzzle.optionsEn
            options.forEachIndexed { idx, opt ->
                Button(
                    onClick = {
                        if (idx == puzzle.correctIndex) {
                            correctCount++
                            score += (200 * difficulty.multiplier).toInt()
                            onCorrectSound()
                        } else {
                            onWrongSound()
                        }
                        explanationToShow = if (isArabic) puzzle.explanationAr else puzzle.explanationEn

                        if (roundIndex >= totalRounds - 1) {
                            val totalTime = System.currentTimeMillis() - startTime
                            val acc = ((correctCount.toFloat() / totalRounds) * 100).toInt()
                            onFinish(score, totalTime, acc, correctCount)
                        } else {
                            roundIndex++
                            puzzle = LogicLockEngine.getPuzzle(difficulty, roundIndex)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = opt,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// -------------------------------------------------------------
// GAME 10 — QUICK LOGIC (المنطق السريع)
// -------------------------------------------------------------
@Composable
fun QuickLogicGame(
    difficulty: GameDifficulty,
    isArabic: Boolean,
    onPause: () -> Unit,
    onCorrectSound: () -> Unit,
    onWrongSound: () -> Unit,
    onFinish: (score: Int, timeMillis: Long, accuracy: Int, combo: Int) -> Unit
) {
    var roundIndex by remember { mutableStateOf(0) }
    val totalRounds = 5
    var question by remember { mutableStateOf(QuickLogicEngine.getQuestion(roundIndex)) }
    var score by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    val startTime = remember { System.currentTimeMillis() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("quick_logic_game"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPause) {
                Icon(Icons.Filled.Pause, contentDescription = "Pause")
            }
            Text(
                text = "${if (isArabic) "السؤال" else "Question"} ${roundIndex + 1} / $totalRounds",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BrandAmber)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = if (isArabic) question.questionAr else question.questionEn,
                    style = MaterialTheme.typography.titleMedium.copy(lineHeight = 26.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val options = if (isArabic) question.optionsAr else question.optionsEn
            options.forEachIndexed { idx, opt ->
                Button(
                    onClick = {
                        if (idx == question.correctIndex) {
                            correctCount++
                            score += (200 * difficulty.multiplier).toInt()
                            onCorrectSound()
                        } else {
                            onWrongSound()
                        }
                        if (roundIndex >= totalRounds - 1) {
                            val totalTime = System.currentTimeMillis() - startTime
                            val acc = ((correctCount.toFloat() / totalRounds) * 100).toInt()
                            onFinish(score, totalTime, acc, correctCount)
                        } else {
                            roundIndex++
                            question = QuickLogicEngine.getQuestion(roundIndex)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = opt,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
