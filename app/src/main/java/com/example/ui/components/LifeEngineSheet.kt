package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.AppLanguage
import com.example.data.local.model.Priority
import com.example.data.local.model.TaskEntity
import com.example.domain.life.*
import com.example.ui.theme.*

private enum class LifeSheetTab {
    TACTICAL,
    TIMELINE,
    RESCUE,
    INTELLIGENCE,
    INSIGHTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeEngineSheet(
    lifeState: LifeEngineState?,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onStartTask: (TaskEntity) -> Unit,
    onCompleteTask: (TaskEntity) -> Unit,
    onSnoozeTask: (Int) -> Unit,
    onRecalculateDay: () -> Unit,
    onUndoRecalculate: () -> Unit,
    onToggleRescueMode: (Boolean) -> Unit,
    onApplyRescuePlan: () -> Unit,
    onSetTimeConstraint: (Int?) -> Unit,
    onApplyHabitRestructuring: (Long, Int?, Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = language == AppLanguage.ARABIC
    var selectedTab by remember { mutableStateOf(LifeSheetTab.TACTICAL) }

    val rec = lifeState?.recommendation
    val plan = lifeState?.adaptivePlan
    val rescue = lifeState?.rescuePlan
    val score = lifeState?.dailyScore
    val goalHabit = lifeState?.goalHabitReport
    val personalization = lifeState?.personalization
    val isRescueActive = lifeState?.isRescueModeActive == true

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("life_engine_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp)
        ) {
            // Header
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRescueActive) Color(0xFFFFEBEE)
                                else SapphirePrimary.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRescueActive) Icons.Filled.HealthAndSafety else Icons.Filled.Psychology,
                            contentDescription = null,
                            tint = if (isRescueActive) Color(0xFFD32F2F) else SapphirePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isArabic) "محرك الحياة YAWMEK" else "YAWMEK LIFE ENGINE",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isArabic) "نظام التشغيل الذكي ليومك" else "Adaptive Decision Engine",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                LifeSheetTab.values().forEach { tab ->
                    val title = when (tab) {
                        LifeSheetTab.TACTICAL -> if (isArabic) "المقترح" else "Action"
                        LifeSheetTab.TIMELINE -> if (isArabic) "الجدول" else "Timeline"
                        LifeSheetTab.RESCUE -> if (isArabic) "الإنقاذ" else "Rescue"
                        LifeSheetTab.INTELLIGENCE -> if (isArabic) "الذكاء" else "Score"
                        LifeSheetTab.INSIGHTS -> if (isArabic) "الأنماط" else "Insights"
                    }
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Content
            when (selectedTab) {
                LifeSheetTab.TACTICAL -> TacticalSection(
                    rec = rec,
                    isArabic = isArabic,
                    onStartTask = onStartTask,
                    onCompleteTask = onCompleteTask,
                    onSnoozeTask = onSnoozeTask,
                    onSetTimeConstraint = onSetTimeConstraint
                )
                LifeSheetTab.TIMELINE -> TimelineSection(
                    plan = plan,
                    isArabic = isArabic,
                    onRecalculateDay = onRecalculateDay,
                    onUndoRecalculate = onUndoRecalculate
                )
                LifeSheetTab.RESCUE -> RescueSection(
                    rescue = rescue,
                    isRescueActive = isRescueActive,
                    isArabic = isArabic,
                    onToggleRescueMode = onToggleRescueMode,
                    onApplyRescuePlan = onApplyRescuePlan
                )
                LifeSheetTab.INTELLIGENCE -> IntelligenceSection(
                    score = score,
                    goalHabit = goalHabit,
                    isArabic = isArabic,
                    onApplyHabitRestructuring = onApplyHabitRestructuring
                )
                LifeSheetTab.INSIGHTS -> InsightsSection(
                    personalization = personalization,
                    isArabic = isArabic
                )
            }
        }
    }
}

