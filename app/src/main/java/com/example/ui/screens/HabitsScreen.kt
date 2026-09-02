package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.model.AppLanguage
import com.example.data.local.model.HabitEntity
import com.example.data.local.model.HabitLogEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QuickAddTab
import com.example.ui.theme.*
import com.example.ui.viewmodel.YawmekUiState
import java.time.LocalDate

@Composable
fun HabitsScreen(
    uiState: YawmekUiState,
    onOpenQuickAdd: (QuickAddTab) -> Unit,
    onToggleHabit: (Long, Long) -> Unit,
    onDeleteHabit: (HabitEntity) -> Unit
) {
    val language = uiState.userSettings.language
    val isArabic = language == AppLanguage.ARABIC
    val todayEpoch = LocalDate.now().toEpochDay()

    val completedTodayCount = uiState.habits.count { habit ->
        uiState.habitLogs.any { it.habitId == habit.id && it.dateEpochDay == todayEpoch }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("habits_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isArabic) "العادات اليومية" else "Daily Habits",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (uiState.habits.isNotEmpty()) {
                        Text(
                            text = if (isArabic) "$completedTodayCount من ${uiState.habits.size} عادات مكتملة اليوم" else "$completedTodayCount of ${uiState.habits.size} completed today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { onOpenQuickAdd(QuickAddTab.HABIT) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ColorHabit.copy(alpha = 0.15f))
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add Habit",
                        tint = ColorHabit
                    )
                }
            }
        }

        if (uiState.habits.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.Repeat,
                    iconTint = ColorHabit,
                    title = if (isArabic) "ابنِ عاداتك باستمرارية" else "Build consistent micro-habits",
                    subtitle = if (isArabic) "العادات الصغيرة المستمرة هي سر التغيير الكبير. أضف عادة مثل القراءة أو الرياضة اليومية." else "Small daily actions compound into life-changing results. Add your first habit.",
                    actionButtonText = if (isArabic) "+ إضافة عادة جديدة" else "+ Create a Habit",
                    onActionClick = { onOpenQuickAdd(QuickAddTab.HABIT) }
                )
            }
        } else {
            items(uiState.habits) { habit ->
                HabitDetailCard(
                    habit = habit,
                    habitLogs = uiState.habitLogs.filter { it.habitId == habit.id },
                    todayEpoch = todayEpoch,
                    language = language,
                    onToggle = { dateEpoch -> onToggleHabit(habit.id, dateEpoch) },
                    onDelete = { onDeleteHabit(habit) }
                )
            }
        }
    }
}

@Composable
fun HabitDetailCard(
    habit: HabitEntity,
    habitLogs: List<HabitLogEntity>,
    todayEpoch: Long,
    language: AppLanguage,
    onToggle: (Long) -> Unit,
    onDelete: () -> Unit
) {
    val isArabic = language == AppLanguage.ARABIC
    val isDoneToday = habitLogs.any { it.dateEpochDay == todayEpoch }

    // Calculate current streak
    val streak = calculateStreak(habitLogs, todayEpoch)

    // Last 5 days history
    val last5Days = (4 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ColorHabit.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Repeat, contentDescription = null, tint = ColorHabit, modifier = Modifier.size(18.dp))
                    }

                    Column {
                        Text(
                            text = habit.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = habit.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (streak > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandAmber.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🔥 $streak ${if (isArabic) "أيام" else "days"}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = BrandAmber
                            )
                        }
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5-Day completion dots row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                last5Days.forEach { date ->
                    val epoch = date.toEpochDay()
                    val isChecked = habitLogs.any { it.dateEpochDay == epoch }
                    val isToday = epoch == todayEpoch

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isToday) (if (isArabic) "اليوم" else "Today") else date.dayOfWeek.name.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isChecked) SemanticSuccess else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onToggle(epoch) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isChecked) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun calculateStreak(logs: List<HabitLogEntity>, todayEpoch: Long): Int {
    val sortedDays = logs.map { it.dateEpochDay }.sortedDescending()
    var streak = 0
    var currentCheck = if (sortedDays.contains(todayEpoch)) todayEpoch else todayEpoch - 1

    while (sortedDays.contains(currentCheck)) {
        streak++
        currentCheck--
    }
    return streak
}
