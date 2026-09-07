package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.AppLanguage
import com.example.data.local.model.Priority
import com.example.data.local.model.TaskEntity
import com.example.domain.life.*
import com.example.ui.theme.*

@Composable
fun LifeEngineHeroCard(
    lifeState: LifeEngineState?,
    language: AppLanguage,
    onStartAction: (TaskEntity) -> Unit,
    onCompleteAction: (TaskEntity) -> Unit,
    onSnoozeAction: (Int) -> Unit,
    onRescheduleAction: (TaskEntity) -> Unit,
    onOpenLifeDashboard: () -> Unit,
    onRecalculateDay: () -> Unit,
    onUndoRecalculate: () -> Unit,
    onToggleRescueMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = language == AppLanguage.ARABIC
    var showSnoozeMenu by remember { mutableStateOf(false) }

    val recommendation = lifeState?.recommendation
    val score = lifeState?.dailyScore
    val plan = lifeState?.adaptivePlan
    val rescue = lifeState?.rescuePlan
    val isRescueActive = lifeState?.isRescueModeActive == true

    // Dynamic Gradient Border
    val heroBrush = Brush.horizontalGradient(
        colors = if (isRescueActive) {
            listOf(Color(0xFFE53935), Color(0xFFFFB74D), Color(0xFFE53935))
        } else {
            listOf(SapphirePrimary, WarmAmberGold, SapphireAccent)
        }
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.5.dp, heroBrush, RoundedCornerShape(24.dp))
            .clickable { onOpenLifeDashboard() }
            .testTag("life_engine_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Life Engine Badge + Daily Score Pill + Full Dashboard Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRescueActive) Color(0xFFFFEBEE)
                                else SapphirePrimary.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRescueActive) Icons.Filled.Warning else Icons.Filled.Psychology,
                            contentDescription = "Life Engine",
                            tint = if (isRescueActive) Color(0xFFD32F2F) else SapphirePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (isArabic) "محرك الحياة YAWMEK" else "YAWMEK LIFE ENGINE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = if (isRescueActive) Color(0xFFD32F2F) else SapphirePrimary
                            )
                            if (isRescueActive) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFD32F2F),
                                    modifier = Modifier.padding(start = 2.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) "إنقاذ" else "RESCUE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isArabic) "ماذا أفعل الآن؟" else "What should I do now?",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Daily Score Chip
                if (score != null) {
                    val tierColor = when (score.tier) {
                        DailyScoreTier.MASTERY -> EmeraldGreen
                        DailyScoreTier.MOMENTUM -> SapphirePrimary
                        DailyScoreTier.FOCUSED -> WarmAmberGold
                        DailyScoreTier.BUILDING -> Color(0xFFFF9800)
                        DailyScoreTier.RESTART -> Color(0xFFE53935)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = tierColor.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, tierColor.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clickable { onOpenLifeDashboard() }
                            .testTag("daily_score_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${score.totalScore}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = tierColor
                            )
                            Text(
                                text = score.tier.name.take(3),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = tierColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recommendation Card Body
            if (recommendation != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Title & Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = recommendation.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (recommendation.estimatedDurationMinutes > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = "${recommendation.estimatedDurationMinutes}m",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                val priColor = when (recommendation.priority) {
                                    Priority.HIGH -> Color(0xFFD32F2F)
                                    Priority.MEDIUM -> WarmAmberGold
                                    Priority.LOW -> EmeraldGreen
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = priColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = recommendation.priority.name,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = priColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Reason text
                        Text(
                            text = if (isArabic) recommendation.reasonArabic else recommendation.reasonEnglish,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        // Expected Impact statement
                        val impact = if (isArabic) recommendation.expectedImpactArabic else recommendation.expectedImpactEnglish
                        if (impact.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = WarmAmberGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = impact,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = WarmAmberGold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (recommendation.task != null && recommendation.canStart) {
                        Button(
                            onClick = { onStartAction(recommendation.task) },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("life_start_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SapphirePrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 12.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArabic) "بدء التركيز" else "Start Focus",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    if (recommendation.task != null && recommendation.canComplete) {
                        FilledTonalButton(
                            onClick = { onCompleteAction(recommendation.task) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("life_complete_button"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 10.dp)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArabic) "إنجاز" else "Done",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // Snooze Dropdown
                    if (recommendation.canSnooze) {
                        Box {
                            OutlinedButton(
                                onClick = { showSnoozeMenu = true },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                modifier = Modifier.testTag("life_snooze_button")
                            ) {
                                Icon(Icons.Outlined.Snooze, contentDescription = "Snooze", modifier = Modifier.size(16.dp))
                            }

                            DropdownMenu(
                                expanded = showSnoozeMenu,
                                onDismissRequest = { showSnoozeMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (isArabic) "+١٥ دقيقة" else "+15 minutes") },
                                    onClick = {
                                        showSnoozeMenu = false
                                        onSnoozeAction(15)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isArabic) "+٣٠ دقيقة" else "+30 minutes") },
                                    onClick = {
                                        showSnoozeMenu = false
                                        onSnoozeAction(30)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isArabic) "+ساعة واحدة" else "+1 hour") },
                                    onClick = {
                                        showSnoozeMenu = false
                                        onSnoozeAction(60)
                                    }
                                )
                            }
                        }
                    }

                    // Recalculate / Undo Button
                    if (plan?.canUndo == true) {
                        IconButton(
                            onClick = { onUndoRecalculate() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Filled.Undo, contentDescription = "Undo Plan", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        IconButton(
                            onClick = { onRecalculateDay() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Recalculate Day", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Rescue Mode Notice / Quick Toggle Banner
            if (rescue != null && (rescue.isTriggered || isRescueActive)) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isRescueActive) Color(0xFFFFEBEE) else Color(0xFFFFF3E0),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleRescueMode() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.HealthAndSafety,
                                contentDescription = null,
                                tint = if (isRescueActive) Color(0xFFD32F2F) else Color(0xFFEF6C00),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isRescueActive) {
                                    if (isArabic) "وضع الإنقاذ نشط: تم تأمين ${rescue.mustDoTasks.size} مهام إلزامية"
                                    else "Rescue Active: ${rescue.mustDoTasks.size} Must-Do tasks isolated"
                                } else {
                                    if (isArabic) "الجدول مضغوط! انقر لتفعيل وضع الإنقاذ"
                                    else "Schedule tight! Tap to activate Rescue Mode"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isRescueActive) Color(0xFFD32F2F) else Color(0xFFEF6C00),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = if (isArabic) "عرض الخطة" else "View Plan",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = if (isRescueActive) Color(0xFFD32F2F) else Color(0xFFEF6C00)
                        )
                    }
                }
            }
        }
    }
}
