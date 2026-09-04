package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class YawmekRepository(private val db: AppDatabase) {

    // Tasks
    val allTasks: Flow<List<TaskEntity>> = db.taskDao().getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = db.taskDao().getPendingTasks()

    suspend fun getTaskById(taskId: Long): TaskEntity? = db.taskDao().getTaskById(taskId)
    suspend fun insertTask(task: TaskEntity): Long = db.taskDao().insertTask(task)
    suspend fun updateTask(task: TaskEntity) = db.taskDao().updateTask(task)
    suspend fun deleteTask(task: TaskEntity) {
        db.taskDao().deleteSubtasksByTaskId(task.id)
        db.taskDao().deleteTask(task)
    }
    suspend fun deleteTaskById(taskId: Long) {
        db.taskDao().deleteSubtasksByTaskId(taskId)
        db.taskDao().deleteTaskById(taskId)
    }
    suspend fun toggleTaskCompletion(task: TaskEntity) {
        val updated = task.copy(
            isCompleted = !task.isCompleted,
            completedAtMillis = if (!task.isCompleted) System.currentTimeMillis() else null
        )
        db.taskDao().updateTask(updated)
    }

    // Subtasks
    fun getSubtasksForTask(taskId: Long): Flow<List<SubtaskEntity>> = db.taskDao().getSubtasksForTask(taskId)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long = db.taskDao().insertSubtask(subtask)
    suspend fun updateSubtask(subtask: SubtaskEntity) = db.taskDao().updateSubtask(subtask)
    suspend fun deleteSubtask(subtask: SubtaskEntity) = db.taskDao().deleteSubtask(subtask)
    suspend fun toggleSubtask(subtask: SubtaskEntity) = db.taskDao().updateSubtask(subtask.copy(isCompleted = !subtask.isCompleted))

    // Expenses
    val allExpenses: Flow<List<ExpenseEntity>> = db.expenseDao().getAllExpenses()
    fun getExpensesInRange(startMillis: Long, endMillis: Long): Flow<List<ExpenseEntity>> =
        db.expenseDao().getExpensesInRange(startMillis, endMillis)
    suspend fun insertExpense(expense: ExpenseEntity): Long = db.expenseDao().insertExpense(expense)
    suspend fun updateExpense(expense: ExpenseEntity) = db.expenseDao().updateExpense(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = db.expenseDao().deleteExpense(expense)
    suspend fun deleteExpenseById(expenseId: Long) = db.expenseDao().deleteExpenseById(expenseId)

    // Budgets
    val allBudgets: Flow<List<BudgetEntity>> = db.budgetDao().getAllBudgets()
    fun getBudgetFlow(period: BudgetPeriod): Flow<BudgetEntity?> = db.budgetDao().getBudgetFlowByPeriod(period)
    suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long = db.budgetDao().insertOrUpdateBudget(budget)

    // Habits
    val allHabits: Flow<List<HabitEntity>> = db.habitDao().getAllActiveHabits()
    fun getHabitLogsSince(startEpochDay: Long): Flow<List<HabitLogEntity>> = db.habitDao().getHabitLogsSince(startEpochDay)
    suspend fun insertHabit(habit: HabitEntity): Long = db.habitDao().insertHabit(habit)
    suspend fun updateHabit(habit: HabitEntity) = db.habitDao().updateHabit(habit)
    suspend fun deleteHabit(habit: HabitEntity) {
        db.habitDao().deleteLogsForHabit(habit.id)
        db.habitDao().deleteHabit(habit)
    }
    suspend fun toggleHabitForDate(habitId: Long, dateEpochDay: Long) {
        val existing = db.habitDao().getLog(habitId, dateEpochDay)
        if (existing != null) {
            db.habitDao().deleteHabitLog(habitId, dateEpochDay)
        } else {
            db.habitDao().insertHabitLog(
                HabitLogEntity(
                    habitId = habitId,
                    dateEpochDay = dateEpochDay,
                    isCompleted = true,
                    timestampMillis = System.currentTimeMillis()
                )
            )
        }
    }

    // Goals
    val allGoals: Flow<List<GoalEntity>> = db.goalDao().getAllGoals()
    val allMilestones: Flow<List<MilestoneEntity>> = db.goalDao().getAllMilestones()
    fun getMilestonesForGoal(goalId: Long): Flow<List<MilestoneEntity>> = db.goalDao().getMilestonesForGoal(goalId)
    suspend fun insertGoal(goal: GoalEntity): Long = db.goalDao().insertGoal(goal)
    suspend fun updateGoal(goal: GoalEntity) = db.goalDao().updateGoal(goal)
    suspend fun deleteGoal(goal: GoalEntity) {
        db.goalDao().deleteMilestonesForGoal(goal.id)
        db.goalDao().deleteGoal(goal)
    }
    suspend fun insertMilestone(milestone: MilestoneEntity): Long = db.goalDao().insertMilestone(milestone)
    suspend fun updateMilestone(milestone: MilestoneEntity) = db.goalDao().updateMilestone(milestone)
    suspend fun deleteMilestone(milestone: MilestoneEntity) = db.goalDao().deleteMilestone(milestone)
    suspend fun toggleMilestone(milestone: MilestoneEntity) = db.goalDao().updateMilestone(milestone.copy(isCompleted = !milestone.isCompleted))

    // Notes
    val allNotes: Flow<List<NoteEntity>> = db.noteDao().getAllNotes()
    suspend fun insertNote(note: NoteEntity): Long = db.noteDao().insertNote(note)
    suspend fun updateNote(note: NoteEntity) = db.noteDao().updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = db.noteDao().deleteNote(note)
    suspend fun deleteNoteById(noteId: Long) = db.noteDao().deleteNoteById(noteId)

    // User Settings
    val userSettings: Flow<UserSettingsEntity?> = db.userSettingsDao().getUserSettingsFlow()
    suspend fun getUserSettings(): UserSettingsEntity? = db.userSettingsDao().getUserSettings()
    suspend fun updateUserSettings(settings: UserSettingsEntity) = db.userSettingsDao().insertOrUpdateSettings(settings)

    // User Account
    val activeAccount: Flow<UserAccountEntity?> = db.userAccountDao().getActiveAccountFlow()
    suspend fun getActiveAccount(): UserAccountEntity? = db.userAccountDao().getActiveAccount()
    suspend fun insertOrUpdateAccount(account: UserAccountEntity) = db.userAccountDao().insertOrUpdateAccount(account)
    suspend fun deleteAccountById(accountId: String) = db.userAccountDao().deleteAccountById(accountId)

    // Focus Sessions & Preferences
    val allFocusSessions: Flow<List<FocusSessionEntity>> = db.focusSessionDao().getAllSessionsFlow()
    fun getFocusSessionsSince(startMillis: Long): Flow<List<FocusSessionEntity>> = db.focusSessionDao().getSessionsSince(startMillis)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long = db.focusSessionDao().insertSession(session)
    val focusPreferences: Flow<FocusPreferencesEntity?> = db.focusPreferencesDao().getPreferencesFlow()
    suspend fun getFocusPreferences(): FocusPreferencesEntity? = db.focusPreferencesDao().getPreferences()
    suspend fun updateFocusPreferences(prefs: FocusPreferencesEntity) = db.focusPreferencesDao().insertOrUpdatePreferences(prefs)

    // Advanced Money System
    val allActiveAccounts: Flow<List<WalletAccountEntity>> = db.walletAccountDao().getAllActiveAccounts()
    suspend fun getAccountById(id: Long) = db.walletAccountDao().getAccountById(id)
    suspend fun getDefaultAccount() = db.walletAccountDao().getDefaultAccount()
    suspend fun insertAccount(account: WalletAccountEntity): Long = db.walletAccountDao().insertAccount(account)
    suspend fun updateAccount(account: WalletAccountEntity) = db.walletAccountDao().updateAccount(account)
    suspend fun deleteAccount(account: WalletAccountEntity) = db.walletAccountDao().deleteAccount(account)
    suspend fun updateAccountBalanceDelta(accountId: Long, deltaMinor: Long) = db.walletAccountDao().updateBalanceDelta(accountId, deltaMinor)
    suspend fun clearDefaultAccountFlags() = db.walletAccountDao().clearDefaultFlags()

    val allTransactions: Flow<List<FinancialTransactionEntity>> = db.financialTransactionDao().getAllTransactions()
    fun getTransactionsInRange(startMillis: Long, endMillis: Long) = db.financialTransactionDao().getTransactionsInRange(startMillis, endMillis)
    fun getTransactionsForAccount(accountId: Long) = db.financialTransactionDao().getTransactionsForAccount(accountId)
    fun getTransactionsForGoal(goalId: Long) = db.financialTransactionDao().getTransactionsForGoal(goalId)
    suspend fun insertTransaction(tx: FinancialTransactionEntity): Long = db.financialTransactionDao().insertTransaction(tx)
    suspend fun updateTransaction(tx: FinancialTransactionEntity) = db.financialTransactionDao().updateTransaction(tx)
    suspend fun deleteTransaction(tx: FinancialTransactionEntity) = db.financialTransactionDao().deleteTransaction(tx)

    val allFinancialCategories: Flow<List<FinancialCategoryEntity>> = db.financialCategoryDao().getAllCategories()
    suspend fun insertFinancialCategory(category: FinancialCategoryEntity): Long = db.financialCategoryDao().insertCategory(category)
    suspend fun insertFinancialCategories(categories: List<FinancialCategoryEntity>) = db.financialCategoryDao().insertCategories(categories)
    suspend fun deleteFinancialCategory(category: FinancialCategoryEntity) = db.financialCategoryDao().deleteCategory(category)

    val activeRecurringTransactions: Flow<List<RecurringTransactionEntity>> = db.recurringTransactionDao().getActiveRecurring()
    suspend fun insertRecurringTransaction(recurring: RecurringTransactionEntity): Long = db.recurringTransactionDao().insertRecurring(recurring)
    suspend fun updateRecurringTransaction(recurring: RecurringTransactionEntity) = db.recurringTransactionDao().updateRecurring(recurring)
    suspend fun deleteRecurringTransaction(recurring: RecurringTransactionEntity) = db.recurringTransactionDao().deleteRecurring(recurring)

    val allFinancialGoals: Flow<List<FinancialGoalEntity>> = db.financialGoalDao().getAllGoals()
    suspend fun getFinancialGoalById(id: Long) = db.financialGoalDao().getGoalById(id)
    suspend fun insertFinancialGoal(goal: FinancialGoalEntity): Long = db.financialGoalDao().insertGoal(goal)
    suspend fun updateFinancialGoal(goal: FinancialGoalEntity) = db.financialGoalDao().updateGoal(goal)
    suspend fun deleteFinancialGoal(goal: FinancialGoalEntity) = db.financialGoalDao().deleteGoal(goal)
    suspend fun contributeToFinancialGoal(goalId: Long, deltaMinor: Long) = db.financialGoalDao().contributeToGoal(goalId, deltaMinor)

    // Professional Calendar Events
    val allCalendarEvents: Flow<List<CalendarEventEntity>> = db.calendarEventDao().getAllEvents()
    fun getCalendarEventsForDay(startOfDay: Long, endOfDay: Long) = db.calendarEventDao().getEventsForDay(startOfDay, endOfDay)
    suspend fun getCalendarEventsInRange(startRange: Long, endRange: Long) = db.calendarEventDao().getEventsInRange(startRange, endRange)
    suspend fun getCalendarEventById(id: Long) = db.calendarEventDao().getEventById(id)
    suspend fun insertCalendarEvent(event: CalendarEventEntity): Long = db.calendarEventDao().insertEvent(event)
    suspend fun updateCalendarEvent(event: CalendarEventEntity) = db.calendarEventDao().updateEvent(event)
    suspend fun deleteCalendarEvent(event: CalendarEventEntity) = db.calendarEventDao().deleteEvent(event)
    suspend fun deleteCalendarEventById(id: Long) = db.calendarEventDao().deleteEventById(id)
    suspend fun deleteCalendarEventsByTaskId(taskId: Long) = db.calendarEventDao().deleteEventsByTaskId(taskId)

    // Notification Preferences
    val notificationPreferences: Flow<NotificationPreferencesEntity?> = db.notificationPreferencesDao().getPreferencesFlow()
    suspend fun getNotificationPreferences(): NotificationPreferencesEntity? = db.notificationPreferencesDao().getPreferences()
    suspend fun updateNotificationPreferences(prefs: NotificationPreferencesEntity) = db.notificationPreferencesDao().insertOrUpdatePreferences(prefs)

    // Sync Metadata
    val syncMetadata: Flow<List<SyncMetadataEntity>> = db.syncMetadataDao().getAllMetadataFlow()

    // RPG Progression System
    val rpgCharacter: Flow<CharacterRpgEntity?> = db.rpgDao().getCharacterFlow()
    val rpgEquipment: Flow<List<EquipmentItemEntity>> = db.rpgDao().getAllEquipmentItems()
    val rpgCompanions: Flow<List<CompanionEntity>> = db.rpgDao().getAllCompanions()
    val rpgSkills: Flow<List<SkillNodeEntity>> = db.rpgDao().getAllSkills()
    val rpgQuests: Flow<List<RpgQuestEntity>> = db.rpgDao().getAllQuests()
    val rpgBosses: Flow<List<BossChallengeEntity>> = db.rpgDao().getAllBosses()

    suspend fun getRpgCharacter(): CharacterRpgEntity? = db.rpgDao().getCharacter()
    suspend fun saveRpgCharacter(character: CharacterRpgEntity) = db.rpgDao().insertOrUpdateCharacter(character)
    suspend fun saveEquipmentItems(items: List<EquipmentItemEntity>) = db.rpgDao().insertEquipmentItems(items)
    suspend fun updateEquipmentItem(item: EquipmentItemEntity) = db.rpgDao().updateEquipmentItem(item)
    suspend fun saveCompanions(companions: List<CompanionEntity>) = db.rpgDao().insertCompanions(companions)
    suspend fun updateCompanion(companion: CompanionEntity) = db.rpgDao().updateCompanion(companion)
    suspend fun saveSkills(skills: List<SkillNodeEntity>) = db.rpgDao().insertSkills(skills)
    suspend fun updateSkill(skill: SkillNodeEntity) = db.rpgDao().updateSkill(skill)
    suspend fun saveQuests(quests: List<RpgQuestEntity>) = db.rpgDao().insertQuests(quests)
    suspend fun updateQuest(quest: RpgQuestEntity) = db.rpgDao().updateQuest(quest)
    suspend fun insertBoss(boss: BossChallengeEntity): Long = db.rpgDao().insertBoss(boss)
    suspend fun updateBoss(boss: BossChallengeEntity) = db.rpgDao().updateBoss(boss)
    suspend fun deleteBoss(boss: BossChallengeEntity) = db.rpgDao().deleteBoss(boss)

    // Community System
    val communityProfile: Flow<CommunityProfileEntity?> = db.communityDao().getProfileFlow()
    val allCommunityFriends: Flow<List<CommunityFriendEntity>> = db.communityDao().getAllFriendsFlow()
    val allCommunityBattles: Flow<List<CommunityBattleEntity>> = db.communityDao().getAllBattlesFlow()

    suspend fun getCommunityProfile(): CommunityProfileEntity? = db.communityDao().getProfile()
    suspend fun saveCommunityProfile(profile: CommunityProfileEntity) = db.communityDao().insertOrUpdateProfile(profile)
    suspend fun getFriendByUsername(username: String): CommunityFriendEntity? = db.communityDao().getFriendByUsername(username)
    suspend fun insertFriend(friend: CommunityFriendEntity): Long = db.communityDao().insertFriend(friend)
    suspend fun updateFriend(friend: CommunityFriendEntity) = db.communityDao().updateFriend(friend)
    suspend fun deleteFriend(friend: CommunityFriendEntity) = db.communityDao().deleteFriend(friend)
    suspend fun insertBattle(battle: CommunityBattleEntity): Long = db.communityDao().insertBattle(battle)
    suspend fun getRecentBattles(limit: Int = 20): List<CommunityBattleEntity> = db.communityDao().getRecentBattles(limit)

    // Clear all / Reset workspace
    suspend fun clearAllData() {
        db.taskDao().clearAllTasks()
        db.expenseDao().clearAllExpenses()
        db.budgetDao().clearAllBudgets()
        db.habitDao().clearAllHabits()
        db.habitDao().clearAllHabitLogs()
        db.goalDao().clearAllGoals()
        db.noteDao().clearAllNotes()
        db.focusSessionDao().clearAllSessions()
    }
}
