package com.example.ui.games.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.model.*
import com.example.domain.games.GameAchievementsCatalog
import com.example.ui.games.GameResultData
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.SemanticSuccess

// -------------------------------------------------------------
// 1. GAME START / DETAILS DIALOG
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameStartDialog(
    game: GameId,
    stats: GameStatsEntity?,
    isArabic: Boolean,
    initialDifficulty: GameDifficulty,
    onDismiss: () -> Unit,
    onStartGame: (GameDifficulty) -> Unit
) {
    var selectedDifficulty by remember { mutableStateOf(initialDifficulty) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("game_start_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(BrandAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SportsEsports,
                        contentDescription = null,
                        tint = BrandAmber,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isArabic) game.titleAr else game.titleEn,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isArabic) game.descriptionAr else game.descriptionEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats preview row
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isArabic) "أفضل رقم" else "Best Score",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${stats?.bestScore ?: 0}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BrandAmber
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isArabic) "المدة المقدرة" else "Est. Time",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "~${game.estimatedMinutes} ${if (isArabic) "د" else "m"}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isArabic) "مرات اللعب" else "Played",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${stats?.gamesPlayed ?: 0}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Difficulty Selector
                Text(
                    text = if (isArabic) "مستوى الصعوبة" else "Select Difficulty",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GameDifficulty.entries.forEach { diff ->
                        val isSelected = selectedDifficulty == diff
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDifficulty = diff },
                            label = {
                                Text(
                                    text = if (isArabic) diff.labelAr else diff.labelEn,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandAmber,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isArabic) "إلغاء" else "Cancel")
                    }

                    Button(
                        onClick = { onStartGame(selectedDifficulty) },
                        modifier = Modifier.weight(1.3f).testTag("start_game_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "ابدأ اللعب" else "Play Now",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. GAME RESULT DIALOG
// -------------------------------------------------------------
@Composable
fun GameResultDialog(
    result: GameResultData,
    isArabic: Boolean,
    onPlayAgain: () -> Unit,
    onChangeDifficulty: () -> Unit,
    onBackToGames: () -> Unit,
    onBackToMyDay: () -> Unit
) {
    Dialog(onDismissRequest = onBackToGames) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("game_result_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // New Record Banner
                if (result.isNewRecord) {
                    Surface(
                        color = BrandAmber,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isArabic) "🏆 رقم قياسي شخصي جديد!" else "🏆 NEW PERSONAL RECORD!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Text(
                    text = if (isArabic) result.gameId.titleAr else result.gameId.titleEn,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Score Display
                Text(
                    text = "${result.score}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (result.isNewRecord) BrandAmber else BrandBlue
                    )
                )
                Text(
                    text = if (isArabic) "النقاط المحققة" else "Final Score",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Stats breakdown grid
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isArabic) "أفضل رقم سابق" else "Personal Best", style = MaterialTheme.typography.bodyMedium)
                            Text("${result.bestScore}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isArabic) "مستوى الصعوبة" else "Difficulty", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (isArabic) result.difficulty.labelAr else result.difficulty.labelEn,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isArabic) "الدقة" else "Accuracy", style = MaterialTheme.typography.bodyMedium)
                            Text("${result.accuracy}%", fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isArabic) "نقاط الخبرة المكتسبة" else "XP Earned", style = MaterialTheme.typography.bodyMedium)
                            Text("+${result.xpEarned} XP", fontWeight = FontWeight.Bold, color = SemanticSuccess)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier.fillMaxWidth().testTag("play_again_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Icon(Icons.Filled.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isArabic) "العب مجدداً" else "Play Again", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onChangeDifficulty,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isArabic) "تغيير الصعوبة" else "Difficulty", fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = onBackToGames,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isArabic) "قائمة الألعاب" else "All Games", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Subtle productivity return option
                TextButton(
                    onClick = onBackToMyDay,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "جاهز للإنجاز؟ العودة إلى يومي" else "Ready to get things done? Back to my day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. PAUSE DIALOG
// -------------------------------------------------------------
@Composable
fun GamePauseDialog(
    isArabic: Boolean,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isArabic) "اللعبة متوقفة مؤقتاً ⏸️" else "Game Paused ⏸️",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (isArabic) "استرح لثوانٍ ثم واصل تمرين عقلك" else "Take a breath and continue when you're ready",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isArabic) "استئناف اللعب" else "Resume")
                }

                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isArabic) "إعادة المحاولة" else "Restart")
                }

                TextButton(
                    onClick = onQuit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isArabic) "الخروج من اللعبة" else "Exit Game", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. GAME SETTINGS DIALOG
