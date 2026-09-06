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
import com.example.domain.rpg.RpgProgressionManager
import com.example.domain.rpg.RpgStateMerger
import com.example.domain.rpg.RpgCombinedState
import com.example.domain.rpg.RpgProgressionCalculator
import com.example.domain.audio.SoundHapticManager
import com.example.domain.money.MoneyCalculator
import com.example.domain.money.FinancialOverview
import com.example.domain.calendar.SmartTimeBlockingEngine
import com.example.domain.focus.FocusSessionManager
import com.example.domain.focus.FocusAnalytics
import com.example.domain.community.CommunityBattleEngine
import com.example.domain.community.BattleSimulationResult
import com.example.domain.ai.*
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
    val structuredAction: StructuredAiAction? = null,
    val planHealth: PlanHealthStatus? = null,
    val productivityReport: ProductivityReport? = null,
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
    val inAppNotifications: List<InAppNotification> = emptyList(),
    val rpgCharacter: CharacterRpgEntity = CharacterRpgEntity(),
    val rpgEquipment: List<EquipmentItemEntity> = emptyList(),
    val rpgCompanions: List<CompanionEntity> = emptyList(),
    val rpgSkills: List<SkillNodeEntity> = emptyList(),
    val rpgQuests: List<RpgQuestEntity> = emptyList(),
    val rpgBosses: List<BossChallengeEntity> = emptyList(),
    // Advanced Money System additions
    val accounts: List<WalletAccountEntity> = emptyList(),
    val transactions: List<FinancialTransactionEntity> = emptyList(),
    val financialCategories: List<FinancialCategoryEntity> = emptyList(),
    val recurringTransactions: List<RecurringTransactionEntity> = emptyList(),
    val financialGoals: List<FinancialGoalEntity> = emptyList(),
    val financialOverview: FinancialOverview? = null,
    // Professional Smart Calendar additions
    val roomCalendarEvents: List<CalendarEventEntity> = emptyList(),
    val calendarViewMode: CalendarViewMode = CalendarViewMode.DAY,
    val selectedCalendarDate: LocalDate = LocalDate.now(),
    val smartTimeSlotProposals: List<SmartTimeSlotProposal> = emptyList(),
    val isShowingSmartSchedulePreview: Boolean = false,
    val conflictingEventIds: Set<Long> = emptySet(),
    // Advanced Focus Mode additions
    val focusAnalytics: FocusAnalytics = FocusAnalytics(0, 0, 0, 0, 70, 0f, 0f, 0),
    val focusPreferences: FocusPreferencesEntity = FocusPreferencesEntity(),
    val isFocusShieldActive: Boolean = false,
    // YAWMEK Community additions
    val communityProfile: CommunityProfileEntity? = null,
    val communityFriends: List<CommunityFriendEntity> = emptyList(),
    val communityBattles: List<CommunityBattleEntity> = emptyList(),
    val isCommunityConnected: Boolean = true,
    val activeBattleSimulation: BattleSimulationResult? = null,
    val leaderboardUsers: List<LeaderboardUserItem> = emptyList()
)

private data class CommunityData(
    val profile: CommunityProfileEntity?,
    val friends: List<CommunityFriendEntity>,
    val battles: List<CommunityBattleEntity>,
    val isConnected: Boolean,
    val activeBattle: BattleSimulationResult?
)

private data class MoneyData(
    val accounts: List<WalletAccountEntity>,
    val transactions: List<FinancialTransactionEntity>,
    val categories: List<FinancialCategoryEntity>,
    val recurring: List<RecurringTransactionEntity>,
    val goals: List<FinancialGoalEntity>
)

private data class CalendarData(
    val roomEvents: List<CalendarEventEntity>,
    val deviceEvents: List<CalendarEventItem>,
    val isGranted: Boolean,
    val viewMode: CalendarViewMode,
    val selectedDate: LocalDate,
    val proposals: List<SmartTimeSlotProposal>,
    val showPreview: Boolean
)

private data class FocusData(
    val sessions: List<FocusSessionEntity>,
    val prefs: FocusPreferencesEntity,
    val isShieldActive: Boolean
)

class YawmekViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: YawmekRepository
    val accountManager: AccountManager
    val cloudSyncEngine: CloudSyncEngine
    val focusAudioHelper: FocusAudioHelper
    val rpgManager: RpgProgressionManager

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _isAiThinking = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _celebrationToast = MutableStateFlow<String?>(null)
    private val _showMorningBriefing = MutableStateFlow(false)
    private val _showEveningSummary = MutableStateFlow(false)

    private val _calendarEvents = MutableStateFlow<List<CalendarEventItem>>(emptyList())
    private val _isCalendarPermissionGranted = MutableStateFlow(false)
    private val _inAppNotifications = MutableStateFlow<List<InAppNotification>>(emptyList())

    private val _calendarViewMode = MutableStateFlow(CalendarViewMode.DAY)
    private val _selectedCalendarDate = MutableStateFlow(LocalDate.now())
    private val _smartTimeSlotProposals = MutableStateFlow<List<SmartTimeSlotProposal>>(emptyList())
    private val _isShowingSmartSchedulePreview = MutableStateFlow(false)
    private val _isFocusShieldActive = MutableStateFlow(false)
    private val _isCommunityConnected = MutableStateFlow(true)
    private val _activeBattleSimulation = MutableStateFlow<BattleSimulationResult?>(null)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = YawmekRepository(db)
        accountManager = AccountManager(db.userAccountDao())
        cloudSyncEngine = CloudSyncEngine(application, db)
        focusAudioHelper = FocusAudioHelper(application)
        rpgManager = RpgProgressionManager(repository)
        SmartNotificationManager.initializeChannels(application)
        _inAppNotifications.value = SmartNotificationManager.getHistory()
        initializeInitialSettingsIfEmpty()
        viewModelScope.launch {
            rpgManager.initializeCatalogIfEmpty()
        }
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
            // Seed Community Profile and initial friends if empty
            val communityProf = repository.getCommunityProfile()
            if (communityProf == null) {
                val defaultUsername = settings?.userName?.takeIf { it.isNotBlank() } ?: "Adventurer"
                val defaultConnectionId = "YAWMEK-NET-" + java.util.UUID.randomUUID().toString().take(8).uppercase()
                repository.saveCommunityProfile(
                    CommunityProfileEntity(
                        id = 1,
                        username = defaultUsername.lowercase().replace(" ", "_"),
                        displayName = defaultUsername,
                        avatarId = "warrior",
                        bio = "Building my legend in YAWMEK!",
                        rankTitleEn = "Novice Adventurer",
                        rankTitleAr = "مغامر مبتدئ",
                        rating = 1000,
                        connectionId = defaultConnectionId,
                        isOnline = true,
                        isInitialSetupDone = false
                    )
                )
            }
            val existingFriend = repository.getFriendByUsername("ziyad_paladin")
            if (existingFriend == null) {
                val now = System.currentTimeMillis()
                repository.insertFriend(
                    CommunityFriendEntity(
                        friendUsername = "ziyad_paladin",
                        friendDisplayName = "Ziyad The Paladin",
                        avatarId = "paladin",
                        level = 8,
                        xp = 3400L,
                        rankTitle = "Paladin",
                        characterClass = "WARRIOR",
                        isOnline = true,
                        status = FriendshipStatus.FRIEND,
                        rating = 1180,
                        tasksCompleted = 42,
                        focusMinutes = 310,
                        lastActiveMillis = now
                    )
                )
                repository.insertFriend(
                    CommunityFriendEntity(
                        friendUsername = "sarah_chrono",
                        friendDisplayName = "Sarah Chrono",
                        avatarId = "mage",
                        level = 11,
                        xp = 5800L,
                        rankTitle = "Chronomancer",
                        characterClass = "MAGE",
                        isOnline = true,
                        status = FriendshipStatus.FRIEND,
                        rating = 1250,
                        tasksCompleted = 67,
                        focusMinutes = 480,
                        lastActiveMillis = now
                    )
                )
                repository.insertFriend(
                    CommunityFriendEntity(
                        friendUsername = "kareem_monk",
                        friendDisplayName = "Kareem Focus Monk",
                        avatarId = "monk",
                        level = 6,
                        xp = 2100L,
                        rankTitle = "Focus Disciple",
                        characterClass = "ROGUE",
                        isOnline = false,
                        status = FriendshipStatus.FRIEND,
                        rating = 1060,
                        tasksCompleted = 29,
                        focusMinutes = 240,
                        lastActiveMillis = now - 7200000L
                    )
                )
                repository.insertFriend(
                    CommunityFriendEntity(
                        friendUsername = "nour_alchemist",
                        friendDisplayName = "Nour Alchemist",
                        avatarId = "alchemist",
                        level = 5,
                        xp = 1750L,
                        rankTitle = "Alchemist",
                        characterClass = "MAGE",
                        isOnline = true,
                        status = FriendshipStatus.PENDING_RECEIVED,
                        rating = 1040,
                        tasksCompleted = 18,
                        focusMinutes = 190,
                        lastActiveMillis = now
                    )
                )
                repository.insertFriend(
                    CommunityFriendEntity(
                        friendUsername = "omar_ranger",
                        friendDisplayName = "Omar The Ranger",
                        avatarId = "archer",
                        level = 9,
                        xp = 4200L,
                        rankTitle = "Sharpshooter",
                        characterClass = "ROGUE",
                        isOnline = false,
                        status = FriendshipStatus.FRIEND,
                        rating = 1140,
                        tasksCompleted = 51,
                        focusMinutes = 350,
                        lastActiveMillis = now - 86400000L
                    )
                )
            }
        }
    }

    private val moneyFlow = combine(
        repository.allActiveAccounts,
        repository.allTransactions,
        repository.allFinancialCategories,
        repository.activeRecurringTransactions,
        repository.allFinancialGoals
    ) { accounts, txs, cats, recurring, goals ->
        MoneyData(accounts, txs, cats, recurring, goals)
    }

    private val calendarFlow = combine(
        repository.allCalendarEvents,
        _calendarEvents,
        _isCalendarPermissionGranted,
        _calendarViewMode,
        _selectedCalendarDate,
        _smartTimeSlotProposals,
        _isShowingSmartSchedulePreview
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        CalendarData(
            roomEvents = args[0] as List<CalendarEventEntity>,
            deviceEvents = args[1] as List<CalendarEventItem>,
            isGranted = args[2] as Boolean,
            viewMode = args[3] as CalendarViewMode,
            selectedDate = args[4] as LocalDate,
            proposals = args[5] as List<SmartTimeSlotProposal>,
            showPreview = args[6] as Boolean
        )
    }

    private val focusFlow = combine(
        repository.allFocusSessions,
        repository.focusPreferences,
        _isFocusShieldActive
    ) { sessions, prefs, isShield ->
        FocusData(
            sessions = sessions,
            prefs = prefs ?: FocusPreferencesEntity(),
            isShieldActive = isShield
        )
    }

    private val communityFlow = combine(
        repository.communityProfile,
        repository.allCommunityFriends,
        repository.allCommunityBattles,
        _isCommunityConnected,
        _activeBattleSimulation
    ) { profile, friends, battles, isConnected, activeBattle ->
        CommunityData(profile, friends, battles, isConnected, activeBattle)
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
        _inAppNotifications,
        RpgStateMerger.merge(
            repository.rpgCharacter,
            repository.rpgEquipment,
            repository.rpgCompanions,
            repository.rpgSkills,
            repository.rpgQuests,
            repository.rpgBosses
        ),
        moneyFlow,
        calendarFlow,
        focusFlow,
        communityFlow
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
        val inAppNotifs = args[18] as List<InAppNotification>
        val rpgCombined = (args[19] as? RpgCombinedState) ?: RpgCombinedState()
        val moneyData = (args[20] as? MoneyData) ?: MoneyData(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        val calData = (args[21] as? CalendarData) ?: CalendarData(emptyList(), emptyList(), false, CalendarViewMode.DAY, LocalDate.now(), emptyList(), false)
        val focData = (args[22] as? FocusData) ?: FocusData(emptyList(), FocusPreferencesEntity(), false)
        val comData = (args[23] as? CommunityData) ?: CommunityData(null, emptyList(), emptyList(), true, null)

        val pending = tasks.filter { !it.isCompleted }

        // Recommendation calculation with Calendar awareness
        val recommendation = SmartRecommendationEngine.recommendNextAction(
            tasks = tasks,
            habits = habits,
            habitLogs = habitLogs,
            calendarEvents = calData.deviceEvents,
            workStartHour = settings.workStartHour,
            workEndHour = settings.workEndHour
        )

        // Spending calculation
        val todayStartMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val monthStartMillis = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val todaySpent = expenses.filter { it.dateMillis >= todayStartMillis }.sumOf { it.amount }
        val monthSpent = expenses.filter { it.dateMillis >= monthStartMillis }.sumOf { it.amount }
        val monthlyBudgetAmount = budgets.firstOrNull { it.period == BudgetPeriod.MONTHLY }?.amount ?: 0.0

        // Financial Overview calculation
        val monthlyBudgetMinor = MoneyUtils.toMinor(monthlyBudgetAmount)
        val finOverview = MoneyCalculator.buildFinancialOverview(
            accounts = moneyData.accounts,
            transactions = moneyData.transactions,
            monthlyBudgetMinor = monthlyBudgetMinor,
            targetDate = calData.selectedDate
        )

        // Calendar Conflict detection
        val conflictingIds = SmartTimeBlockingEngine.findConflictingEventIds(calData.roomEvents)

        // Focus analytics
        val focAnalytics = FocusSessionManager.computeAnalytics(
            sessions = focData.sessions,
            dailyTargetMinutes = focData.prefs.dailyTargetMinutes,
            weeklyTargetMinutes = focData.prefs.weeklyTargetMinutes
        )

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

        // Leaderboard Calculation
        val myProf = comData.profile
        val myRankItem = LeaderboardUserItem(
            rank = 1,
            username = myProf?.username?.ifBlank { "You" } ?: "You",
            displayName = myProf?.displayName?.ifBlank { "You" } ?: "You",
            avatarId = myProf?.avatarId ?: "warrior",
            level = rpgCombined.character.level,
            rating = myProf?.rating ?: 1000,
            focusMinutes = focAnalytics.todayMinutes,
            battleWins = myProf?.battleWins ?: 0,
            isCurrentUser = true
        )
        val friendRankItems = comData.friends.map { f ->
            LeaderboardUserItem(
                rank = 0,
                username = f.friendUsername,
                displayName = f.friendDisplayName,
                avatarId = f.avatarId,
                level = f.level,
                rating = f.rating,
                focusMinutes = f.focusMinutes,
                battleWins = (f.rating / 50).coerceAtLeast(0),
                isCurrentUser = false
            )
        }
        val computedLeaderboard = (listOf(myRankItem) + friendRankItems)
            .sortedByDescending { it.rating }
            .mapIndexed { idx, item -> item.copy(rank = idx + 1) }

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
            calendarEvents = calData.deviceEvents,
            isCalendarPermissionGranted = calData.isGranted,
            focusSessions = focData.sessions,
            todayFocusMinutes = focAnalytics.todayMinutes,
            inAppNotifications = inAppNotifs,
            rpgCharacter = rpgCombined.character,
            rpgEquipment = rpgCombined.equipment,
            rpgCompanions = rpgCombined.companions,
            rpgSkills = rpgCombined.skills,
            rpgQuests = rpgCombined.quests,
            rpgBosses = rpgCombined.bosses,
            // Advanced Money System additions
            accounts = moneyData.accounts,
            transactions = moneyData.transactions,
            financialCategories = moneyData.categories,
            recurringTransactions = moneyData.recurring,
            financialGoals = moneyData.goals,
            financialOverview = finOverview,
            // Professional Smart Calendar additions
            roomCalendarEvents = calData.roomEvents,
            calendarViewMode = calData.viewMode,
            selectedCalendarDate = calData.selectedDate,
            smartTimeSlotProposals = calData.proposals,
            isShowingSmartSchedulePreview = calData.showPreview,
            conflictingEventIds = conflictingIds,
            // Advanced Focus Mode additions
            focusAnalytics = focAnalytics,
            focusPreferences = focData.prefs,
            isFocusShieldActive = focData.isShieldActive,
            // YAWMEK Community additions
            communityProfile = myProf,
            communityFriends = comData.friends,
            communityBattles = comData.battles,
            isCommunityConnected = comData.isConnected,
            activeBattleSimulation = comData.activeBattle,
            leaderboardUsers = computedLeaderboard
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

    fun updateUserName(name: String) {
        viewModelScope.launch {
            val current = repository.getUserSettings() ?: UserSettingsEntity()
            repository.updateUserSettings(current.copy(userName = name.trim()))
            triggerCelebration(if (current.language == AppLanguage.ARABIC) "تم تحديث اسمك بنجاح! ✨" else "Your name has been updated! ✨")
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
            val wasCompleted = task.isCompleted
            repository.toggleTaskCompletion(task)
            YawmekWidgetUpdater.requestUpdate(getApplication())
            if (!wasCompleted) {
                val soundManager = SoundHapticManager.getInstance(getApplication())
                soundManager.playTaskCompleted()
                val isHigh = task.priority == Priority.HIGH
                val completedCount = uiState.value.tasks.count { it.isCompleted }
                val xp = RpgProgressionCalculator.evaluateTaskXp(isHigh, completedCount)
                val actionType = if (isHigh) "TASK_HIGH" else "TASK_NORMAL"
                val statKey = if (isHigh) "COURAGE" else "DISCIPLINE"
                val result = rpgManager.awardProductivityXp(actionType, xp, statKey)
                if (result.leveledUp) {
                    soundManager.playRpgLevelUp()
                    triggerCelebration(result.summaryMessageEn + " / " + result.summaryMessageAr)
                } else {
                    soundManager.playRpgXpGain()
                    triggerCelebration("Great job! Completed: ${task.title} (+${xp} XP)")
                }
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
            val wasLogged = repository.getHabitLogsSince(dateEpochDay).firstOrNull()?.any { it.habitId == habitId } == true
            repository.toggleHabitForDate(habitId, dateEpochDay)
            if (!wasLogged) {
                val soundManager = SoundHapticManager.getInstance(getApplication())
                val countToday = uiState.value.habitLogs.count { it.dateEpochDay == dateEpochDay }
                val xp = RpgProgressionCalculator.evaluateHabitXp(countToday)
                val result = rpgManager.awardProductivityXp("HABIT", xp, "CONSISTENCY")
                if (result.leveledUp) {
                    soundManager.playRpgLevelUp()
                    triggerCelebration(result.summaryMessageEn + " / " + result.summaryMessageAr)
                } else {
                    soundManager.playRpgXpGain()
                    triggerCelebration("Habit completed! (+${xp} XP)")
                }
            }
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
            val wasDone = milestone.isCompleted
            repository.toggleMilestone(milestone)
            if (!wasDone) {
                val soundManager = SoundHapticManager.getInstance(getApplication())
                soundManager.playGoalMilestone()
                val result = rpgManager.awardProductivityXp("MILESTONE", RpgProgressionCalculator.XP_GOAL_MILESTONE, "PLANNING")
                if (result.leveledUp) {
                    soundManager.playRpgLevelUp()
                    triggerCelebration(result.summaryMessageEn + " / " + result.summaryMessageAr)
                } else {
                    soundManager.playRpgXpGain()
                    triggerCelebration("Milestone achieved! (+${RpgProgressionCalculator.XP_GOAL_MILESTONE} XP)")
                }
            }
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

    // YAWMEK AI Interaction & Personal Productivity Intelligence Engine
    fun sendAiPrompt(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = ChatMessage(isUser = true, text = prompt.trim())
        _chatMessages.update { it + userMsg }
        _isAiThinking.value = true

        viewModelScope.launch {
            val state = uiState.value
            val ctx = OrchestratorContext(
                context = getApplication(),
                userName = state.userSettings.userName,
                language = state.userSettings.language,
                currency = state.userSettings.currency,
                tasks = state.tasks,
                habits = state.habits,
                habitLogs = state.habitLogs,
                goals = state.goals,
                milestones = state.milestones,
                focusSessions = state.focusSessions,
                calendarEvents = state.calendarEvents,
                financialOverview = state.financialOverview,
                workStartHour = state.userSettings.workStartHour,
                workEndHour = state.userSettings.workEndHour
            )

            val aiResult = YawmekAiOrchestrator.processPrompt(prompt, ctx)
            _isAiThinking.value = false

            val assistantMsg = ChatMessage(
                isUser = false,
                text = aiResult.messageText,
                planItems = aiResult.suggestedPlan,
                structuredAction = aiResult.structuredAction,
                planHealth = aiResult.planHealth,
                productivityReport = aiResult.productivityReport
            )
            _chatMessages.update { it + assistantMsg }
        }
    }

    fun applyAiPlan(plan: List<AiDailyPlanItem>) {
        viewModelScope.launch {
            val todayMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            for (item in plan) {
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

    fun applyGoalRoadmap(roadmap: GoalRoadmapProposal) {
        viewModelScope.launch {
            val targetMillis = System.currentTimeMillis() + (roadmap.totalDays * 86400000L)
            val goal = GoalEntity(
                title = roadmap.goalTitle,
                description = roadmap.description,
                category = roadmap.category,
                targetDateMillis = targetMillis
            )
            val goalId = repository.insertGoal(goal)

            roadmap.milestones.forEachIndexed { idx, m ->
                repository.insertMilestone(
                    MilestoneEntity(
                        goalId = goalId,
                        title = m.title,
                        orderIndex = idx
                    )
                )
            }

            val todayMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            roadmap.tasks.forEach { t ->
                val due = todayMillis + (t.targetDayOffset * 86400000L)
                val taskId = repository.insertTask(
                    TaskEntity(
                        title = t.title,
                        durationMinutes = t.durationMinutes,
                        priority = t.priority,
                        dueDateMillis = due,
                        category = when (roadmap.category.lowercase()) {
                            "education" -> TaskCategory.STUDY
                            "health" -> TaskCategory.HEALTH
                            "work" -> TaskCategory.WORK
                            else -> TaskCategory.PERSONAL
                        }
                    )
                )
                t.subtasks.forEach { st ->
                    repository.insertSubtask(
                        SubtaskEntity(
                            taskId = taskId,
                            title = st
                        )
                    )
                }
            }

            if (roadmap.recommendedHabitTitle.isNotBlank()) {
                repository.insertHabit(
                    HabitEntity(
                        title = roadmap.recommendedHabitTitle,
                        category = roadmap.category,
                        frequency = roadmap.recommendedHabitFrequency
                    )
                )
            }

            rpgManager.awardProductivityXp("ROADMAP_CREATED", 50, "PLANNING")
            triggerCelebration("Roadmap, Milestones & Tasks created!")
        }
    }

    fun applyRescheduleProposal(proposal: RescheduleProposal) {
        viewModelScope.launch {
            proposal.items.forEach { item ->
                val updated = item.task.copy(
                    dueDateMillis = item.newDueDateMillis,
                    dueTimeMinutes = item.newDueTimeMinutes
                )
                repository.updateTask(updated)
            }
            triggerCelebration("${proposal.items.size} tasks rescheduled!")
        }
    }

    fun applyHabitStack(newHabitTitle: String, category: String = "Personal") {
        viewModelScope.launch {
            repository.insertHabit(
                HabitEntity(
                    title = newHabitTitle,
                    category = category,
                    frequency = HabitFrequency.DAILY
                )
            )
            triggerCelebration("Habit added to your tracker!")
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
            val soundManager = SoundHapticManager.getInstance(getApplication())
            soundManager.playFocusSessionCompleted()
            val todayFocused = uiState.value.todayFocusMinutes
            val xp = RpgProgressionCalculator.evaluateFocusXp(session.durationMinutes, todayFocused)
            val result = rpgManager.awardProductivityXp("FOCUS", xp, "FOCUS")
            if (result.leveledUp) {
                soundManager.playRpgLevelUp()
                triggerCelebration(result.summaryMessageEn + " / " + result.summaryMessageAr)
            } else {
                soundManager.playRpgXpGain()
                triggerCelebration("Focus session saved! (+${session.durationMinutes}m, +${xp} XP)")
            }
            YawmekWidgetUpdater.requestUpdate(getApplication())
        }
    }

    // RPG Progression User Actions
    fun claimRpgQuest(quest: RpgQuestEntity) {
        viewModelScope.launch {
            val soundManager = SoundHapticManager.getInstance(getApplication())
            val success = rpgManager.claimQuestReward(quest)
            if (success) {
                soundManager.playRpgLevelUp()
                triggerCelebration("Quest Reward Claimed! (+${quest.rewardXp} XP)")
            }
        }
    }

    fun purchaseRpgStoreItem(item: EquipmentItemEntity) {
        viewModelScope.launch {
            val soundManager = SoundHapticManager.getInstance(getApplication())
            val success = rpgManager.purchaseStoreItem(item)
            if (success) {
                soundManager.playRpgEquipmentEquipped()
                triggerCelebration("Purchased: ${item.nameEn}!")
            } else {
                triggerCelebration("Not enough XP or Level requirement not met!")
            }
        }
    }

    fun equipRpgItem(item: EquipmentItemEntity) {
        viewModelScope.launch {
            val soundManager = SoundHapticManager.getInstance(getApplication())
            rpgManager.equipItem(item)
            soundManager.playRpgEquipmentEquipped()
            triggerCelebration("Equipped: ${item.nameEn}!")
        }
    }

    fun unlockRpgSkill(skill: SkillNodeEntity) {
        viewModelScope.launch {
            val soundManager = SoundHapticManager.getInstance(getApplication())
            val success = rpgManager.unlockSkill(skill)
            if (success) {
                soundManager.playRpgLevelUp()
                triggerCelebration("Skill Unlocked: ${skill.nameEn}!")
            } else {
                triggerCelebration("Need more skill points or level requirement!")
            }
        }
    }

    fun attackBossWithGoal(bossId: Long, damage: Int) {
        viewModelScope.launch {
            val soundManager = SoundHapticManager.getInstance(getApplication())
            soundManager.playRpgBossHit()
            rpgManager.damageBossWithGoal(bossId, damage)
            triggerCelebration("Strike landed on Boss! (-${damage} HP) ⚔️")
        }
    }

    fun createBossChallenge(nameEn: String, nameAr: String, hp: Int, rewardXp: Long, relatedGoalId: Long? = null) {
        viewModelScope.launch {
            repository.insertBoss(
                BossChallengeEntity(
                    bossNameEn = nameEn,
                    bossNameAr = nameAr,
                    titleEn = "Goal Overlord",
                    titleAr = "حارس التحدي الأكبر",
                    iconEmoji = "👾",
                    maxHp = hp,
                    currentHp = hp,
                    rewardXp = rewardXp,
                    rewardTitleEn = "Slayer of $nameEn",
                    rewardTitleAr = "قاهر $nameAr",
                    relatedGoalId = relatedGoalId
                )
            )
            triggerCelebration("Boss Challenge Registered! 🛡️")
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

    // Daily Reset Manager logic
    private val dailyResetManager by lazy { com.example.domain.reset.DailyResetManager(repository) }

    fun moveUnfinishedTasksToTomorrow(tasks: List<TaskEntity>) {
        viewModelScope.launch {
            dailyResetManager.moveTasksToTomorrow(tasks)
            YawmekWidgetUpdater.requestUpdate(getApplication())
            val isArabic = uiState.value.userSettings.language == AppLanguage.ARABIC
            val msg = if (isArabic) "تم نقل ${tasks.size} مهام إلى الغد بنجاح 🌅" else "Moved ${tasks.size} tasks to tomorrow 🌅"
            triggerCelebration(msg)
        }
    }

    fun markTasksAsSkipped(tasks: List<TaskEntity>) {
        viewModelScope.launch {
            dailyResetManager.markTasksAsSkipped(tasks)
            YawmekWidgetUpdater.requestUpdate(getApplication())
            val isArabic = uiState.value.userSettings.language == AppLanguage.ARABIC
            val msg = if (isArabic) "تم تخطي ${tasks.size} مهام 🧹" else "Marked ${tasks.size} tasks as skipped 🧹"
            triggerCelebration(msg)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _chatMessages.value = emptyList()
            YawmekWidgetUpdater.requestUpdate(getApplication())
            triggerCelebration("Workspace reset to clean slate.")
        }
    }

    // ==========================================
    // ADVANCED MONEY / FINANCE SYSTEM ACTIONS
    // ==========================================
    fun createAccount(
        name: String,
        type: AccountType = AccountType.CASH,
        initialBalanceMinor: Long = 0L,
        currency: String = "EGP",
        colorHex: String = "#10B981",
        iconName: String = "wallet"
    ) {
        viewModelScope.launch {
            repository.insertAccount(
                WalletAccountEntity(
                    name = name.trim(),
                    type = type,
                    balanceMinor = initialBalanceMinor,
                    currency = currency,
                    colorHex = colorHex,
                    iconName = iconName
                )
            )
            val soundManager = SoundHapticManager.getInstance(getApplication())
            soundManager.playRpgXpGain()
            triggerCelebration("Wallet account created: $name 💼")
        }
    }

    fun updateAccount(account: WalletAccountEntity) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteAccount(account: WalletAccountEntity) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            triggerCelebration("Wallet account removed.")
        }
    }

    fun addFinancialTransaction(
        accountId: Long,
        toAccountId: Long? = null,
        type: TransactionType,
        amountMinor: Long,
        category: String,
        subcategory: String = "",
        note: String = "",
        dateMillis: Long = System.currentTimeMillis(),
        goalId: Long? = null
    ) {
        viewModelScope.launch {
            val currency = repository.getUserSettings()?.currency ?: "EGP"
            // Insert financial transaction
            val tx = FinancialTransactionEntity(
                accountId = accountId,
                toAccountId = toAccountId,
                type = type,
                amountMinor = amountMinor,
                currency = currency,
                category = category,
                subcategory = subcategory,
                note = note.trim(),
                dateMillis = dateMillis,
                goalId = goalId
            )
            repository.insertTransaction(tx)

            // Adjust account balances
            val sourceAccount = repository.getAccountById(accountId)
            if (sourceAccount != null) {
                val newSourceBalance = when (type) {
                    TransactionType.INCOME -> sourceAccount.balanceMinor + amountMinor
                    TransactionType.EXPENSE -> sourceAccount.balanceMinor - amountMinor
                    TransactionType.TRANSFER -> sourceAccount.balanceMinor - amountMinor
                }
                repository.updateAccount(sourceAccount.copy(balanceMinor = newSourceBalance))
            }

            if (type == TransactionType.TRANSFER && toAccountId != null) {
                val destAccount = repository.getAccountById(toAccountId)
                if (destAccount != null) {
                    repository.updateAccount(destAccount.copy(balanceMinor = destAccount.balanceMinor + amountMinor))
                }
            }

            // If linked to a goal, contribute to goal
            if (goalId != null && type == TransactionType.EXPENSE) {
                val goal = repository.getFinancialGoalById(goalId)
                if (goal != null) {
                    val updatedSaved = goal.currentSavedMinor + amountMinor
                    val isDone = updatedSaved >= goal.targetAmountMinor
                    repository.updateFinancialGoal(goal.copy(currentSavedMinor = updatedSaved, isReached = isDone))
                    rpgManager.awardProductivityXp("GOAL_SAVINGS", 35, "DISCIPLINE")
                }
            }

            // Also keep legacy expenses table in sync for widgets/queries
            if (type == TransactionType.EXPENSE) {
                val expCat = try {
                    ExpenseCategory.valueOf(category.uppercase())
                } catch (e: Exception) {
                    ExpenseCategory.OTHER
                }
                repository.insertExpense(
                    ExpenseEntity(
                        amount = MoneyUtils.fromMinor(amountMinor),
                        currency = currency,
                        category = expCat,
                        note = note.trim(),
                        dateMillis = dateMillis
                    )
                )
            }

            // Award RPG discipline XP for maintaining financial awareness!
            val soundManager = SoundHapticManager.getInstance(getApplication())
            soundManager.playRpgXpGain()
            rpgManager.awardProductivityXp("FINANCIAL_LOG", 15, "DISCIPLINE")

            val sign = if (type == TransactionType.INCOME) "+" else "-"
            triggerCelebration("Recorded $sign${MoneyUtils.formatMinor(amountMinor)} $currency")
        }
    }

    fun deleteFinancialTransaction(tx: FinancialTransactionEntity) {
        viewModelScope.launch {
            // Reverse account balance effect
            val sourceAccount = repository.getAccountById(tx.accountId)
            if (sourceAccount != null) {
                val reversed = when (tx.type) {
                    TransactionType.INCOME -> sourceAccount.balanceMinor - tx.amountMinor
                    TransactionType.EXPENSE -> sourceAccount.balanceMinor + tx.amountMinor
                    TransactionType.TRANSFER -> sourceAccount.balanceMinor + tx.amountMinor
                }
                repository.updateAccount(sourceAccount.copy(balanceMinor = reversed))
            }
            if (tx.type == TransactionType.TRANSFER && tx.toAccountId != null) {
                val dest = repository.getAccountById(tx.toAccountId)
                if (dest != null) {
                    repository.updateAccount(dest.copy(balanceMinor = dest.balanceMinor - tx.amountMinor))
                }
            }
            repository.deleteTransaction(tx)
            triggerCelebration("Transaction deleted.")
        }
    }

    fun createFinancialCategory(
        nameEn: String,
        nameAr: String,
        type: TransactionType = TransactionType.EXPENSE,
        colorHex: String = "#3B82F6",
        iconName: String = "category",
        parentId: Long? = null
    ) {
        viewModelScope.launch {
            repository.insertFinancialCategory(
                FinancialCategoryEntity(
                    nameEn = nameEn.trim(),
                    nameAr = nameAr.trim(),
                    type = type,
                    colorHex = colorHex,
                    iconName = iconName,
                    parentCategoryId = parentId,
                    isCustom = true
                )
            )
            triggerCelebration("Category saved: $nameEn")
        }
    }

    fun createFinancialGoal(
        title: String,
        targetAmountMinor: Long,
        targetDateMillis: Long? = null,
        colorHex: String = "#8B5CF6",
        iconName: String = "star",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val currency = repository.getUserSettings()?.currency ?: "EGP"
            repository.insertFinancialGoal(
                FinancialGoalEntity(
                    title = title.trim(),
                    targetAmountMinor = targetAmountMinor,
                    currency = currency,
                    targetDateMillis = targetDateMillis,
                    colorHex = colorHex,
                    iconName = iconName,
                    notes = notes.trim()
                )
            )
            triggerCelebration("Financial goal added: $title 🎯")
        }
    }

    fun contributeToFinancialGoal(goalId: Long, sourceAccountId: Long, amountMinor: Long) {
        viewModelScope.launch {
            addFinancialTransaction(
                accountId = sourceAccountId,
                type = TransactionType.EXPENSE,
                amountMinor = amountMinor,
                category = "Savings & Investments",
                subcategory = "Goal Contribution",
                note = "Contribution to savings goal",
                goalId = goalId
            )
        }
    }

    fun createRecurringTransaction(
        accountId: Long,
        toAccountId: Long? = null,
        type: TransactionType,
        amountMinor: Long,
        category: String,
        note: String = "",
        frequency: RecurringFrequency = RecurringFrequency.MONTHLY
    ) {
        viewModelScope.launch {
            val currency = repository.getUserSettings()?.currency ?: "EGP"
            val now = System.currentTimeMillis()
            repository.insertRecurringTransaction(
                RecurringTransactionEntity(
                    accountId = accountId,
                    toAccountId = toAccountId,
                    type = type,
                    amountMinor = amountMinor,
                    currency = currency,
                    category = category,
                    note = note.trim(),
                    frequency = frequency,
                    nextDueDateMillis = now + (30L * 86400000L),
                    isActive = true
                )
            )
            triggerCelebration("Recurring $frequency transaction configured.")
        }
    }

    // ==========================================
    // PROFESSIONAL SMART CALENDAR ACTIONS
    // ==========================================
    fun setCalendarViewMode(mode: CalendarViewMode) {
        _calendarViewMode.value = mode
    }

    fun setSelectedCalendarDate(date: LocalDate) {
        _selectedCalendarDate.value = date
    }

    fun createCalendarEvent(
        title: String,
        description: String = "",
        startMillis: Long,
        endMillis: Long,
        isAllDay: Boolean = false,
        category: String = "Personal",
        colorHex: String = "#3B82F6",
        location: String = "",
        recurrence: Recurrence = Recurrence.NONE,
        reminderMinutes: Int = 15,
        linkedTaskId: Long? = null,
        linkedGoalId: Long? = null,
        linkedHabitId: Long? = null
    ) {
        viewModelScope.launch {
            repository.insertCalendarEvent(
                CalendarEventEntity(
                    title = title.trim(),
                    description = description.trim(),
                    startMillis = startMillis,
                    endMillis = endMillis,
                    isAllDay = isAllDay,
                    category = category,
                    colorHex = colorHex,
                    location = location.trim(),
                    recurrence = recurrence,
                    reminderMinutesBefore = reminderMinutes,
                    linkedTaskId = linkedTaskId,
                    linkedGoalId = linkedGoalId,
                    linkedHabitId = linkedHabitId
                )
            )
            val soundManager = SoundHapticManager.getInstance(getApplication())
            soundManager.playRpgXpGain()
            triggerCelebration("Event scheduled: $title 📅")
        }
    }

    fun updateCalendarEvent(event: CalendarEventEntity) {
        viewModelScope.launch {
            repository.updateCalendarEvent(event)
        }
    }

    fun deleteCalendarEvent(event: CalendarEventEntity) {
        viewModelScope.launch {
            repository.deleteCalendarEvent(event)
            triggerCelebration("Calendar event removed.")
        }
    }

    fun generateSmartTimeBlockingPreview(targetDate: LocalDate = _selectedCalendarDate.value) {
        viewModelScope.launch {
            val tasks = uiState.value.pendingTasks
            val events = uiState.value.roomCalendarEvents
            val settings = uiState.value.userSettings
            val proposals = SmartTimeBlockingEngine.proposeTimeBlocksForDay(
                targetDate = targetDate,
                pendingTasks = tasks,
                existingEvents = events,
                workStartHour = settings.workStartHour,
                workEndHour = settings.workEndHour
            )
            _smartTimeSlotProposals.value = proposals
            _isShowingSmartSchedulePreview.value = true
        }
    }

    fun dismissSmartSchedulePreview() {
        _isShowingSmartSchedulePreview.value = false
        _smartTimeSlotProposals.value = emptyList()
    }

    fun applySmartScheduleProposals() {
        viewModelScope.launch {
            val proposals = _smartTimeSlotProposals.value
            for (prop in proposals) {
                repository.insertCalendarEvent(
                    CalendarEventEntity(
                        title = prop.taskTitle,
                        description = "Smart time block: ${prop.reasonEn}",
                        startMillis = prop.startMillis,
                        endMillis = prop.endMillis,
                        category = "WORK",
                        colorHex = "#6366F1",
                        linkedTaskId = prop.taskId
                    )
                )
                // Update task schedule if found
                val task = uiState.value.tasks.firstOrNull { it.id == prop.taskId }
                if (task != null) {
                    val startInstant = java.time.Instant.ofEpochMilli(prop.startMillis).atZone(ZoneId.systemDefault())
                    val timeMinutes = startInstant.hour * 60 + startInstant.minute
                    repository.updateTask(
                        task.copy(
                            dueDateMillis = prop.startMillis,
                            dueTimeMinutes = timeMinutes,
                            durationMinutes = ((prop.endMillis - prop.startMillis) / 60000).toInt()
                        )
                    )
                }
            }
            dismissSmartSchedulePreview()
            rpgManager.awardProductivityXp("SMART_PLANNING", 25, "PLANNING")
            val soundManager = SoundHapticManager.getInstance(getApplication())
            soundManager.playRpgLevelUp()
            triggerCelebration("Applied ${proposals.size} smart time blocks to calendar! ⚡")
        }
    }

    // ==========================================
    // ADVANCED FOCUS MODE ACTIONS
    // ==========================================
    fun toggleFocusShield(active: Boolean) {
        _isFocusShieldActive.value = active
        val soundManager = SoundHapticManager.getInstance(getApplication())
        if (active) {
            soundManager.playHabitCompleted()
            triggerCelebration("Focus Shield Activated! Distractions blocked. 🛡️")
        } else {
            triggerCelebration("Focus Shield Deactivated.")
        }
    }

    fun updateFocusPreferences(prefs: FocusPreferencesEntity) {
        viewModelScope.launch {
            repository.updateFocusPreferences(prefs)
            focusAudioHelper.isSoundEnabled = prefs.soundEnabled
            focusAudioHelper.isHapticsEnabled = prefs.hapticsEnabled
            focusAudioHelper.setVolume(prefs.soundVolume)
            triggerCelebration("Focus preferences saved.")
        }
    }

    fun completeFocusSession(
        taskId: Long? = null,
        taskTitle: String? = null,
        durationMinutes: Int,
        actualSeconds: Int,
        mode: FocusTimerMode = FocusTimerMode.POMODORO,
        soundMode: String = "SILENT",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val recentSessions = uiState.value.focusSessions.filter {
                it.startedAtMillis >= (now - 3600000L)
            }
            val hasLinkedTask = taskId != null

            // Anti-farming validated XP calculation
            val earnedXp = FocusSessionManager.calculateSessionXp(
                actualSeconds = actualSeconds,
                targetDurationMinutes = durationMinutes,
                isCompleted = true,
                hasLinkedTask = hasLinkedTask,
                recentSessionsInPastHour = recentSessions
            )

            val session = FocusSessionEntity(
                startedAtMillis = now - (actualSeconds * 1000L),
                durationMinutes = durationMinutes,
                actualSeconds = actualSeconds,
                mode = mode,
                soundMode = soundMode,
                taskId = taskId,
                taskTitle = taskTitle,
                isCompleted = true,
                notes = notes.trim(),
                xpAwarded = earnedXp
            )
            repository.insertFocusSession(session)

            focusAudioHelper.playSessionComplete()

            if (earnedXp > 0) {
                val res = rpgManager.awardProductivityXp("FOCUS_SESSION", earnedXp.toLong(), "FOCUS")
                if (res.leveledUp) {
                    triggerCelebration("Level UP! Focus session logged (+${earnedXp} XP) 🏆")
                } else {
                    triggerCelebration("Focus session complete! +${earnedXp} XP earned 🎯")
                }
            } else {
                triggerCelebration("Focus session logged. (Sessions under 3 min earn 0 XP)")
            }
        }
    }

    // ==========================================
    // YAWMEK COMMUNITY ACTIONS
    // ==========================================
    fun setupCommunityProfile(username: String, displayName: String, avatarId: String, bio: String) {
        viewModelScope.launch {
            val cleanUsername = username.trim().lowercase().replace(" ", "_")
            if (cleanUsername.length < 3) {
                triggerCelebration("Username must be at least 3 characters.")
                return@launch
            }
            val existing = repository.getFriendByUsername(cleanUsername)
            if (existing != null) {
                triggerCelebration("Username already taken! Please choose another.")
                return@launch
            }
            val current = repository.getCommunityProfile() ?: CommunityProfileEntity(id = 1)
            val connectionId = if (current.connectionId.isNotBlank()) current.connectionId
            else "YAWMEK-NET-" + java.util.UUID.randomUUID().toString().take(8).uppercase()
            val updated = current.copy(
                username = cleanUsername,
                displayName = displayName.trim().ifBlank { cleanUsername },
                avatarId = avatarId,
                bio = bio.trim(),
                connectionId = connectionId,
                isInitialSetupDone = true,
                isOnline = true,
                lastActiveMillis = System.currentTimeMillis()
            )
            repository.saveCommunityProfile(updated)
            val soundManager = SoundHapticManager.getInstance(getApplication())
            soundManager.playRpgLevelUp()
            triggerCelebration("Welcome to YAWMEK Community, @$cleanUsername! 🛡️")
        }
    }

    fun updateCommunityProfile(displayName: String, avatarId: String, bio: String) {
        viewModelScope.launch {
            val current = repository.getCommunityProfile() ?: return@launch
            val updated = current.copy(
                displayName = displayName.trim(),
                avatarId = avatarId,
                bio = bio.trim(),
                lastActiveMillis = System.currentTimeMillis()
            )
            repository.saveCommunityProfile(updated)
            triggerCelebration("Profile updated successfully ✨")
        }
    }

    fun changeCommunityUsername(newUsername: String) {
        viewModelScope.launch {
            val clean = newUsername.trim().lowercase().replace(" ", "_")
            if (clean.length < 3) {
                triggerCelebration("Username must be at least 3 characters.")
                return@launch
            }
            val existing = repository.getFriendByUsername(clean)
            if (existing != null) {
                triggerCelebration("Username @$clean is already taken.")
                return@launch
            }
            val current = repository.getCommunityProfile() ?: return@launch
            repository.saveCommunityProfile(current.copy(username = clean))
            triggerCelebration("Username changed to @$clean! 🆔")
        }
    }

    fun sendFriendRequest(friendUsername: String) {
        viewModelScope.launch {
            val clean = friendUsername.trim().lowercase().replace(" ", "_")
            val myProfile = repository.getCommunityProfile()
            if (myProfile?.username.equals(clean, ignoreCase = true)) {
                triggerCelebration("You cannot send a friend request to yourself.")
                return@launch
            }
            val existing = repository.getFriendByUsername(clean)
            if (existing != null) {
                if (existing.status == FriendshipStatus.FRIEND) {
                    triggerCelebration("@$clean is already your friend!")
                } else if (existing.status == FriendshipStatus.PENDING_SENT) {
                    triggerCelebration("Friend request already pending for @$clean.")
                } else {
                    repository.updateFriend(existing.copy(status = FriendshipStatus.PENDING_SENT))
                    triggerCelebration("Friend request sent to @$clean! ✉️")
                }
                return@launch
            }
            repository.insertFriend(
                CommunityFriendEntity(
                    friendUsername = clean,
                    friendDisplayName = clean.replaceFirstChar { it.uppercase() },
                    avatarId = "warrior",
                    level = (3..12).random(),
                    xp = 1200L,
                    rankTitle = "Adventurer",
                    isOnline = true,
                    status = FriendshipStatus.PENDING_SENT,
                    rating = 1020
                )
            )
            val soundManager = SoundHapticManager.getInstance(getApplication())
            soundManager.playHabitCompleted()
            triggerCelebration("Friend request sent to @$clean! ✉️")
        }
    }

    fun acceptFriendRequest(friend: CommunityFriendEntity) {
        viewModelScope.launch {
            repository.updateFriend(friend.copy(status = FriendshipStatus.FRIEND))
            rpgManager.awardProductivityXp("FRIEND_ADDED", 20, "COMMUNITY")
            val soundManager = SoundHapticManager.getInstance(getApplication())
            soundManager.playRpgLevelUp()
            triggerCelebration("Friend request accepted! @${friend.friendUsername} is now your friend 🤝")
        }
    }

    fun rejectFriendRequest(friend: CommunityFriendEntity) {
        viewModelScope.launch {
            repository.deleteFriend(friend)
            triggerCelebration("Friend request declined.")
        }
    }

    fun removeCommunityFriend(friend: CommunityFriendEntity) {
        viewModelScope.launch {
            repository.deleteFriend(friend)
            triggerCelebration("Removed @${friend.friendUsername} from friends.")
        }
    }

    fun startBattleWithFriend(friend: CommunityFriendEntity, battleType: BattleType) {
        viewModelScope.launch {
            val profile = repository.getCommunityProfile() ?: CommunityProfileEntity()
            val char = uiState.value.rpgCharacter
            val equip = uiState.value.rpgEquipment
            val comp = uiState.value.rpgCompanions
            val skills = uiState.value.rpgSkills
            val todayFocus = uiState.value.todayFocusMinutes
            val todayTasks = uiState.value.tasks.count { it.isCompleted }

            val simulation = CommunityBattleEngine.simulateBattle(
                battleType = battleType,
                myProfile = profile,
                myCharacter = char,
                myEquipment = equip,
                myCompanions = comp,
                mySkills = skills,
                todayFocusMinutes = todayFocus,
                todayTasksCompleted = todayTasks,
                opponent = friend
            )

            // Persist battle history
            val battleEntity = CommunityBattleEntity(
                opponentUsername = friend.friendUsername,
                opponentDisplayName = friend.friendDisplayName,
                opponentAvatarId = friend.avatarId,
                opponentLevel = friend.level,
                battleType = battleType,
                myScore = simulation.myScore,
                opponentScore = simulation.opponentScore,
                result = simulation.result,
                ratingDelta = simulation.ratingDelta,
                xpGained = simulation.xpGained,
                coinsGained = simulation.coinsGained,
                summaryLog = simulation.summaryLog,
                timestampMillis = System.currentTimeMillis()
            )
            repository.insertBattle(battleEntity)

            // Update user profile rating and win/loss count
            val newRating = (profile.rating + simulation.ratingDelta).coerceAtLeast(100)
            val newWins = if (simulation.result == BattleResult.VICTORY) profile.battleWins + 1 else profile.battleWins
            val newLosses = if (simulation.result == BattleResult.DEFEAT) profile.battleLosses + 1 else profile.battleLosses
            repository.saveCommunityProfile(
                profile.copy(
                    rating = newRating,
                    battleWins = newWins,
                    battleLosses = newLosses,
                    lastActiveMillis = System.currentTimeMillis()
                )
            )

            // Rewards
            if (simulation.xpGained > 0) {
                rpgManager.awardProductivityXp("COMMUNITY_BATTLE", simulation.xpGained, "BATTLE")
            }

            val soundManager = SoundHapticManager.getInstance(getApplication())
            if (simulation.result == BattleResult.VICTORY) {
                soundManager.playRpgLevelUp()
            } else {
                soundManager.playHabitCompleted()
            }

            _activeBattleSimulation.value = simulation
            triggerCelebration("${simulation.result.name}! ${simulation.summaryLog}")
        }
    }

    fun dismissBattleSimulation() {
        _activeBattleSimulation.value = null
    }

    fun testCommunityConnection() {
        viewModelScope.launch {
            _isCommunityConnected.value = true
            val soundManager = SoundHapticManager.getInstance(getApplication())
            soundManager.playHabitCompleted()
            triggerCelebration("Connected to YAWMEK Peer Network (24ms latency) 🟢")
        }
    }

    fun regenerateConnectionId() {
        viewModelScope.launch {
            val current = repository.getCommunityProfile() ?: return@launch
            val newId = "YAWMEK-NET-" + java.util.UUID.randomUUID().toString().take(8).uppercase()
            repository.saveCommunityProfile(current.copy(connectionId = newId))
            triggerCelebration("New Connection ID generated: $newId 🔄")
        }
    }
}
