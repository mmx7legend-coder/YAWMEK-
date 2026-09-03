package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets ORDER BY isDefault DESC, createdAtMillis ASC")
    fun getAllWallets(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE id = :walletId")
    suspend fun getWalletById(walletId: Long): WalletEntity?

    @Query("SELECT * FROM wallets WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultWallet(): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity): Long

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Delete
    suspend fun deleteWallet(wallet: WalletEntity)

    @Query("DELETE FROM wallets WHERE id = :walletId")
    suspend fun deleteWalletById(walletId: Long)

    @Query("DELETE FROM wallets")
    suspend fun clearAllWallets()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE walletId = :walletId ORDER BY dateMillis DESC")
    fun getTransactionsForWallet(walletId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY dateMillis DESC")
    fun getTransactionsInRange(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE walletId = :walletId AND dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY dateMillis DESC")
    fun getWalletTransactionsInRange(walletId: Long, startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type AND dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY dateMillis DESC")
    fun getTransactionsByType(type: MoneyTransactionType, startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY isCompleted ASC, createdAtMillis DESC")
    fun getAllGoals(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: Long): SavingsGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoalEntity)

    @Query("DELETE FROM savings_goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: Long)

    @Query("DELETE FROM savings_goals")
    suspend fun clearAllGoals()

    // Goal Contributions
    @Query("SELECT * FROM goal_contributions WHERE goalId = :goalId ORDER BY dateMillis DESC")
    fun getContributionsForGoal(goalId: Long): Flow<List<GoalContributionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: GoalContributionEntity): Long

    @Delete
    suspend fun deleteContribution(contribution: GoalContributionEntity)

    @Query("DELETE FROM goal_contributions WHERE goalId = :goalId")
    suspend fun deleteContributionsForGoal(goalId: Long)
}

@Dao
interface FinancialSummaryDao {
    @Query("SELECT * FROM financial_summaries ORDER BY periodStartMillis DESC")
    fun getAllSummaries(): Flow<List<FinancialSummaryEntity>>

    @Query("SELECT * FROM financial_summaries WHERE walletId = :walletId ORDER BY periodStartMillis DESC LIMIT 1")
    suspend fun getLatestSummaryForWallet(walletId: Long): FinancialSummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: FinancialSummaryEntity): Long

    @Update
    suspend fun updateSummary(summary: FinancialSummaryEntity)

    @Query("DELETE FROM financial_summaries")
    suspend fun clearAllSummaries()
}

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers ORDER BY dateMillis DESC")
    fun getAllTransfers(): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfers WHERE fromWalletId = :walletId OR toWalletId = :walletId ORDER BY dateMillis DESC")
    fun getTransfersForWallet(walletId: Long): Flow<List<TransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity): Long

    @Delete
    suspend fun deleteTransfer(transfer: TransferEntity)

    @Query("DELETE FROM transfers")
    suspend fun clearAllTransfers()
}

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY startMillis ASC")
    fun getAllEvents(): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE startMillis >= :startMillis AND startMillis <= :endMillis ORDER BY startMillis ASC")
    fun getEventsInRange(startMillis: Long, endMillis: Long): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE startMillis >= :dayStartMillis AND startMillis < :dayEndMillis ORDER BY startMillis ASC")
    fun getEventsForDay(dayStartMillis: Long, dayEndMillis: Long): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE id = :eventId")
    suspend fun getEventById(eventId: Long): CalendarEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity): Long

    @Update
    suspend fun updateEvent(event: CalendarEventEntity)

    @Delete
    suspend fun deleteEvent(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: Long)

    @Query("DELETE FROM calendar_events")
    suspend fun clearAllEvents()
}

@Dao
interface FocusSessionV2Dao {
    @Query("SELECT * FROM focus_session_v2 ORDER BY startedAtMillis DESC")
    fun getAllSessions(): Flow<List<FocusSessionV2Entity>>

    @Query("SELECT * FROM focus_session_v2 WHERE startedAtMillis >= :startMillis ORDER BY startedAtMillis DESC")
    fun getSessionsSince(startMillis: Long): Flow<List<FocusSessionV2Entity>>

    @Query("SELECT * FROM focus_session_v2 WHERE startedAtMillis >= :dayStartMillis AND startedAtMillis < :dayEndMillis ORDER BY startedAtMillis DESC")
    fun getSessionsForDay(dayStartMillis: Long, dayEndMillis: Long): Flow<List<FocusSessionV2Entity>>

    @Query("SELECT * FROM focus_session_v2 WHERE isCompleted = 1 AND startedAtMillis >= :startMillis")
    fun getCompletedSessionsSince(startMillis: Long): Flow<List<FocusSessionV2Entity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionV2Entity): Long

    @Update
    suspend fun updateSession(session: FocusSessionV2Entity)

    @Delete
    suspend fun deleteSession(session: FocusSessionV2Entity)

    @Query("DELETE FROM focus_session_v2")
    suspend fun clearAllSessions()
}

@Dao
interface FocusStatisticsDao {
    @Query("SELECT * FROM focus_statistics WHERE id = 1 LIMIT 1")
    fun getStatisticsFlow(): Flow<FocusStatisticsEntity?>

    @Query("SELECT * FROM focus_statistics WHERE id = 1 LIMIT 1")
    suspend fun getStatistics(): FocusStatisticsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStatistics(stats: FocusStatisticsEntity)

    @Query("DELETE FROM focus_statistics")
    suspend fun clearStatistics()
}
