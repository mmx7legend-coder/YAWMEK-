package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.model.*
import com.example.data.remote.AiDailyPlanItem
import com.example.data.remote.AiResponseResult
import com.example.data.remote.UserContextData
import com.example.data.remote.YawmekAiEngine
import com.example.data.repository.YawmekRepository
import com.example.domain.NaturalLanguageParser
import com.example.domain.RecommendationResult
import com.example.domain.SmartRecommendationEngine
import com.example.domain.UrgencyLevel
import com.example.domain.auth.AccountManager
import com.example.domain.auth.AuthResult
import com.example.domain.calendar.CalendarEventItem
import com.example.domain.calendar.CalendarManager
import com.example.domain.focus.FocusAudioHelper
import com.example.domain.notifications.InAppNotification
import com.example.domain.notifications.SmartNotificationManager
import com.example.domain.sync.CloudSyncEngine
import com.example.domain.sync.SyncState
import com.example.ui.widget.YawmekWidgetUpdater
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val planItems: List<AiDailyPlanItem> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class GlobalSearchResults(
    val tasks: List<TaskEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList()
)

data class YawmekUiState(
    val isLoading: Boolean = false,
    val userSettings: UserSettingsEntity = UserSettingsEntity(),
    val tasks: List<TaskEntity> = emptyList(),
    val pendingTasks: List<TaskEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val habitLogs: List<HabitLogEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val milestones: List<MilestoneEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val recommendation: RecommendationResult? = null,
    val todaySpending: Double = 0.0,
    val monthlySpending: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val chatMessages: List<ChatMessage> = emptyList(),
    val isAiThinking: Boolean = false,
    val searchQuery: String = "",
    val searchResults: GlobalSearchResults = GlobalSearchResults(),
    val showCelebrationToast: String? = null,
    val showMorningBriefing: Boolean = false,
    val showEveningSummary: Boolean = false,
    val activeAccount: UserAccountEntity? = null,
    val syncState: SyncState = SyncState.Idle,
    val notificationPreferences: NotificationPreferencesEntity = NotificationPreferencesEntity(),
    val calendarEvents: List<CalendarEventItem> = emptyList(),
    val isCalendarPermissionGranted: Boolean = false,
    val focusSessions: List<FocusSessionEntity> = emptyList(),
    val todayFocusMinutes: Int = 0,
    val inAppNotifications: List<InAppNotification> = emptyList()
)

class YawmekViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: YawmekRepository
    val accountManager: AccountManager
    val cloudSyncEngine: CloudSyncEngine
    val focusAudioHelper: FocusAudioHelper

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _isAiThinking = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _celebrationToast = MutableStateFlow<String?>(null)
    private val _showMorningBriefing = MutableStateFlow(false)
    private val _showEveningSummary = MutableStateFlow(false)

    private val _calendarEvents = MutableStateFlow<List<CalendarEventItem>>(emptyList())
    private val _isCalendarPermissionGranted = MutableStateFlow(false)
    private val _inAppNotifications = MutableStateFlow<List<InAppNotification>>(emptyList())

    init {
        val db = AppDatabase.getDatabase(application)
        repository = YawmekRepository(db)
        accountManager = AccountManager(db.userAccountDao())
        cloudSyncEngine = CloudSyncEngine(application, db)
        focusAudioHelper = FocusAudioHelper(application)
        SmartNotificationManager.initializeChannels(application)
        _inAppNotifications.value = SmartNotificationManager.getHistory()
        initializeInitialSettingsIfEmpty()
        YawmekWidgetUpdater.requestUpdate(application)
    }

    private fun initializeInitialSettingsIfEmpty() {
        viewModelScope.launch {
            val settings = repository.getUserSettings()
            if (settings == null) {
                repository.updateUserSettings(UserSettingsEntity(id = 1))
            }
            val prefs = repository.getNotificationPreferences()
            if (prefs == null) {
                repository.updateNotificationPreferences(NotificationPreferencesEntity(id = 1))
            }
        }
    }

    // Combine flows into single reactive UI State
    val uiState: StateFlow<YawmekUiState> = combine(
        repository.allTasks,
        repository.allExpenses,
        repository.allBudgets,
        repository.allHabits,
        repository.getHabitLogsSince(LocalDate.now().minusDays(35).toEpochDay()),
        repository.allGoals,
        repository.allMilestones,
        repository.allNotes,
        repository.userSettings,
        _chatMessages,
        _isAiThinking,
        _searchQuery,
        _celebrationToast,
        _showMorningBriefing,
        _showEveningSummary,
        repository.activeAccount,
        cloudSyncEngine.syncState,
        repository.notificationPreferences,
        repository.allFocusSessions,
        _calendarEvents,
        _isCalendarPermissionGranted,
        _inAppNotifications
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val tasks = args[0] as List<TaskEntity>
        @Suppress("UNCHECKED_CAST")
        val expenses = args[1] as List<ExpenseEntity>
        @Suppress("UNCHECKED_CAST")
        val budgets = args[2] as List<BudgetEntity>
        @Suppress("UNCHECKED_CAST")
        val habits = args[3] as List<HabitEntity>
        @Suppress("UNCHECKED_CAST")
        val habitLogs = args[4] as List<HabitLogEntity>
        @Suppress("UNCHECKED_CAST")
        val goals = args[5] as List<GoalEntity>
        @Suppress("UNCHECKED_CAST")
        val milestones = args[6] as List<MilestoneEntity>
        @Suppress("UNCHECKED_CAST")
        val notes = args[7] as List<NoteEntity>
        val settings = (args[8] as? UserSettingsEntity) ?: UserSettingsEntity()
        @Suppress("UNCHECKED_CAST")
        val messages = args[9] as List<ChatMessage>
        val isThinking = args[10] as Boolean
        val query = args[11] as String
        val celebration = args[12] as? String
        val morningBrief = args[13] as Boolean
        val eveningSum = args[14] as Boolean
        val activeAccount = args[15] as? UserAccountEntity
        val syncState = (args[16] as? SyncState) ?: SyncState.Idle
        val notifPrefs = (args[17] as? NotificationPreferencesEntity) ?: NotificationPreferencesEntity()
        @Suppress("UNCHECKED_CAST")
        val focusSessions = (args[18] as? List<FocusSessionEntity>) ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val calendarEvents = (args[19] as? List<CalendarEventItem>) ?: emptyList()
        val isCalGranted = (args[20] as? Boolean) ?: false
        @Suppress("UNCHECKED_CAST")
        val inAppNotifs = (args[21] as? List<InAppNotification>) ?: emptyList()

        val pending = tasks.filter { !it.isCompleted }

        // Recommendation calculation with Calendar awareness
        val recommendation = SmartRecommendationEngine.recommendNextAction(
            tasks = tasks,
            habits = habits,
            habitLogs = habitLogs,
            calendarEvents = calendarEvents,
            workStartHour = settings.workStartHour,
            workEndHour = settings.workEndHour
        )

        // Spending calculation
        val todayStartMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val monthStartMillis = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val todaySpent = expenses.filter { it.dateMillis >= todayStartMillis }.sumOf { it.amount }
        val monthSpent = expenses.filter { it.dateMillis >= monthStartMillis }.sumOf { it.amount }
        val monthlyBudgetAmount = budgets.firstOrNull { it.period == BudgetPeriod.MONTHLY }?.amount ?: 0.0

        val todayFocusMinutes = focusSessions
            .filter { it.startedAtMillis >= todayStartMillis }
            .sumOf { it.actualSeconds / 60 }

        // Search filtering
        val searchResults = if (query.isNotBlank()) {
            val q = query.lowercase().trim()
            GlobalSearchResults(
                tasks = tasks.filter { it.title.lowercase().contains(q) || it.description.lowercase().contains(q) },
                expenses = expenses.filter { it.note.lowercase().contains(q) || it.category.name.lowercase().contains(q) },
                habits = habits.filter { it.title.lowercase().contains(q) || it.category.lowercase().contains(q) },
                goals = goals.filter { it.title.lowercase().contains(q) || it.description.lowercase().contains(q) },
                notes = notes.filter { it.title.lowercase().contains(q) || it.content.lowercase().contains(q) || it.tag.lowercase().contains(q) }
            )
        } else {
            GlobalSearchResults()
        }

        YawmekUiState(
            isLoading = false,
            userSettings = settings,
            tasks = tasks,
            pendingTasks = pending,
            expenses = expenses,
            budgets = budgets,
            habits = habits,
            habitLogs = habitLogs,
            goals = goals,
            milestones = milestones,
            notes = notes,
            recommendation = recommendation,
            todaySpending = todaySpent,
            monthlySpending = monthSpent,
            monthlyBudget = monthlyBudgetAmount,
            chatMessages = messages,
            isAiThinking = isThinking,
            searchQuery = query,
            searchResults = searchResults,
            showCelebrationToast = celebration,
            showMorningBriefing = morningBrief,
            showEveningSummary = eveningSum,
            activeAccount = activeAccount,
            syncState = syncState,
            notificationPreferences = notifPrefs,
            calendarEvents = calendarEvents,
            isCalendarPermissionGranted = isCalGranted,
            focusSessions = focusSessions,
            todayFocusMinutes = todayFocusMinutes,
            inAppNotifications = inAppNotifs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = YawmekUiState(isLoading = true)
    )

    // User & Onboarding Actions
    fun completeOnboarding(userName: String, selectedPriorities: List<String>) {
        viewModelScope.launch {
            val current = repository.getUserSettings() ?: UserSettingsEntity()
            val updated = current.copy(
                userName = userName.trim(),
                hasCompletedOnboarding = true,
                selectedPriorities = selectedPriorities.joinToString(",")
            )
            repository.updateUserSettings(updated)
        }
    }

    fun updateUserSettings(updated: UserSettingsEntity) {
        viewModelScope.launch {
            repository.updateUserSettings(updated)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            val current = repository.getUserSettings() ?: UserSettingsEntity()
            repository.updateUserSettings(current.copy(language = language))
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            val current = repository.getUserSettings() ?: UserSettingsEntity()
            repository.updateUserSettings(current.copy(themeMode = themeMode))
        }
    }

    fun setCurrency(currency: String) {
        viewModelScope.launch {
            val current = repository.getUserSettings() ?: UserSettingsEntity()
            repository.updateUserSettings(current.copy(currency = currency))
        }
    }

    // Task Actions
    fun addTaskFromNaturalLanguage(text: String) {
        viewModelScope.launch {
            val draft = NaturalLanguageParser.parse(text)
            if (draft.title.isNotBlank()) {
                val task = TaskEntity(
                    title = draft.title,
                    dueDateMillis = draft.dueDateMillis,
                    dueTimeMinutes = draft.dueTimeMinutes,
                    durationMinutes = draft.durationMinutes,
                    priority = draft.priority,
                    category = draft.category
                )
                repository.insertTask(task)
                triggerCelebration("Task added: ${draft.title}")
            }
        }
    }

    fun createTask(
        title: String,
        description: String = "",
        dueDateMillis: Long? = null,
        dueTimeMinutes: Int? = null,
        durationMinutes: Int = 30,
        priority: Priority = Priority.MEDIUM,
        category: TaskCategory = TaskCategory.PERSONAL,
        recurrence: Recurrence = Recurrence.NONE,
        reminderEnabled: Boolean = false,
        subtasks: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val taskId = repository.insertTask(
                TaskEntity(
                    title = title.trim(),
                    description = description.trim(),
                    dueDateMillis = dueDateMillis,
                    dueTimeMinutes = dueTimeMinutes,
                    durationMinutes = durationMinutes,
                    priority = priority,
                    category = category,
                    recurrence = recurrence,
                    reminderEnabled = reminderEnabled
                )
            )
            subtasks.filter { it.isNotBlank() }.forEach { sub ->
                repository.insertSubtask(SubtaskEntity(taskId = taskId, title = sub.trim()))
            }
            triggerCelebration("Task created!")
            YawmekWidgetUpdater.requestUpdate(getApplication())
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
            YawmekWidgetUpdater.requestUpdate(getApplication())
            if (!task.isCompleted) {
                triggerCelebration("Great job! Completed: ${task.title}")
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            YawmekWidgetUpdater.requestUpdate(getApplication())
        }
    }

    fun rescheduleTask(task: TaskEntity, newDueDateMillis: Long?, newDueTimeMinutes: Int?) {
        viewModelScope.launch {
            repository.updateTask(task.copy(dueDateMillis = newDueDateMillis, dueTimeMinutes = newDueTimeMinutes))
        }
    }

    // Expense & Budget Actions
    fun addExpense(
        amount: Double,
        category: ExpenseCategory,
        note: String = "",
        dateMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val currency = repository.getUserSettings()?.currency ?: "EGP"
            repository.insertExpense(
                ExpenseEntity(
                    amount = amount,
                    currency = currency,
                    category = category,
                    note = note.trim(),
                    dateMillis = dateMillis
                )
            )
            triggerCelebration("Expense logged: $amount $currency")
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun setBudget(period: BudgetPeriod, amount: Double) {
        viewModelScope.launch {
            val currency = repository.getUserSettings()?.currency ?: "EGP"
            repository.insertOrUpdateBudget(
                BudgetEntity(
                    period = period,
                    amount = amount,
                    currency = currency
                )
            )
        }
    }

    // Habit Actions
    fun createHabit(
        title: String,
        category: String = "Personal",
        frequency: HabitFrequency = HabitFrequency.DAILY,
        targetDaysPerWeek: Int = 7,
        colorHex: String = "#3B82F6",
        iconKey: String = "check"
    ) {
        viewModelScope.launch {
            repository.insertHabit(
                HabitEntity(
                    title = title.trim(),
                    category = category.trim(),
                    frequency = frequency,
                    targetDaysPerWeek = targetDaysPerWeek,
                    colorHex = colorHex,
                    iconKey = iconKey
                )
            )
            triggerCelebration("New habit created!")
        }
    }

    fun toggleHabit(habitId: Long, dateEpochDay: Long = LocalDate.now().toEpochDay()) {
        viewModelScope.launch {
            repository.toggleHabitForDate(habitId, dateEpochDay)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // Goal Actions
    fun createGoal(
        title: String,
        description: String = "",
        category: String = "General",
        targetDateMillis: Long? = null,
        colorHex: String = "#8B5CF6",
        milestoneTitles: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val goalId = repository.insertGoal(
                GoalEntity(
                    title = title.trim(),
                    description = description.trim(),
                    category = category.trim(),
                    targetDateMillis = targetDateMillis,
                    colorHex = colorHex
                )
            )
            milestoneTitles.filter { it.isNotBlank() }.forEachIndexed { idx, mTitle ->
                repository.insertMilestone(
                    MilestoneEntity(
                        goalId = goalId,
                        title = mTitle.trim(),
                        orderIndex = idx
                    )
                )
            }
            triggerCelebration("Goal created!")
        }
    }

    fun toggleMilestone(milestone: MilestoneEntity) {
        viewModelScope.launch {
            repository.toggleMilestone(milestone)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // Note Actions
    fun createNote(title: String, content: String, tag: String = "", isPinned: Boolean = false) {
        viewModelScope.launch {
            repository.insertNote(
                NoteEntity(
                    title = title.trim(),
                    content = content.trim(),
                    tag = tag.trim(),
                    isPinned = isPinned
                )
            )
            triggerCelebration("Note saved")
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(updatedAtMillis = System.currentTimeMillis()))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun toggleNotePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    // YAWMEK AI Interaction
    fun sendAiPrompt(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = ChatMessage(isUser = true, text = prompt.trim())
        _chatMessages.update { it + userMsg }
        _isAiThinking.value = true

        viewModelScope.launch {
            val state = uiState.value
            val contextData = UserContextData(
                userName = state.userSettings.userName,
                language = state.userSettings.language,
                currency = state.userSettings.currency,
                tasks = state.tasks,
                pendingTasksSummary = state.pendingTasks.take(5).joinToString(", ") { "${it.title} (${it.priority})" },
                todaySpending = state.todaySpending,
                habitsSummary = state.habits.take(5).joinToString(", ") { it.title }
            )

            val aiResult: AiResponseResult = YawmekAiEngine.generateAiAdvice(prompt, contextData)
            _isAiThinking.value = false

            val assistantMsg = ChatMessage(
                isUser = false,
                text = aiResult.messageText,
                planItems = aiResult.suggestedPlan
            )
            _chatMessages.update { it + assistantMsg }
        }
    }

    fun applyAiPlan(plan: List<AiDailyPlanItem>) {
        viewModelScope.launch {
            val todayMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            for (item in plan) {
                // Parse timeFormatted
                val parsedDraft = NaturalLanguageParser.parse("at ${item.timeFormatted}")
                repository.insertTask(
                    TaskEntity(
                        title = item.title,
                        description = item.reason,
                        dueDateMillis = todayMillis,
                        dueTimeMinutes = parsedDraft.dueTimeMinutes ?: 960,
                        durationMinutes = item.durationMinutes,
                        priority = item.priority,
                        category = TaskCategory.WORK
                    )
                )
            }
            triggerCelebration("Plan applied to today's schedule!")
        }
    }

    // Search & Dialog Controls
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleMorningBriefing(show: Boolean) {
        _showMorningBriefing.value = show
    }

    fun toggleEveningSummary(show: Boolean) {
        _showEveningSummary.value = show
    }

    fun triggerCelebration(msg: String) {
        _celebrationToast.value = msg
    }

    fun clearCelebration() {
        _celebrationToast.value = null
    }

    // Focus Actions
    fun saveFocusSession(session: FocusSessionEntity, markTaskComplete: Boolean) {
        viewModelScope.launch {
            repository.insertFocusSession(session)
            if (markTaskComplete && session.taskId != null) {
                val task = uiState.value.tasks.firstOrNull { it.id == session.taskId }
                if (task != null && !task.isCompleted) {
                    repository.toggleTaskCompletion(task)
                }
            }
            triggerCelebration("Focus session saved! (+${session.durationMinutes}m)")
            YawmekWidgetUpdater.requestUpdate(getApplication())
        }
    }

    // Account & Cloud Sync Actions
    fun signIn(email: String, pass: String, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            when (val res = accountManager.signIn(email, pass)) {
                is AuthResult.Success -> {
                    triggerCelebration("Welcome back, ${res.account.displayName}!")
                    cloudSyncEngine.syncNow()
                    onResult(true, null)
                }
                is AuthResult.Error -> {
                    onResult(false, res.message)
                }
            }
        }
    }

    fun signUp(email: String, name: String, pass: String, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            when (val res = accountManager.signUp(email, name, pass)) {
                is AuthResult.Success -> {
                    triggerCelebration("Account created! Syncing your data...")
                    cloudSyncEngine.syncNow()
                    onResult(true, null)
                }
                is AuthResult.Error -> {
                    onResult(false, res.message)
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            accountManager.signOut()
            triggerCelebration("Signed out.")
        }
    }

    fun requestPasswordReset(email: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val msg = accountManager.requestPasswordReset(email)
            onResult(msg)
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val acc = accountManager.getActiveAccount()
            if (acc != null) {
                accountManager.deleteAccount(acc.id)
                triggerCelebration("Account deleted.")
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            val result = cloudSyncEngine.syncNow()
            if (result is SyncState.Synced) {
                triggerCelebration("Cloud sync complete! (${result.itemsSynced} items)")
                YawmekWidgetUpdater.requestUpdate(getApplication())
            } else if (result is SyncState.OfflinePending) {
                triggerCelebration("${result.pendingCount} changes queued offline.")
            } else if (result is SyncState.Error) {
                triggerCelebration("Sync failed: ${result.message}")
            }
        }
    }

    // Calendar Integration Actions
    fun setCalendarPermissionGranted(granted: Boolean) {
        _isCalendarPermissionGranted.value = granted
    }

    fun refreshCalendarEvents() {
        refreshCalendarEvents(getApplication())
    }

    fun refreshCalendarEvents(context: Context) {
        viewModelScope.launch {
            val events = CalendarManager.fetchEventsForDay(context, LocalDate.now())
            _calendarEvents.value = events
        }
    }

    fun exportTaskToCalendar(task: TaskEntity) {
        exportTaskToCalendar(getApplication(), task)
    }

    fun exportTaskToCalendar(context: Context, task: TaskEntity) {
        viewModelScope.launch {
            val todayMillis = task.dueDateMillis ?: System.currentTimeMillis()
            val startMillis = if (task.dueTimeMinutes != null) {
                val date = java.time.Instant.ofEpochMilli(todayMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                date.atTime(task.dueTimeMinutes / 60, task.dueTimeMinutes % 60).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                todayMillis
            }
            val success = CalendarManager.addEventToCalendar(
                context = context,
                title = task.title,
                description = task.description.ifBlank { "Task scheduled in YAWMEK" },
                startMillis = startMillis,
                durationMinutes = task.durationMinutes
            )
            if (success) {
                refreshCalendarEvents(context)
                triggerCelebration("Exported to device calendar! 📅")
            } else {
                triggerCelebration("Could not add to calendar. Check permissions.")
            }
        }
    }

    // Notification Preferences
    fun updateNotificationPreferences(
        quietHoursEnabled: Boolean,
        taskRemindersEnabled: Boolean,
        budgetWarningsEnabled: Boolean,
        habitRemindersEnabled: Boolean
    ) {
        val current = uiState.value.notificationPreferences
        updateNotificationPreferences(
            current.copy(
                quietHoursEnabled = quietHoursEnabled,
                taskRemindersEnabled = taskRemindersEnabled,
                budgetWarningsEnabled = budgetWarningsEnabled,
                habitRemindersEnabled = habitRemindersEnabled
            )
        )
    }

    fun updateNotificationPreferences(prefs: NotificationPreferencesEntity) {
        viewModelScope.launch {
            repository.updateNotificationPreferences(prefs)
            triggerCelebration("Notification preferences updated.")
        }
    }

    fun refreshInAppNotifications() {
        _inAppNotifications.value = SmartNotificationManager.getHistory()
    }

    fun clearNotificationHistory() {
        SmartNotificationManager.clearHistory()
        _inAppNotifications.value = emptyList()
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _chatMessages.value = emptyList()
            YawmekWidgetUpdater.requestUpdate(getApplication())
            triggerCelebration("Workspace reset to clean slate.")
        }
    }
}