@Composable
private fun TacticalSection(
    rec: LifeRecommendation?,
    isArabic: Boolean,
    onStartTask: (TaskEntity) -> Unit,
    onCompleteTask: (TaskEntity) -> Unit,
    onSnoozeTask: (Int) -> Unit,
    onSetTimeConstraint: (Int?) -> Unit
) {
    var selectedFilter by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Time Constraint Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isArabic) "كم من الوقت المتاح معك الآن؟" else "How much time do you have?",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filters = listOf(null to if (isArabic) "الكل" else "All", 15 to "15m", 30 to "30m", 45 to "45m", 60 to "60m")
                    items(filters) { (mins, label) ->
                        FilterChip(
                            selected = selectedFilter == mins,
                            onClick = {
                                selectedFilter = mins
                                onSetTimeConstraint(mins)
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }

        // Primary Recommendation
        if (rec != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SapphirePrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isArabic) "المقترح الأعلى أثراً" else "Optimal Next Action",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SapphirePrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (rec.estimatedDurationMinutes > 0) {
                                Text(
                                    text = "${rec.estimatedDurationMinutes} min",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = rec.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = if (isArabic) rec.reasonArabic else rec.reasonEnglish,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )

                        val impact = if (isArabic) rec.expectedImpactArabic else rec.expectedImpactEnglish
                        if (impact.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = WarmAmberGold.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Filled.Stars, contentDescription = null, tint = WarmAmberGold, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = impact,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = WarmAmberGold
                                    )
                                }
                            }
                        }

                        // Buttons
                        if (rec.task != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onStartTask(rec.task) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isArabic) "بدء الجلسة" else "Start Focus")
                                }

                                FilledTonalButton(
                                    onClick = { onCompleteTask(rec.task) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Alternatives List
            if (rec.alternatives.isNotEmpty()) {
                item {
                    Text(
                        text = if (isArabic) "خيارات بديلة يمكنك البدء بها" else "Alternative Next Actions",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                items(rec.alternatives) { altTask ->
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStartTask(altTask) },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = altTask.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${altTask.durationMinutes}m • ${altTask.priority.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onStartTask(altTask) }) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Start", tint = SapphirePrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineSection(
    plan: AdaptiveDayPlan?,
    isArabic: Boolean,
    onRecalculateDay: () -> Unit,
    onUndoRecalculate: () -> Unit
) {
    if (plan == null) return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Controls Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (plan.isOverloaded) (if (isArabic) "الجدول مزدحم" else "Schedule Tight")
                            else (if (isArabic) "الجدول متوازن" else "Schedule Balanced"),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (plan.isOverloaded) Color(0xFFEF6C00) else EmeraldGreen
                        )
                        Text(
                            text = if (isArabic) plan.explanationArabic else plan.explanationEnglish,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (plan.canUndo) {
                            IconButton(onClick = onUndoRecalculate) {
                                Icon(Icons.Filled.Undo, contentDescription = "Undo", tint = SapphirePrimary)
                            }
                        }
                        IconButton(onClick = onRecalculateDay) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Recalculate", tint = SapphirePrimary)
                        }
                    }
                }
            }
        }

        // Timeline Items
        items(plan.plannedItems) { item ->
            val isCal = item.itemType == PlannedItemType.CALENDAR_EVENT
            val isHabit = item.itemType == PlannedItemType.HABIT
            val hour = item.startMinuteOfDay / 60
            val min = item.startMinuteOfDay % 60
            val timeString = String.format("%02d:%02d", hour, min)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Time Pillar
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(42.dp)
                )

                // Block Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isCal -> SapphirePrimary.copy(alpha = 0.15f)
                        isHabit -> WarmAmberGold.copy(alpha = 0.15f)
                        item.tier == RescueTier.MUST_DO -> Color(0xFFFFEBEE)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${item.durationMinutes} min • " + (if (isArabic) item.reasonArabic else item.reasonEnglish),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (item.tier == RescueTier.MUST_DO && !isCal) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFD32F2F)
                            ) {
                                Text(
                                    text = if (isArabic) "إلزامي" else "MUST",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RescueSection(
    rescue: RescuePlan?,
    isRescueActive: Boolean,
    isArabic: Boolean,
    onToggleRescueMode: (Boolean) -> Unit,
    onApplyRescuePlan: () -> Unit
) {
    if (rescue == null) return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Toggle Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isRescueActive) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "وضع إنقاذ اليوم" else "Day Rescue Mode",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isRescueActive) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isArabic) rescue.summaryArabic else rescue.summaryEnglish,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isRescueActive,
                            onCheckedChange = { onToggleRescueMode(it) }
                        )
                    }

                    // Stat Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        RescueStatPill(
                            label = if (isArabic) "إلزامية" else "Must Do",
                            value = "${rescue.mustDoTasks.size}",
                            color = Color(0xFFD32F2F)
                        )
                        RescueStatPill(
                            label = if (isArabic) "مهمة" else "Important",
                            value = "${rescue.importantTasks.size}",
                            color = WarmAmberGold
                        )
                        RescueStatPill(
                            label = if (isArabic) "تأجيل آمن" else "Optional",
                            value = "${rescue.optionalTasks.size}",
                            color = EmeraldGreen
                        )
                    }

                    if (rescue.optionalTasks.isNotEmpty()) {
                        Button(
                            onClick = onApplyRescuePlan,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRescueActive) Color(0xFFD32F2F) else SapphirePrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isArabic) "تطبيق خطة الإنقاذ (تأجيل ${rescue.optionalTasks.size} مهام للغد)"
                                else "Apply Rescue Plan (Defer ${rescue.optionalTasks.size} to tomorrow)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Must Do List
        if (rescue.mustDoTasks.isNotEmpty()) {
            item {
                Text(
                    text = if (isArabic) "🔴 مهام إلزامية (حماية الأهداف والالتزامات)" else "🔴 Must Do (Protects Core Commitments)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFD32F2F)
                )
            }
            items(rescue.mustDoTasks) { item ->
                RescueTaskCard(item = item, isArabic = isArabic)
            }
        }

        // Important List
        if (rescue.importantTasks.isNotEmpty()) {
            item {
                Text(
                    text = if (isArabic) "🟠 مهام مهمة (تنفذ في حال توفر وقت إضافي)" else "🟠 Important (Execute If Time Permits)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = WarmAmberGold
                )
            }
            items(rescue.importantTasks) { item ->
                RescueTaskCard(item = item, isArabic = isArabic)
            }
        }

        // Optional List
        if (rescue.optionalTasks.isNotEmpty()) {
            item {
                Text(
                    text = if (isArabic) "🟢 مهام اختيارية (آمنة للنقل للغد بدون أي ضرر)" else "🟢 Optional (Safe to Postpone Without Risk)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldGreen
                )
            }
            items(rescue.optionalTasks) { item ->
                RescueTaskCard(item = item, isArabic = isArabic)
            }
        }
    }
}

