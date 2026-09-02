package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.AppLanguage
import com.example.data.local.model.GoalEntity
import com.example.data.local.model.MilestoneEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QuickAddTab
import com.example.ui.theme.ColorGoal
import com.example.ui.theme.SemanticSuccess
import com.example.ui.viewmodel.YawmekUiState

@Composable
fun GoalsScreen(
    uiState: YawmekUiState,
    onOpenQuickAdd: (QuickAddTab) -> Unit,
    onToggleMilestone: (MilestoneEntity) -> Unit,
    onDeleteGoal: (GoalEntity) -> Unit
) {
    val language = uiState.userSettings.language
    val isArabic = language == AppLanguage.ARABIC

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("goals_screen"),
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
                Text(
                    text = if (isArabic) "الأهداف والمسار" else "Goals & Milestones",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = { onOpenQuickAdd(QuickAddTab.GOAL) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ColorGoal.copy(alpha = 0.15f))
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add Goal",
                        tint = ColorGoal
                    )
                }
            }
        }

        if (uiState.goals.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.Flag,
                    iconTint = ColorGoal,
                    title = if (isArabic) "حدد أهدافك وطموحاتك" else "Set meaningful goals",
                    subtitle = if (isArabic) "قسّم أهدافك الكبيرة إلى محطات إنجاز واضحة وقابلة للتحقيق بسهولة." else "Break down ambitious dreams into concrete, bite-sized milestones.",
                    actionButtonText = if (isArabic) "+ إضافة هدف جديد" else "+ Create a Goal",
                    onActionClick = { onOpenQuickAdd(QuickAddTab.GOAL) }
                )
            }
        } else {
            items(uiState.goals) { goal ->
                val goalMilestones = uiState.milestones.filter { it.goalId == goal.id }
                GoalCard(
                    goal = goal,
                    milestones = goalMilestones,
                    language = language,
                    onToggleMilestone = onToggleMilestone,
                    onDelete = { onDeleteGoal(goal) }
                )
            }
        }
    }
}

@Composable
fun GoalCard(
    goal: GoalEntity,
    milestones: List<MilestoneEntity>,
    language: AppLanguage,
    onToggleMilestone: (MilestoneEntity) -> Unit,
    onDelete: () -> Unit
) {
    val isArabic = language == AppLanguage.ARABIC
    val completedCount = milestones.count { it.isCompleted }
    val totalCount = milestones.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                            .background(ColorGoal.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Flag, contentDescription = null, tint = ColorGoal, modifier = Modifier.size(18.dp))
                    }

                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = goal.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }

            if (goal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (totalCount > 0) {
                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ColorGoal,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isArabic) "$completedCount من $totalCount محطات مكتملة" else "$completedCount of $totalCount milestones completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ColorGoal
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Milestones checklist
                milestones.forEach { milestone ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = milestone.isCompleted,
                            onCheckedChange = { onToggleMilestone(milestone) },
                            colors = CheckboxDefaults.colors(checkedColor = ColorGoal)
                        )
                        Text(
                            text = milestone.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = if (milestone.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (milestone.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