// -------------------------------------------------------------
@Composable
fun GameSettingsDialog(
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    animationsEnabled: Boolean,
    preferredDifficulty: GameDifficulty,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSave: (sound: Boolean, haptics: Boolean, animations: Boolean, diff: GameDifficulty) -> Unit
) {
    var sound by remember { mutableStateOf(soundEnabled) }
    var haptics by remember { mutableStateOf(hapticsEnabled) }
    var animations by remember { mutableStateOf(animationsEnabled) }
    var diff by remember { mutableStateOf(preferredDifficulty) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = if (isArabic) "إعدادات الألعاب" else "Game Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (isArabic) "المؤثرات الصوتية" else "Sound Effects", fontWeight = FontWeight.Medium)
                        Text(if (isArabic) "أصوات هادئة للإجابات" else "Subtle audio cues", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = sound, onCheckedChange = { sound = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (isArabic) "الاهتزاز والتفاعل اللمسي" else "Haptic Feedback", fontWeight = FontWeight.Medium)
                        Text(if (isArabic) "اهتزاز لطيف عند الإجابة" else "Vibration on answers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = haptics, onCheckedChange = { haptics = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (isArabic) "المؤثرات البصرية" else "Animations", fontWeight = FontWeight.Medium)
                        Text(if (isArabic) "حركات ناعمة للبطاقات" else "Smooth transitions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = animations, onCheckedChange = { animations = it })
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isArabic) "إلغاء" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(sound, haptics, animations, diff)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text(if (isArabic) "حفظ" else "Save")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. ACHIEVEMENTS SHEET / DIALOG
// -------------------------------------------------------------
@Composable
fun AchievementsDialog(
    unlockedIds: Set<String>,
    isArabic: Boolean,
    onDismiss: () -> Unit
) {
    val allAchievements = GameAchievementsCatalog.allAchievements
    val unlockedCount = unlockedIds.size

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "أوسمة الإنجاز" else "Achievements",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "$unlockedCount / ${allAchievements.size} ${if (isArabic) "مفتوح" else "Unlocked"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandAmber
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allAchievements) { ach ->
                        val isUnlocked = unlockedIds.contains(ach.id)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isUnlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = if (isUnlocked) CardDefaults.outlinedCardBorder() else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = if (isUnlocked) ach.iconEmoji else "🔒",
                                    fontSize = 28.sp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isArabic) ach.titleAr else ach.titleEn,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (isArabic) ach.descriptionAr else ach.descriptionEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isUnlocked) SemanticSuccess.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = "+${ach.xpBonus} XP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isUnlocked) SemanticSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. LOCAL LEADERBOARD DIALOG
// -------------------------------------------------------------
@Composable
fun LeaderboardDialog(
    topToday: List<GameSessionEntity>,
    topWeek: List<GameSessionEntity>,
    topAllTime: List<GameSessionEntity>,
    isArabic: Boolean,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Today, 1: This Week, 2: All Time

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "أرقامك القياسية" else "Personal Leaderboard",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isArabic) "ترتيب أفضل جولاتك محلياً" else "Your top recorded scores",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(if (isArabic) "اليوم" else "Today") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(if (isArabic) "هذا الأسبوع" else "This Week") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(if (isArabic) "كل الأوقات" else "All Time") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val listToShow = when (selectedTab) {
                    0 -> topToday
                    1 -> topWeek
                    else -> topAllTime
                }

                if (listToShow.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isArabic) "لم تسجل أي جولة في هذه الفترة بعد.\nابدأ لعبة الآن لتحرز أول رقم!" else "No scores recorded in this period yet.\nPlay a game to set your first score!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listToShow.size) { index ->
                            val item = listToShow[index]
                            val gameDef = GameId.fromId(item.gameId)
                            val rankBadge = when (index) {
                                0 -> "🥇"
                                1 -> "🥈"
                                2 -> "🥉"
                                else -> "#${index + 1}"
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = rankBadge,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.width(36.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isArabic) gameDef.titleAr else gameDef.titleEn,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${if (isArabic) item.difficulty.labelAr else item.difficulty.labelEn} • ${item.accuracy}% ${if (isArabic) "دقة" else "acc"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${item.score}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = BrandAmber
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
