package com.example.ui.screens

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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.*
import com.example.domain.audio.SoundHapticManager
import com.example.ui.components.*
import com.example.ui.components.rpg.HomeRpgBanner
import com.example.ui.theme.*
import com.example.ui.viewmodel.YawmekUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    uiState: YawmekUiState,
    onOpenQuickAdd: (QuickAddTab) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onRescheduleTask: (TaskEntity) -> Unit,
    onToggleHabit: (Long) -> Unit,
    onOpenAi: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenMorningBrief: () -> Unit,
    onOpenEveningSummary: () -> Unit,
    onOpenFocus: (TaskEntity?) -> Unit = {},
    onOpenAccountSync: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onRequestCalendar: () -> Unit = {},
    onNavigateToTab: (String) -> Unit
) {
    val language = uiState.userSettings.language
    val isArabic = language == AppLanguage.ARABIC
    val userName = uiState.userSettings.userName.ifBlank { if (isArabic) "صديقي" else "there" }

    // Formatted current date
    val today = LocalDate.now()
    val formattedDate = if (isArabic) {
        val formatter = DateTimeFormatter.ofPattern("EEEE، d MMMM", Locale("ar"))
        today.format(formatter)
    } else {
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)
        today.format(formatter)
    }

    val hasAnyData = uiState.tasks.isNotEmpty() || uiState.expenses.isNotEmpty() ||
            uiState.habits.isNotEmpty() || uiState.goals.isNotEmpty() || uiState.notes.isNotEmpty()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. Header Bar: Greeting, Date, Cloud Sync, Notifications, Search, AI
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "أهلاً بك، $userName 👋" else "Good morning, $userName 👋",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cloud Sync Badge
                    IconButton(
                        onClick = onOpenAccountSync,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .testTag("home_sync_btn")
                    ) {
                        val (icon, tint) = when {
                            uiState.activeAccount?.isLoggedIn == true -> Icons.Filled.CloudDone to Color(0xFF10B981)
                            else -> Icons.Filled.CloudQueue to MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Icon(icon, contentDescription = "Sync", tint = tint, modifier = Modifier.size(18.dp))
                    }

                    // Notification Bell with Badge
                    BadgedBox(
                        badge = {
                            if (uiState.inAppNotifications.isNotEmpty()) {
                                Badge(containerColor = Color(0xFFF59E0B)) {
                                    Text("${uiState.inAppNotifications.size}")
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .testTag("home_notif_btn")
                        ) {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Search Button
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .testTag("home_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // AI Assistant Button
                    IconButton(
                        onClick = onOpenAi,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BrandAmber.copy(alpha = 0.15f))
                            .testTag("home_ai_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "AI",
                            tint = BrandAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. Daily Briefing / Review Quick Banner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onOpenMorningBrief,
                    shape = RoundedCornerShape(14.dp),
                    color = BrandBlue.copy(alpha = 0.08f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(BrandBlue.copy(alpha = 0.3f), BrandBlue.copy(alpha = 0.1f)))
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.WbSunny, contentDescription = null, tint = BrandAmber, modifier = Modifier.size(18.dp))
                        Text(
                            text = if (isArabic) "موجز الصباح" else "Morning Brief",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    onClick = onOpenEveningSummary,
                    shape = RoundedCornerShape(14.dp),
                    color = ColorHabit.copy(alpha = 0.08f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(ColorHabit.copy(alpha = 0.3f), ColorHabit.copy(alpha = 0.1f)))
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.NightsStay, contentDescription = null, tint = ColorHabit, modifier = Modifier.size(18.dp))
                        Text(
                            text = if (isArabic) "مراجعة المساء" else "Evening Review",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // RPG Character Progression Hero Card
        item {
            HomeRpgBanner(
                character = uiState.rpgCharacter,
                isArabic = isArabic,
                onOpenAdventure = { onNavigateToTab("adventure") }
            )
        }

        // Calendar Connect / Upcoming Calendar Event Banner
        item {
            if (uiState.isCalendarPermissionGranted && uiState.calendarEvents.isNotEmpty()) {
                val nextEvent = uiState.calendarEvents.firstOrNull { it.startMillis >= System.currentTimeMillis() }
                    ?: uiState.calendarEvents.first()
                val timeStr = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date(nextEvent.startMillis))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "الموعد القادم بالتقويم" else "Next Calendar Event",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "${nextEvent.title} ($timeStr)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFFF8FAFC)
                                )
                            }
                        }
                        TextButton(onClick = { onNavigateToTab("plan") }) {
                            Text(if (isArabic) "عرض الجدول" else "Timeline", color = Color(0xFF38BDF8))
                        }
                    }
                }
            } else if (!uiState.isCalendarPermissionGranted) {
                Surface(
                    onClick = onRequestCalendar,
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.8f),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                            Text(
                                text = if (isArabic) "ربط تقويم الجهاز لحساب أوقات الفراغ" else "Connect Calendar to find free gaps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (isArabic) "ربط" else "Connect",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }

        // Deep Focus Quick Card
        item {
            Surface(
                onClick = { onOpenFocus(null) },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F1B2C),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFFF59E0B).copy(alpha = 0.4f), Color(0xFF1E293B)))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_focus_mode_card")
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Timer, contentDescription = "Focus", tint = Color(0xFFF59E0B), modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text(
                                text = if (isArabic) "جلسة تركيز عميق ⏱" else "Deep Focus Mode ⏱",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFF8FAFC)
                            )
                            val focusText = if (uiState.todayFocusMinutes > 0) {
                                if (isArabic) "ركزت اليوم: ${uiState.todayFocusMinutes} دقيقة 🔥" else "${uiState.todayFocusMinutes}m focused today 🔥"
                            } else {
                                if (isArabic) "مؤقت بومودورو وأصوات هادئة" else "Pomodoro timer & ambient sounds"
                            }
                            Text(
                                text = focusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF59E0B)
                    ) {
                        Text(
                            text = if (isArabic) "ابدأ (25د)" else "Start (25m)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F1B2C),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Train Your Brain — 5-minute intelligent break card
        item {
            Surface(
                onClick = { onNavigateToTab("games") },
                shape = RoundedCornerShape(16.dp),
                color = BrandAmber.copy(alpha = 0.08f),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(BrandAmber.copy(alpha = 0.4f), BrandAmber.copy(alpha = 0.15f))
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_brain_games_card")
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandAmber.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🧠", fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                text = if (isArabic) "شغّل دماغك 🧠" else "Train Your Brain 🧠",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isArabic) "خُض تحدياً ذكياً لمدة 5 دقائق" else "Take a 5-minute challenge",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BrandAmber
                    ) {
                        Text(
                            text = if (isArabic) "العب الآن" else "Play Now",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // YAWMEK Community Arena Quick Card
        item {
            Surface(
                onClick = { onNavigateToTab("community") },
                shape = RoundedCornerShape(16.dp),
                color = SapphirePrimary.copy(alpha = 0.08f),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(SapphirePrimary.copy(alpha = 0.4f), ColorRpg.copy(alpha = 0.3f))
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_community_card")
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SapphirePrimary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⚔️", fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                text = if (isArabic) "مجتمع يومك ومبارزات الإنتاجية ⚔️" else "YAWMEK Community & Battles ⚔️",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val friendCount = uiState.communityFriends.size
                            Text(
                                text = if (isArabic) "$friendCount أصدقاء • تحديات RPG وتركيز" else "$friendCount friends • RPG & Focus battles",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SapphirePrimary
                    ) {
                        Text(
                            text = if (isArabic) "ادخل الحلبة" else "Enter",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 3. Signature Hero Card: WHAT SHOULD I DO NOW?
        item {
            WhatShouldIDoNowHeroCard(
                recommendation = uiState.recommendation,
                language = language,
                onStartAction = { task -> onOpenFocus(task) },
                onCompleteAction = { task -> onToggleTask(task) },
                onRescheduleAction = { task -> onRescheduleTask(task) },
                onChooseAnother = { onOpenQuickAdd(QuickAddTab.TASK) }
            )
        }

        // 4. If Totally Empty Workspace -> Beautiful Welcoming Empty State Card
        if (!hasAnyData) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.AddCircleOutline,
                    iconTint = BrandBlue,
                    title = if (isArabic) "لنبدأ ببناء يومك ☀️" else "Let's build your day ☀️",
                    subtitle = if (isArabic)
                        "مساحتك نظيفة ومستعدة. أضف أول مهمة، مصروف، عادة أو هدف لتبدأ رحلتك المنظمة."
                    else
                        "Your workspace is clean and ready. Add your first task, expense, habit, or goal to personalize your day.",
                    actionButtonText = if (isArabic) "+ أضف أول عنصر" else "+ Add Something",
                    onActionClick = { onOpenQuickAdd(QuickAddTab.TASK) },
                    secondaryContent = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isArabic) "أو اختر نوع العنصر:" else "Or choose what to create:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    QuickAddTab.TASK to (if (isArabic) "+ مهمة" else "+ Task"),
                                    QuickAddTab.EXPENSE to (if (isArabic) "+ مصروف" else "+ Expense"),
                                    QuickAddTab.HABIT to (if (isArabic) "+ عادة" else "+ Habit"),
                                    QuickAddTab.GOAL to (if (isArabic) "+ هدف" else "+ Goal"),
                                    QuickAddTab.NOTE to (if (isArabic) "+ ملاحظة" else "+ Note")
                                ).forEach { (tab, label) ->
                                    SuggestionChip(
                                        onClick = { onOpenQuickAdd(tab) },
                                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        // 5. Dynamic Module: Tasks Section (Adaptive: Only shown if tasks exist, or as guided prompt)
        if (uiState.tasks.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isArabic) "مهام اليوم" else "Today's Tasks",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { onNavigateToTab("plan") }) {
                        Text(if (isArabic) "الجدول الكامل" else "Full Plan", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            items(uiState.pendingTasks.take(4)) { task ->
                TaskCardRow(
                    task = task,
                    language = language,
                    onToggle = { onToggleTask(task) }
                )
            }
        }

        // 6. Dynamic Module: Habits Check-in Row (Adaptive)
        if (uiState.habits.isNotEmpty()) {
            item {
                val todayEpoch = LocalDate.now().toEpochDay()
                val completedHabitIdsToday = uiState.habitLogs.filter { it.dateEpochDay == todayEpoch }.map { it.habitId }.toSet()

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "العادات اليومية" else "Daily Habits",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = { onNavigateToTab("habits") }) {
                            Text(if (isArabic) "متابعة العادات" else "View All", color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.habits) { habit ->
                            val isDone = completedHabitIdsToday.contains(habit.id)
                            HabitQuickChip(
                                habit = habit,
                                isCompletedToday = isDone,
                                onToggle = { onToggleHabit(habit.id) }
                            )
                        }
                    }
                }
            }
        }

        // 7. Dynamic Module: Money & Budget Snapshot (Adaptive)
        if (uiState.expenses.isNotEmpty() || uiState.budgets.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = ColorFinance)
                                Text(
                                    text = if (isArabic) "مصاريف اليوم" else "Today's Spending",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = "${uiState.todaySpending} ${uiState.userSettings.currency}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        if (uiState.monthlyBudget > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val progress = (uiState.monthlySpending / uiState.monthlyBudget).coerceIn(0.0, 1.0).toFloat()
                            val isNearLimit = progress >= 0.8f
                            YawmekProgressBar(
                                progress = progress,
                                fillBrush = Brush.horizontalGradient(
                                    if (isNearLimit) listOf(SemanticWarning, SemanticError)
                                    else listOf(ColorFinance, SapphireAccent)
                                ),
                                height = 8.dp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${uiState.monthlySpending} / ${uiState.monthlyBudget} ${uiState.userSettings.currency}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isNearLimit) SemanticWarning else ColorFinance
                                )
                            }
                        }
                    }
                }
            }
        }

        // 8. Dynamic Module: Goals Snapshot (Adaptive)
        if (uiState.goals.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isArabic) "الأهداف الحالية" else "Active Goals",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { onNavigateToTab("goals") }) {
                        Text(if (isArabic) "جميع الأهداف" else "View All", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            items(uiState.goals.take(2)) { goal ->
                val goalMilestones = uiState.milestones.filter { it.goalId == goal.id }
                val completedCount = goalMilestones.count { it.isCompleted }
                val totalCount = goalMilestones.size
                val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "$completedCount/$totalCount",
                                style = MaterialTheme.typography.labelMedium,
                                color = ColorGoal
                            )
                        }
                        if (totalCount > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            YawmekProgressBar(
                                progress = progress,
                                fillBrush = Brush.horizontalGradient(listOf(ColorGoal, BrandAmberLight)),
                                height = 7.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCardRow(
    task: TaskEntity,
    language: AppLanguage,
    onToggle: () -> Unit
) {
    val isArabic = language == AppLanguage.ARABIC

    val priorityColor = when (task.priority) {
        Priority.HIGH -> SemanticError
        Priority.MEDIUM -> BrandAmber
        Priority.LOW -> BrandBlue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            YawmekTaskCheckbox(
                isChecked = task.isCompleted,
                onCheckedChange = onToggle
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                if (task.dueTimeMinutes != null || task.durationMinutes > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏱ ${task.durationMinutes}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                        Text(
                            text = task.category.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(priorityColor)
            )
        }
    }
}

@Composable
fun HabitQuickChip(
    habit: HabitEntity,
    isCompletedToday: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { SoundHapticManager.getInstance(context) }

    Surface(
        onClick = {
            if (!isCompletedToday) {
                soundManager.playHabitCompleted()
            } else {
                soundManager.playTap()
            }
            onToggle()
        },
        shape = RoundedCornerShape(16.dp),
        color = if (isCompletedToday) SemanticSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (isCompletedToday) Brush.horizontalGradient(listOf(SemanticSuccess, SemanticSuccess)) else CardDefaults.outlinedCardBorder().brush
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (isCompletedToday) SemanticSuccess else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (isCompletedToday) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            Text(
                text = habit.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
