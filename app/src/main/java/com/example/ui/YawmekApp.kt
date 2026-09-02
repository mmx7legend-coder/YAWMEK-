package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.model.AppLanguage
import com.example.data.local.model.Priority
import com.example.data.local.model.TaskCategory
import com.example.data.local.model.TaskEntity
import com.example.ui.components.*
import com.example.ui.games.GamesHomeScreen
import com.example.ui.navigation.AppDestination
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.YawmekViewModel
import kotlinx.coroutines.delay

@Composable
fun YawmekApp(
    viewModel: YawmekViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showSplash by remember { mutableStateOf(true) }
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    var quickAddSheetTab by remember { mutableStateOf<QuickAddTab?>(null) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showFocusSheet by remember { mutableStateOf(false) }
    var activeFocusTask by remember { mutableStateOf<TaskEntity?>(null) }
    var showCalendarPermissionDialog by remember { mutableStateOf(false) }

    val language = uiState.userSettings.language
    val isArabic = language == AppLanguage.ARABIC

    YawmekTheme(themeMode = uiState.userSettings.themeMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                showSplash -> {
                    SplashScreen(onSplashFinished = { showSplash = false })
                }
                !uiState.userSettings.hasCompletedOnboarding -> {
                    OnboardingScreen(
                        language = language,
                        onComplete = { name, priorities ->
                            viewModel.completeOnboarding(name, priorities)
                        }
                    )
                }
                else -> {
                    Scaffold(
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp,
                                modifier = Modifier.testTag("yawmek_bottom_bar")
                            ) {
                                listOf(
                                    AppDestination.HOME,
                                    AppDestination.PLAN,
                                    AppDestination.MONEY,
                                    AppDestination.HABITS,
                                    AppDestination.GAMES,
                                    AppDestination.AI,
                                    AppDestination.PROFILE
                                ).forEach { dest ->
                                    val isSelected = currentDestination == dest
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentDestination = dest },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = if (isArabic) dest.titleAr else dest.titleEn
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = if (isArabic) dest.titleAr else dest.titleEn,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                }
                            }
                        },
                        floatingActionButton = {
                            if (currentDestination == AppDestination.HOME) {
                                FloatingActionButton(
                                    onClick = { quickAddSheetTab = QuickAddTab.TASK },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    shape = CircleShape,
                                    modifier = Modifier.testTag("global_fab_add")
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Quick Add")
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentDestination) {
                                AppDestination.HOME -> {
                                    HomeScreen(
                                        uiState = uiState,
                                        onOpenQuickAdd = { tab -> quickAddSheetTab = tab },
                                        onToggleTask = { task -> viewModel.toggleTask(task) },
                                        onDeleteTask = { task -> viewModel.deleteTask(task) },
                                        onRescheduleTask = { task ->
                                            viewModel.rescheduleTask(task, System.currentTimeMillis() + 86400000, null)
                                        },
                                        onToggleHabit = { habitId -> viewModel.toggleHabit(habitId) },
                                        onOpenAi = { currentDestination = AppDestination.AI },
                                        onOpenSearch = { showSearchDialog = true },
                                        onOpenMorningBrief = { viewModel.toggleMorningBriefing(true) },
                                        onOpenEveningSummary = { viewModel.toggleEveningSummary(true) },
                                        onOpenFocus = { task ->
                                            activeFocusTask = task
                                            showFocusSheet = true
                                        },
                                        onOpenAccountSync = { showAccountDialog = true },
                                        onOpenNotifications = { showNotificationsSheet = true },
                                        onRequestCalendar = { showCalendarPermissionDialog = true },
                                        onNavigateToTab = { route ->
                                            when (route) {
                                                "plan" -> currentDestination = AppDestination.PLAN
                                                "money" -> currentDestination = AppDestination.MONEY
                                                "habits" -> currentDestination = AppDestination.HABITS
                                                "goals" -> currentDestination = AppDestination.GOALS
                                                "notes" -> currentDestination = AppDestination.NOTES
                                                "games" -> currentDestination = AppDestination.GAMES
                                                "ai" -> currentDestination = AppDestination.AI
                                            }
                                        }
                                    )
                                }
                                AppDestination.PLAN -> {
                                    PlanScreen(
                                        uiState = uiState,
                                        onOpenQuickAdd = { tab -> quickAddSheetTab = tab },
                                        onToggleTask = { task -> viewModel.toggleTask(task) },
                                        onDeleteTask = { task -> viewModel.deleteTask(task) },
                                        onRescheduleTask = { task ->
                                            viewModel.rescheduleTask(task, System.currentTimeMillis() + 86400000, null)
                                        },
                                        onOpenFocus = { task ->
                                            activeFocusTask = task
                                            showFocusSheet = true
                                        },
                                        onExportToCalendar = { task -> viewModel.exportTaskToCalendar(task) }
                                    )
                                }
                                AppDestination.MONEY -> {
                                    MoneyScreen(
                                        uiState = uiState,
                                        onOpenQuickAdd = { tab -> quickAddSheetTab = tab },
                                        onSetBudget = { period, amount -> viewModel.setBudget(period, amount) },
                                        onDeleteExpense = { exp -> viewModel.deleteExpense(exp) }
                                    )
                                }
                                AppDestination.HABITS -> {
                                    HabitsScreen(
                                        uiState = uiState,
                                        onOpenQuickAdd = { tab -> quickAddSheetTab = tab },
                                        onToggleHabit = { habitId, dateEpoch -> viewModel.toggleHabit(habitId, dateEpoch) },
                                        onDeleteHabit = { habit -> viewModel.deleteHabit(habit) }
                                    )
                                }
                                AppDestination.GOALS -> {
                                    GoalsScreen(
                                        uiState = uiState,
                                        onOpenQuickAdd = { tab -> quickAddSheetTab = tab },
                                        onToggleMilestone = { m -> viewModel.toggleMilestone(m) },
                                        onDeleteGoal = { goal -> viewModel.deleteGoal(goal) }
                                    )
                                }
                                AppDestination.NOTES -> {
                                    NotesScreen(
                                        uiState = uiState,
                                        onOpenQuickAdd = { tab -> quickAddSheetTab = tab },
                                        onTogglePin = { note -> viewModel.toggleNotePin(note) },
                                        onDeleteNote = { note -> viewModel.deleteNote(note) }
                                    )
                                }
                                AppDestination.GAMES -> {
                                    GamesHomeScreen(
                                        isArabic = isArabic,
                                        onBackToMyDay = { currentDestination = AppDestination.HOME }
                                    )
                                }
                                AppDestination.AI -> {
                                    AiScreen(
                                        uiState = uiState,
                                        onSendPrompt = { prompt -> viewModel.sendAiPrompt(prompt) },
                                        onApplyPlan = { plan -> viewModel.applyAiPlan(plan) }
                                    )
                                }
                                AppDestination.PROFILE -> {
                                    ProfileScreen(
                                        uiState = uiState,
                                        onLanguageChange = { lang -> viewModel.setLanguage(lang) },
                                        onThemeChange = { mode -> viewModel.setThemeMode(mode) },
                                        onCurrencyChange = { curr -> viewModel.setCurrency(curr) },
                                        onResetAllData = { viewModel.resetAllData() },
                                        onOpenAccountSync = { showAccountDialog = true },
                                        onUpdateNotificationPreferences = { q, t, b, h ->
                                            viewModel.updateNotificationPreferences(q, t, b, h)
                                        }
                                    )
                                }
                            }

                            // Celebration Toast Floating Banner
                            uiState.showCelebrationToast?.let { toastMsg ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                                        .align(Alignment.TopCenter)
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = SemanticSuccess),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White)
                                            Text(
                                                text = toastMsg,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                LaunchedEffect(toastMsg) {
                                    delay(2500)
                                    viewModel.clearCelebration()
                                }
                            }
                        }
                    }

                    // Quick Add Modal Bottom Sheet
                    quickAddSheetTab?.let { tab ->
                        QuickAddBottomSheet(
                            initialTab = tab,
                            language = language,
                            currency = uiState.userSettings.currency,
                            onDismiss = { quickAddSheetTab = null },
                            onNaturalLanguageTaskSubmit = { text -> viewModel.addTaskFromNaturalLanguage(text) },
                            onCreateTask = { title, desc, due, dueTime, dur, pri, cat, subs ->
                                viewModel.createTask(
                                    title = title,
                                    description = desc,
                                    dueDateMillis = due,
                                    dueTimeMinutes = dueTime,
                                    durationMinutes = dur,
                                    priority = pri,
                                    category = cat,
                                    subtasks = subs
                                )
                            },
                            onCreateExpense = { amount, cat, note ->
                                viewModel.addExpense(amount, cat, note)
                            },
                            onCreateHabit = { title, cat, freq, color ->
                                viewModel.createHabit(title, cat, freq, colorHex = color)
                            },
                            onCreateGoal = { title, desc, cat, miles ->
                                viewModel.createGoal(title, desc, cat, milestoneTitles = miles)
                            },
                            onCreateNote = { title, content, tag, pinned ->
                                viewModel.createNote(title, content, tag, isPinned = pinned)
                            }
                        )
                    }

                    // Global Search Dialog
                    if (showSearchDialog) {
                        GlobalSearchDialog(
                            query = uiState.searchQuery,
                            results = uiState.searchResults,
                            language = language,
                            onQueryChange = { q -> viewModel.setSearchQuery(q) },
                            onDismiss = {
                                showSearchDialog = false
                                viewModel.setSearchQuery("")
                            }
                        )
                    }

                    // Morning Briefing Dialog
                    if (uiState.showMorningBriefing) {
                        MorningBriefingDialog(
                            userName = uiState.userSettings.userName.ifBlank { if (isArabic) "صديقي" else "there" },
                            language = language,
                            todayTasksCount = uiState.pendingTasks.size,
                            highPriorityCount = uiState.pendingTasks.count { it.priority == Priority.HIGH },
                            remainingBudget = uiState.monthlyBudget - uiState.monthlySpending,
                            currency = uiState.userSettings.currency,
                            onDismiss = { viewModel.toggleMorningBriefing(false) }
                        )
                    }

                    // Evening Summary Dialog
                    if (uiState.showEveningSummary) {
                        val completedToday = uiState.tasks.filter { it.isCompleted }
                        EveningSummaryDialog(
                            userName = uiState.userSettings.userName.ifBlank { if (isArabic) "صديقي" else "there" },
                            language = language,
                            completedTasks = completedToday,
                            unfinishedTasks = uiState.pendingTasks,
                            todaySpent = uiState.todaySpending,
                            currency = uiState.userSettings.currency,
                            onMoveUnfinishedToTomorrow = {
                                val tomorrow = System.currentTimeMillis() + 86400000
                                uiState.pendingTasks.forEach { task ->
                                    viewModel.rescheduleTask(task, tomorrow, null)
                                }
                            },
                            onDismiss = { viewModel.toggleEveningSummary(false) }
                        )
                    }

                    // Account & Cloud Sync Dialog
                    if (showAccountDialog) {
                        AccountSyncDialog(
                            account = uiState.activeAccount,
                            syncState = uiState.syncState,
                            isArabic = isArabic,
                            onDismiss = { showAccountDialog = false },
                            onSignIn = { email, pass -> viewModel.signIn(email, pass) },
                            onSignUp = { email, name, pass -> viewModel.signUp(email, name, pass) },
                            onSignOut = { viewModel.signOut() },
                            onSyncNow = { viewModel.syncNow() },
                            onResetPassword = { email -> viewModel.requestPasswordReset(email) {} },
                            onDeleteAccount = { viewModel.deleteAccount() }
                        )
                    }

                    // In-App Notification Center Sheet
                    if (showNotificationsSheet) {
                        NotificationCenterSheet(
                            notifications = uiState.inAppNotifications,
                            isArabic = isArabic,
                            onClearAll = { viewModel.clearNotificationHistory() },
                            onDismiss = { showNotificationsSheet = false }
                        )
                    }

                    // Deep Focus Session Bottom Sheet
                    if (showFocusSheet) {
                        FocusSessionSheet(
                            initialTask = activeFocusTask,
                            pendingTasks = uiState.pendingTasks,
                            focusSessions = uiState.focusSessions,
                            todayFocusMinutes = uiState.todayFocusMinutes,
                            isArabic = isArabic,
                            onDismiss = { showFocusSheet = false },
                            onSessionCompleted = { session, markTaskComplete ->
                                viewModel.saveFocusSession(session, markTaskComplete)
                                showFocusSheet = false
                            },
                            onQuickCreateTask = { title ->
                                viewModel.createTask(
                                    title = title,
                                    category = TaskCategory.WORK,
                                    priority = Priority.HIGH,
                                    durationMinutes = 25
                                )
                            }
                        )
                    }

                    // Calendar Permission Rationale Dialog
                    if (showCalendarPermissionDialog) {
                        CalendarPermissionDialog(
                            isArabic = isArabic,
                            onDismiss = { showCalendarPermissionDialog = false },
                            onRequestPermission = {
                                viewModel.setCalendarPermissionGranted(true)
                                viewModel.refreshCalendarEvents()
                                showCalendarPermissionDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}
