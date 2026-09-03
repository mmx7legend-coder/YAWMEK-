package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

// Extension to existing YawmekRepository
fun YawmekRepository.getMoneyFlow() = db.walletDao().getAllWallets()
fun YawmekRepository.getTransactionsFlow() = db.transactionDao().getAllTransactions()
fun YawmekRepository.getCalendarEventsFlow() = db.calendarEventDao().getAllEvents()
fun YawmekRepository.getFocusStatisticsFlow() = db.focusStatisticsDao().getStatisticsFlow()

class MoneyRepository(private val db: AppDatabase) {
    // Wallets
    val allWallets: Flow<List<WalletEntity>> = db.walletDao().getAllWallets()
    
    suspend fun getWalletById(walletId: Long) = db.walletDao().getWalletById(walletId)
    suspend fun getDefaultWallet() = db.walletDao().getDefaultWallet()
    suspend fun insertWallet(wallet: WalletEntity): Long = db.walletDao().insertWallet(wallet)
    suspend fun updateWallet(wallet: WalletEntity) = db.walletDao().updateWallet(wallet)
    suspend fun deleteWallet(wallet: WalletEntity) = db.walletDao().deleteWallet(wallet)

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
    
    fun getTransactionsForWallet(walletId: Long): Flow<List<TransactionEntity>> = 
        db.transactionDao().getTransactionsForWallet(walletId)
    
    fun getTransactionsInRange(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>> = 
        db.transactionDao().getTransactionsInRange(startMillis, endMillis)
    
    fun getWalletTransactionsInRange(walletId: Long, startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>> = 
        db.transactionDao().getWalletTransactionsInRange(walletId, startMillis, endMillis)
    
    suspend fun insertTransaction(transaction: TransactionEntity): Long = 
        db.transactionDao().insertTransaction(transaction)
    
    suspend fun updateTransaction(transaction: TransactionEntity) = 
        db.transactionDao().updateTransaction(transaction)
    
    suspend fun deleteTransaction(transaction: TransactionEntity) = 
        db.transactionDao().deleteTransaction(transaction)

    // Savings Goals
    val allSavingsGoals: Flow<List<SavingsGoalEntity>> = db.savingsGoalDao().getAllGoals()
    
    suspend fun getGoalById(goalId: Long) = db.savingsGoalDao().getGoalById(goalId)
    suspend fun insertSavingsGoal(goal: SavingsGoalEntity): Long = db.savingsGoalDao().insertGoal(goal)
    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) = db.savingsGoalDao().updateGoal(goal)
    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) = db.savingsGoalDao().deleteGoal(goal)

    // Goal Contributions
    fun getContributionsForGoal(goalId: Long): Flow<List<GoalContributionEntity>> = 
        db.savingsGoalDao().getContributionsForGoal(goalId)
    
    suspend fun addContribution(contribution: GoalContributionEntity): Long = 
        db.savingsGoalDao().insertContribution(contribution)

    // Financial Summaries
    val allSummaries: Flow<List<FinancialSummaryEntity>> = db.financialSummaryDao().getAllSummaries()
    
    suspend fun getLatestSummaryForWallet(walletId: Long) = 
        db.financialSummaryDao().getLatestSummaryForWallet(walletId)
    
    suspend fun insertSummary(summary: FinancialSummaryEntity): Long = 
        db.financialSummaryDao().insertSummary(summary)

    // Transfers
    val allTransfers: Flow<List<TransferEntity>> = db.transferDao().getAllTransfers()
    
    fun getTransfersForWallet(walletId: Long): Flow<List<TransferEntity>> = 
        db.transferDao().getTransfersForWallet(walletId)
    
    suspend fun insertTransfer(transfer: TransferEntity): Long = 
        db.transferDao().insertTransfer(transfer)
    
    suspend fun deleteTransfer(transfer: TransferEntity) = 
        db.transferDao().deleteTransfer(transfer)

    // Calculations
    suspend fun calculateMonthlySpending(walletId: Long): Long {
        val monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val monthEnd = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 86400000
        
        var total = 0L
        db.transactionDao().getWalletTransactionsInRange(walletId, monthStart, monthEnd).collect { transactions ->
            total = transactions.filter { it.type == MoneyTransactionType.EXPENSE }.sumOf { it.amount }
        }
        return total
    }

