package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.AppLanguage
import com.example.data.local.model.TaskEntity
import com.example.domain.ScheduleCalculator
import com.example.domain.TimelineItem
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QuickAddTab
import com.example.ui.components.LifeEngineHeroCard
import com.example.ui.components.LifeEngineSheet
import com.example.ui.theme.*
import com.example.ui.viewmodel.YawmekUiState
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PlanScreen(
    uiState: YawmekUiState,
    onOpenQuickAdd: (QuickAddTab) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onRescheduleTask: (TaskEntity) -> Unit,
    onOpenFocus: (TaskEntity) -> Unit = {},
    onExportToCalendar: (TaskEntity) -> Unit = {},
    onSnoozeTask: (Int) -> Unit = {},
    onRecalculateDay: () -> Unit = {},
    onUndoRecalculate: () -> Unit = {},
    onToggleRescueMode: (Boolean?) -> Unit = {},
    onApplyRescuePlan: () -> Unit = {},
    onSetTimeConstraint: (Int?) -> Unit = {},
    onApplyHabitRestructuring: (Long, Int?, Int?) -> Unit = { _, _, _ -> }
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val language = uiState.userSettings.language
    val isArabic = language == AppLanguage.ARABIC
    var showLifeEngineSheet by remember { mutableStateOf(false) }

    val timelineItems = remember(uiState.tasks, selectedDate) {
        ScheduleCalculator.buildTimelineForDate(
            tasks = uiState.tasks,
            targetDate = selectedDate,
            dayStartHour = uiState.userSettings.workStartHour,
            dayEndHour = uiState.userSettings.workEndHour
        )
    }

    // Days for the 7-day strip
    val dateStrip = remember {
        val today = LocalDate.now()
        (0..6).map { today.plusDays(it.toLong()) }
    }

    val isSelectedDateToday = selectedDate == LocalDate.now()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("plan_screen"),
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
                        text = if (isArabic) "الجدول والوقت" else "Daily Timeline",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (uiState.isCalendarPermissionGranted) {
                        Text(
                            text = if (isArabic) "مدمج مع تقويم الجهاز ✓" else "Synced with device calendar ✓",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }

                IconButton(
                    onClick = { onOpenQuickAdd(QuickAddTab.TASK) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add Task",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Date Strip
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dateStrip) { date ->
                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()

                    val dayName = if (isArabic) {
                        val formatter = DateTimeFormatter.ofPattern("EEE", Locale("ar"))
                        date.format(formatter)
                    } else {
                        val formatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)
                        date.format(formatter)
                    }

                    Card(
                        modifier = Modifier
                            .width(62.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedDate = date },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) null else CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${date.dayOfMonth}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isToday) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else BrandAmber)
                                    )
                            }
                        }
                    }
                }
            }
        }

        // Life Engine Hero Card for Today
        if (isSelectedDateToday && uiState.lifeEngineState != null) {
            item {
                LifeEngineHeroCard(
                    lifeState = uiState.lifeEngineState,
                    language = language,
                    onStartAction = { task -> onOpenFocus(task) },
                    onCompleteAction = { task -> onToggleTask(task) },
                    onSnoozeAction = { minutes -> onSnoozeTask(minutes) },
                    onRescheduleAction = { task -> onRescheduleTask(task) },
                    onOpenLifeDashboard = { showLifeEngineSheet = true },
                    onRecalculateDay = onRecalculateDay,
                    onUndoRecalculate = onUndoRecalculate,
                    onToggleRescueMode = { onToggleRescueMode(null) }
                )
            }
        }

        // External Device Calendar Events for Today
        if (isSelectedDateToday && uiState.calendarEvents.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = if (isArabic) "مواعيد تقويم الجهاز 📅" else "Device Calendar Events 📅",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            items(uiState.calendarEvents) { calEvent ->
                val timeStr = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date(calEvent.startMillis))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B2C)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF38BDF8).copy(alpha = 0.4f), Color(0xFF1E293B)))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Event, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = calEvent.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFF8FAFC)
                            )
                            Text(
                                text = "$timeStr • ${calEvent.durationMinutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        // Timeline Items
        if (timelineItems.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.EventAvailable,
                    iconTint = BrandBlue,
                    title = if (isArabic) "لا توجد مواعيد مجدولة" else "Nothing scheduled for this day",
                    subtitle = if (isArabic) "أضف مهام بمواعيد محددة لتظهر في خطك الزمني وتتعرف على أوقات فراغك." else "Schedule tasks with specific times to see your dynamic timeline and free time windows.",
                    actionButtonText = if (isArabic) "+ جدولة مهمة" else "+ Schedule a Task",
                    onActionClick = { onOpenQuickAdd(QuickAddTab.TASK) }
                )
            }
        } else {
            items(timelineItems) { item ->
                when (item) {
                    is TimelineItem.TaskBlock -> {
                        TimelineTaskCard(
                            block = item,
                            language = language,
                            onToggle = { onToggleTask(item.task) },
                            onStartFocus = { onOpenFocus(item.task) },
                            onExportToCalendar = { onExportToCalendar(item.task) }
                        )
                    }
                    is TimelineItem.FreeTimeSlot -> {
                        TimelineFreeTimeCard(
                            slot = item,
                            language = language,
                            onFillSlot = { onOpenQuickAdd(QuickAddTab.TASK) }
                        )
                    }
                }
            }
        }

        // Unscheduled Tasks for today
        val targetStartMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val targetEndMillis = selectedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        val unscheduled = uiState.tasks.filter {
            it.dueDateMillis != null && it.dueDateMillis in targetStartMillis..targetEndMillis && it.dueTimeMinutes == null
        }

        if (unscheduled.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isArabic) "مهام غير مقيدة بوقت محدد" else "Anytime Today",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(unscheduled) { task ->
                TaskCardRow(task = task, language = language, onToggle = { onToggleTask(task) })
            }
        }
    }

    if (showLifeEngineSheet) {
        LifeEngineSheet(
            lifeState = uiState.lifeEngineState,
            language = language,
            onDismiss = { showLifeEngineSheet = false },
            onStartTask = { task ->
                showLifeEngineSheet = false
                onOpenFocus(task)
            },
            onCompleteTask = { task ->
                onToggleTask(task)
            },
            onSnoozeTask = { minutes ->
                onSnoozeTask(minutes)
            },
            onRecalculateDay = onRecalculateDay,
            onUndoRecalculate = onUndoRecalculate,
            onToggleRescueMode = { active -> onToggleRescueMode(active) },
            onApplyRescuePlan = {
                showLifeEngineSheet = false
                onApplyRescuePlan()
            },
            onSetTimeConstraint = onSetTimeConstraint,
            onApplyHabitRestructuring = onApplyHabitRestructuring
        )
    }
}

