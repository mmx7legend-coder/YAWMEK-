package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, dueDateMillis ASC, createdAtMillis DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY priority DESC, dueDateMillis ASC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    // Subtasks
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId")
    fun getSubtasksForTask(taskId: Long): Flow<List<SubtaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long

    @Update
    suspend fun updateSubtask(subtask: SubtaskEntity)

    @Delete
    suspend fun deleteSubtask(subtask: SubtaskEntity)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasksByTaskId(taskId: Long)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY dateMillis DESC")
    fun getExpensesInRange(startMillis: Long, endMillis: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Long)

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE period = :period LIMIT 1")
    suspend fun getBudgetByPeriod(period: BudgetPeriod): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE period = :period LIMIT 1")
    fun getBudgetFlowByPeriod(period: BudgetPeriod): Flow<BudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long

    @Query("DELETE FROM budgets")
    suspend fun clearAllBudgets()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAtMillis ASC")
    fun getAllActiveHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: Long)

    @Query("DELETE FROM habits")
    suspend fun clearAllHabits()

    // Habit Logs
    @Query("SELECT * FROM habit_logs WHERE dateEpochDay >= :startEpochDay ORDER BY dateEpochDay ASC")
    fun getHabitLogsSince(startEpochDay: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getLog(habitId: Long, dateEpochDay: Long): HabitLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLogEntity): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay")
    suspend fun deleteHabitLog(habitId: Long, dateEpochDay: Long)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)

    @Query("DELETE FROM habit_logs")
    suspend fun clearAllHabitLogs()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY isCompleted ASC, createdAtMillis DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: Long)

    @Query("DELETE FROM goals")
    suspend fun clearAllGoals()

    // Milestones
    @Query("SELECT * FROM milestones WHERE goalId = :goalId ORDER BY orderIndex ASC")
    fun getMilestonesForGoal(goalId: Long): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones")
    fun getAllMilestones(): Flow<List<MilestoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity): Long

    @Update
    suspend fun updateMilestone(milestone: MilestoneEntity)

    @Delete
    suspend fun deleteMilestone(milestone: MilestoneEntity)

    @Query("DELETE FROM milestones WHERE goalId = :goalId")
    suspend fun deleteMilestonesForGoal(goalId: Long)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAtMillis DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Long)

    @Query("DELETE FROM notes")
    suspend fun clearAllNotes()
}

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettingsFlow(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettings(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: UserSettingsEntity)

    @Query("DELETE FROM user_settings")
    suspend fun clearSettings()
}
