package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletAccountDao {
    @Query("SELECT * FROM wallet_accounts WHERE isArchived = 0 ORDER BY isDefault DESC, createdAtMillis ASC")
    fun getAllActiveAccounts(): Flow<List<WalletAccountEntity>>

    @Query("SELECT * FROM wallet_accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): WalletAccountEntity?

    @Query("SELECT * FROM wallet_accounts WHERE isDefault = 1 AND isArchived = 0 LIMIT 1")
    suspend fun getDefaultAccount(): WalletAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: WalletAccountEntity): Long

    @Update
    suspend fun updateAccount(account: WalletAccountEntity)

    @Delete
    suspend fun deleteAccount(account: WalletAccountEntity)

    @Query("UPDATE wallet_accounts SET balanceMinor = balanceMinor + :deltaMinor WHERE id = :accountId")
    suspend fun updateBalanceDelta(accountId: Long, deltaMinor: Long)

    @Query("UPDATE wallet_accounts SET isDefault = 0")
    suspend fun clearDefaultFlags()
}

@Dao
interface FinancialTransactionDao {
    @Query("SELECT * FROM financial_transactions ORDER BY dateMillis DESC, createdAtMillis DESC")
    fun getAllTransactions(): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY dateMillis DESC")
    fun getTransactionsInRange(startMillis: Long, endMillis: Long): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions WHERE accountId = :accountId OR toAccountId = :accountId ORDER BY dateMillis DESC")
    fun getTransactionsForAccount(accountId: Long): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions WHERE goalId = :goalId ORDER BY dateMillis DESC")
    fun getTransactionsForGoal(goalId: Long): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): FinancialTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinancialTransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: FinancialTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: FinancialTransactionEntity)

    @Query("DELETE FROM financial_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}

@Dao
interface FinancialCategoryDao {
    @Query("SELECT * FROM financial_categories ORDER BY isCustom DESC, nameEn ASC")
    fun getAllCategories(): Flow<List<FinancialCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: FinancialCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<FinancialCategoryEntity>)

    @Update
    suspend fun updateCategory(category: FinancialCategoryEntity)

    @Delete
    suspend fun deleteCategory(category: FinancialCategoryEntity)
}

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 ORDER BY nextDueDateMillis ASC")
    fun getActiveRecurring(): Flow<List<RecurringTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(recurring: RecurringTransactionEntity): Long

    @Update
    suspend fun updateRecurring(recurring: RecurringTransactionEntity)

    @Delete
    suspend fun deleteRecurring(recurring: RecurringTransactionEntity)
}

@Dao
interface FinancialGoalDao {
    @Query("SELECT * FROM financial_goals ORDER BY isReached ASC, createdAtMillis DESC")
    fun getAllGoals(): Flow<List<FinancialGoalEntity>>

    @Query("SELECT * FROM financial_goals WHERE id = :id")
    suspend fun getGoalById(id: Long): FinancialGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: FinancialGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: FinancialGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: FinancialGoalEntity)

    @Query("UPDATE financial_goals SET currentSavedMinor = currentSavedMinor + :deltaMinor, isReached = CASE WHEN (currentSavedMinor + :deltaMinor) >= targetAmountMinor THEN 1 ELSE 0 END WHERE id = :goalId")
    suspend fun contributeToGoal(goalId: Long, deltaMinor: Long)
}