@Composable
fun TimelineTaskCard(
    block: TimelineItem.TaskBlock,
    language: AppLanguage,
    onToggle: () -> Unit,
    onStartFocus: () -> Unit = {},
    onExportToCalendar: () -> Unit = {}
) {
    val startTime = ScheduleCalculator.formatMinuteOfDay(block.startMinuteOfDay)
    val endTime = ScheduleCalculator.formatMinuteOfDay(block.endMinuteOfDay)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (block.task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(62.dp)
            ) {
                Text(
                    text = startTime,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = endTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(BrandBlue)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = block.task.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "⏱ ${block.durationMinutes} min • ${block.task.category.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Focus action
            IconButton(
                onClick = onStartFocus,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
            ) {
                Icon(Icons.Filled.Timer, contentDescription = "Focus", tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
            }

            // Export to calendar
            IconButton(
                onClick = onExportToCalendar,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = "Export to Calendar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }

            // Checkbox
            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (block.task.isCompleted) SemanticSuccess else MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (block.task.isCompleted) {
                    Icon(Icons.Filled.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun TimelineFreeTimeCard(
    slot: TimelineItem.FreeTimeSlot,
    language: AppLanguage,
    onFillSlot: () -> Unit
) {
    val isArabic = language == AppLanguage.ARABIC
    val startTime = ScheduleCalculator.formatMinuteOfDay(slot.startMinuteOfDay)
    val endTime = ScheduleCalculator.formatMinuteOfDay(slot.endMinuteOfDay)

    val hours = slot.durationMinutes / 60
    val mins = slot.durationMinutes % 60
    val durationLabel = when {
        hours > 0 && mins > 0 -> "${hours}h ${mins}m"
        hours > 0 -> "${hours}h"
        else -> "${mins}m"
    }

    Surface(
        onClick = onFillSlot,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(BrandBlue.copy(alpha = 0.2f), SemanticSuccess.copy(alpha = 0.2f)))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Spa,
                    contentDescription = null,
                    tint = SemanticSuccess,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = if (isArabic) "وقت فراغ متاح • $durationLabel" else "Free Time • $durationLabel",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = SemanticSuccess
                    )
                    Text(
                        text = "$startTime – $endTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = if (isArabic) "+ استغل الوقت" else "+ Plan slot",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
