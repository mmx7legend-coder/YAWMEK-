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

    // Focus Sessions
    val allFocusSessions: Flow<List<FocusSessionEntity>> = db.focusSessionDao().getAllSessionsFlow()
    fun getFocusSessionsSince(startMillis: Long): Flow<List<FocusSessionEntity>> = db.focusSessionDao().getSessionsSince(startMillis)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long = db.focusSessionDao().insertSession(session)

    // Notification Preferences
    val notificationPreferences: Flow<NotificationPreferencesEntity?> = db.notificationPreferencesDao().getPreferencesFlow()
    suspend fun getNotificationPreferences(): NotificationPreferencesEntity? = db.notificationPreferencesDao().getPreferences()
    suspend fun updateNotificationPreferences(prefs: NotificationPreferencesEntity) = db.notificationPreferencesDao().insertOrUpdatePreferences(prefs)

    // Sync Metadata
    val syncMetadata: Flow<List<SyncMetadataEntity>> = db.syncMetadataDao().getAllMetadataFlow()

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
