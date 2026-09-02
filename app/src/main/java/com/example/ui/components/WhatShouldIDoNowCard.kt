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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.AppLanguage
import com.example.data.local.model.TaskEntity
import com.example.domain.RecommendationResult
import com.example.domain.UrgencyLevel
import com.example.ui.theme.*

@Composable
fun WhatShouldIDoNowHeroCard(
    recommendation: RecommendationResult?,
    language: AppLanguage,
    onStartAction: (TaskEntity) -> Unit,
    onCompleteAction: (TaskEntity) -> Unit,
    onRescheduleAction: (TaskEntity) -> Unit,
    onChooseAnother: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val isArabic = language == AppLanguage.ARABIC

    val headerTitle = if (isArabic) "أعمل إيه دلوقتي؟" else "What should I do now?"
    val headerSubtitle = if (isArabic) "المقترح الأنسب لجدولك وطاقتك الآن" else "Smart recommendation based on your day"

    // Gradient border for hero emphasis
    val heroBrush = Brush.horizontalGradient(
        colors = listOf(
            BrandBlue,
            BrandAmberLight,
            ColorHabit
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.5.dp, heroBrush, RoundedCornerShape(24.dp))
            .testTag("what_should_i_do_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
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
                            .background(BrandAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = BrandAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = headerTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = headerSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (recommendation?.urgencyLevel != null) {
                    val (badgeText, badgeBg, badgeTextCol) = when (recommendation.urgencyLevel) {
                        UrgencyLevel.OVERDUE -> Triple(if (isArabic) "متأخر" else "Overdue", SemanticErrorContainer, SemanticError)
                        UrgencyLevel.URGENT -> Triple(if (isArabic) "أولوية قصوى" else "Urgent", BrandAmber.copy(alpha = 0.2f), BrandAmber)
                        UrgencyLevel.HIGH_PRIORITY -> Triple(if (isArabic) "مهم جداً" else "High Priority", BrandBlue.copy(alpha = 0.15f), BrandBlue)
                        UrgencyLevel.GOOD_TIMING -> Triple(if (isArabic) "وقت مثالي" else "Good Timing", ColorHabit.copy(alpha = 0.15f), ColorHabit)
                        UrgencyLevel.MINDFUL_PAUSE -> Triple(if (isArabic) "استراحة" else "All Done", SemanticSuccessContainer, SemanticSuccess)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = badgeTextCol
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (recommendation == null || (recommendation.task == null && recommendation.habit == null && recommendation.urgencyLevel == UrgencyLevel.MINDFUL_PAUSE)) {
                // All caught up state
                Text(
                    text = if (isArabic) "أنت منجز لكل مهامك الحالية!" else "You are completely on top of your day.",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isArabic)
                        "لا توجد مهام عاجلة الآن. خذ استراحة مستحقة، أو خطط لمهام الغد."
                    else
                        "No urgent tasks pending. Take a mindful break or set up your next milestone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Recommended Task / Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = recommendation.title,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏱ ~${recommendation.estimatedDurationMinutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "•",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = recommendation.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // "Why this?" Explanation Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = BrandAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isArabic) recommendation.reasonArabic else recommendation.reasonEnglish,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row
                if (recommendation.task != null) {
                    val task = recommendation.task
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onCompleteAction(task) },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "إتمام" else "Mark Done",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        OutlinedButton(
                            onClick = { onRescheduleAction(task) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Text(
                                text = if (isArabic) "تأجيل" else "Reschedule",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        if (recommendation.alternatives.isNotEmpty()) {
                            IconButton(
                                onClick = onChooseAnother,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Shuffle,
                                    contentDescription = if (isArabic) "اقتراح آخر" else "Choose another",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
