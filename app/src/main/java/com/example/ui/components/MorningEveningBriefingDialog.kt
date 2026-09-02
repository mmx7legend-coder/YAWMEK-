package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.model.AppLanguage
import com.example.data.local.model.TaskEntity
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.ColorHabit
import com.example.ui.theme.SemanticSuccess

@Composable
fun MorningBriefingDialog(
    userName: String,
    language: AppLanguage,
    todayTasksCount: Int,
    highPriorityCount: Int,
    remainingBudget: Double,
    currency: String,
    onDismiss: () -> Unit
) {
    val isArabic = language == AppLanguage.ARABIC
    val greeting = if (isArabic) "صباح الخير يا $userName 👋" else "Good morning, $userName 👋"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(BrandAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = null,
                        tint = BrandAmber,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isArabic) "إليك نظرة سريعة على بداية يومك" else "Here is your morning outlook",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Cards
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BriefingRow(
                        icon = Icons.Filled.CheckCircle,
                        iconTint = BrandBlue,
                        title = if (isArabic) "المهام المجدولة اليوم" else "Today's Tasks",
                        value = "$todayTasksCount"
                    )

                    if (highPriorityCount > 0) {
                        BriefingRow(
                            icon = Icons.Filled.PriorityHigh,
                            iconTint = BrandAmber,
                            title = if (isArabic) "مهام بأولوية قصوى" else "High Priority",
                            value = "$highPriorityCount"
                        )
                    }

                    if (remainingBudget > 0) {
                        BriefingRow(
                            icon = Icons.Filled.AccountBalanceWallet,
                            iconTint = SemanticSuccess,
                            title = if (isArabic) "المتبقي من الميزانية" else "Budget Remaining",
                            value = "$remainingBudget $currency"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (isArabic) "انطلق في يومك" else "Let's Seize The Day")
                }
            }
        }
    }
}

@Composable
fun EveningSummaryDialog(
    userName: String,
    language: AppLanguage,
    completedTasks: List<TaskEntity>,
    unfinishedTasks: List<TaskEntity>,
    todaySpent: Double,
    currency: String,
    onMoveUnfinishedToTomorrow: () -> Unit,
    onDismiss: () -> Unit
) {
    val isArabic = language == AppLanguage.ARABIC
    val greeting = if (isArabic) "مساء الخير يا $userName 🌙" else "Good evening, $userName 🌙"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(ColorHabit.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.NightsStay,
                        contentDescription = null,
                        tint = ColorHabit,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isArabic) "مراجعة إنجازات اليوم" else "Daily evening review",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BriefingRow(
                        icon = Icons.Filled.Check,
                        iconTint = SemanticSuccess,
                        title = if (isArabic) "مهام تم إنجازها" else "Tasks Completed",
                        value = "${completedTasks.size}"
                    )

                    BriefingRow(
                        icon = Icons.Filled.AccountBalanceWallet,
                        iconTint = BrandBlue,
                        title = if (isArabic) "إجمالي مصاريف اليوم" else "Today's Spending",
                        value = "$todaySpent $currency"
                    )

                    if (unfinishedTasks.isNotEmpty()) {
                        BriefingRow(
                            icon = Icons.Filled.PendingActions,
                            iconTint = BrandAmber,
                            title = if (isArabic) "مهام متبقية" else "Unfinished Tasks",
                            value = "${unfinishedTasks.size}"
                        )
                    }
                }

                if (unfinishedTasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "نقل المهام المتبقية إلى الغد؟" else "Move unfinished tasks to tomorrow?",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isArabic) "تلقائياً تحديث موعد ${unfinishedTasks.size} مهام لتبدأ بها غداً بنشاط." else "Reschedule ${unfinishedTasks.size} items to start fresh tomorrow.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    onMoveUnfinishedToTomorrow()
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(if (isArabic) "نعم، انقلها للغد" else "Yes, Move to Tomorrow")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (isArabic) "إغلاق المراجعة" else "Close Review")
                }
            }
        }
    }
}

@Composable
private fun BriefingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