@Composable
private fun RescueTaskCard(item: RescueTaskItem, isArabic: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${item.task.durationMinutes}m",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (isArabic) item.selectionReasonArabic else item.selectionReasonEnglish,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RescueStatPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun IntelligenceSection(
    score: DailyScoreResult?,
    goalHabit: GoalHabitIntelligenceReport?,
    isArabic: Boolean,
    onApplyHabitRestructuring: (Long, Int?, Int?) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Daily Score Gauge
        if (score != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isArabic) "مؤشر الإنتاجية اليومي" else "Daily Intelligence Score",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "${score.totalScore}",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                            color = SapphirePrimary
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SapphirePrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = score.tier.name,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = SapphirePrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = if (isArabic) score.feedbackArabic else score.feedbackEnglish,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        // 4 Point Breakdowns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ScoreFactorColumn(label = if (isArabic) "المهام" else "Tasks", pts = score.taskCompletionPoints, max = 35)
                            ScoreFactorColumn(label = if (isArabic) "العادات" else "Habits", pts = score.habitConsistencyPoints, max = 25)
                            ScoreFactorColumn(label = if (isArabic) "التركيز" else "Focus", pts = score.focusMinutesPoints, max = 25)
                            ScoreFactorColumn(label = if (isArabic) "الانضباط" else "Discipline", pts = score.scheduleDisciplinePoints, max = 15)
                        }
                    }
                }
            }
        }

        // At-risk Goals
        if (goalHabit?.atRiskGoals?.isNotEmpty() == true) {
            item {
                Text(
                    text = if (isArabic) "أهداف في دائرة الخطر" else "At-Risk Goals",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFD32F2F)
                )
            }
            items(goalHabit.atRiskGoals) { risk ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFEBEE),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = risk.goal.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFD32F2F)
                        )
                        Text(
                            text = if (isArabic) risk.riskReasonArabic else risk.riskReasonEnglish,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isArabic) risk.suggestedActionArabic else risk.suggestedActionEnglish,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }

        // Failing Habits with Intelligent Restructuring
        if (goalHabit?.failingHabits?.isNotEmpty() == true) {
            item {
                Text(
                    text = if (isArabic) "إعادة هيكلة العادات المتراجعة" else "Habit Recovery & Restructuring",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            items(goalHabit.failingHabits) { failing ->
                val rest = failing.restructuringSuggestion
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = failing.habit.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = WarmAmberGold.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isArabic) "${failing.consecutiveMissedDays} أيام انقطاع" else "${failing.consecutiveMissedDays} days missed",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = WarmAmberGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isArabic) rest.titleArabic else rest.titleEnglish,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = SapphirePrimary
                        )

                        Text(
                            text = if (isArabic) rest.descriptionArabic else rest.descriptionEnglish,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                onApplyHabitRestructuring(
                                    rest.habitId,
                                    rest.suggestedNewDurationMinutes,
                                    rest.suggestedNewReminderMinutes
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isArabic) "تطبيق الهيكلة المقترحة" else "Apply Restructuring")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreFactorColumn(label: String, pts: Int, max: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$pts/$max", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InsightsSection(
    personalization: PersonalizationInsights?,
    isArabic: Boolean
) {
    if (personalization == null) return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isArabic) "أنماط إنتاجيتك الحقيقية" else "Empirical Productivity Patterns",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(personalization.smartInsights) { insight ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SapphirePrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (insight.category) {
                                InsightCategory.VELOCITY -> Icons.Filled.Speed
                                InsightCategory.PATTERN -> Icons.Filled.Timeline
                                InsightCategory.HABIT -> Icons.Filled.Loop
                                InsightCategory.WARNING -> Icons.Filled.Warning
                            },
                            contentDescription = null,
                            tint = SapphirePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isArabic) insight.titleArabic else insight.titleEnglish,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isArabic) insight.detailArabic else insight.detailEnglish,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