    suspend fun calculateMonthlySavings(walletId: Long): Long {
        val monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val monthEnd = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 86400000
        
        var income = 0L
        var expense = 0L
        db.transactionDao().getWalletTransactionsInRange(walletId, monthStart, monthEnd).collect { transactions ->
            income = transactions.filter { it.type == MoneyTransactionType.INCOME }.sumOf { it.amount }
            expense = transactions.filter { it.type == MoneyTransactionType.EXPENSE }.sumOf { it.amount }
        }
        return income - expense
    }
}

class CalendarRepository(private val db: AppDatabase) {
    val allEvents: Flow<List<CalendarEventEntity>> = db.calendarEventDao().getAllEvents()
    
    fun getEventsInRange(startMillis: Long, endMillis: Long): Flow<List<CalendarEventEntity>> = 
        db.calendarEventDao().getEventsInRange(startMillis, endMillis)
    
    fun getEventsForDay(dayStartMillis: Long, dayEndMillis: Long): Flow<List<CalendarEventEntity>> = 
        db.calendarEventDao().getEventsForDay(dayStartMillis, dayEndMillis)
    
    suspend fun getEventById(eventId: Long) = db.calendarEventDao().getEventById(eventId)
    suspend fun insertEvent(event: CalendarEventEntity): Long = db.calendarEventDao().insertEvent(event)
    suspend fun updateEvent(event: CalendarEventEntity) = db.calendarEventDao().updateEvent(event)
    suspend fun deleteEvent(event: CalendarEventEntity) = db.calendarEventDao().deleteEvent(event)

    // Conflict detection
    suspend fun checkConflict(startMillis: Long, endMillis: Long): Boolean {
        var hasConflict = false
        db.calendarEventDao().getEventsInRange(startMillis, endMillis).collect { events ->
            hasConflict = events.any { event ->
                !(endMillis <= event.startMillis || startMillis >= event.endMillis)
            }
        }
        return hasConflict
    }
}

class FocusRepository(private val db: AppDatabase) {
    val allSessions: Flow<List<FocusSessionV2Entity>> = db.focusSessionV2Dao().getAllSessions()
    
    fun getSessionsSince(startMillis: Long): Flow<List<FocusSessionV2Entity>> = 
        db.focusSessionV2Dao().getSessionsSince(startMillis)
    
    fun getSessionsForDay(dayStartMillis: Long, dayEndMillis: Long): Flow<List<FocusSessionV2Entity>> = 
        db.focusSessionV2Dao().getSessionsForDay(dayStartMillis, dayEndMillis)
    
    fun getCompletedSessionsSince(startMillis: Long): Flow<List<FocusSessionV2Entity>> = 
        db.focusSessionV2Dao().getCompletedSessionsSince(startMillis)
    
    suspend fun insertSession(session: FocusSessionV2Entity): Long = db.focusSessionV2Dao().insertSession(session)
    suspend fun updateSession(session: FocusSessionV2Entity) = db.focusSessionV2Dao().updateSession(session)
    suspend fun deleteSession(session: FocusSessionV2Entity) = db.focusSessionV2Dao().deleteSession(session)

    // Statistics
    val statistics: Flow<FocusStatisticsEntity?> = db.focusStatisticsDao().getStatisticsFlow()
    
    suspend fun getStatistics() = db.focusStatisticsDao().getStatistics()
    suspend fun updateStatistics(stats: FocusStatisticsEntity) = db.focusStatisticsDao().insertOrUpdateStatistics(stats)

    // XP anti-farming: Calculate earned XP based on actual session length
    fun calculateXPEarned(durationMinutes: Int, actualSeconds: Int, mode: FocusTimerMode): Int {
        if (actualSeconds < 60) return 0 // Minimum 1 minute
        val completedMinutes = actualSeconds / 60
        if (completedMinutes < durationMinutes / 2) return (completedMinutes * 5).coerceAtMost(50) // Partial credit
        return durationMinutes * 10 // Full credit
    }
}
