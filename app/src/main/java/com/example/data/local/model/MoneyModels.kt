package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Money system enums
enum class MoneyTransactionType {
    INCOME, EXPENSE, TRANSFER
}

enum class MoneyCategory {
    // Income
    SALARY, FREELANCE, INVESTMENT, BONUS, GIFT, OTHER_INCOME,
    // Expense
    FOOD, TRANSPORT, EDUCATION, SHOPPING, BILLS, ENTERTAINMENT, HEALTH, UTILITIES, RENT, GROCERIES, OTHER_EXPENSE
}

// Wallet/Account entity
@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val accountType: String = "CHECKING", // CHECKING, SAVINGS, CASH, CARD
    val currency: String = "EGP",
    val balance: Long = 0L, // In minor units (cents, fils, etc.)
    val isDefault: Boolean = false,
    val colorHex: String = "#3B82F6",
    val createdAtMillis: Long = System.currentTimeMillis()
)

// Transaction entity (unified for income, expense, transfer)
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val walletId: Long,
    val type: MoneyTransactionType = MoneyTransactionType.EXPENSE,
    val amount: Long = 0L, // In minor units
    val currency: String = "EGP",
    val category: MoneyCategory = MoneyCategory.OTHER_EXPENSE,
    val description: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val recurringPattern: String = "", // DAILY, WEEKLY, MONTHLY, YEARLY
    val recurringEndDate: Long? = null,
    val tags: String = "", // Comma-separated
    val attachmentPath: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)

// Income entry (streamlined from transactions)
@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val walletId: Long,
    val amount: Long = 0L, // In minor units
    val currency: String = "EGP",
    val source: String = "Salary", // Salary, Freelance, Investment, etc.
    val description: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val recurringPattern: String = "", // DAILY, WEEKLY, MONTHLY, YEARLY
    val createdAtMillis: Long = System.currentTimeMillis()
)

// Expense entry
@Entity(tableName = "expenses_v2")
data class ExpenseV2Entity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val walletId: Long,
    val amount: Long = 0L, // In minor units
    val currency: String = "EGP",
    val category: MoneyCategory = MoneyCategory.OTHER_EXPENSE,
    val subcategory: String = "",
    val description: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val recurringPattern: String = "", // DAILY, WEEKLY, MONTHLY, YEARLY
    val tags: String = "", // Comma-separated
    val createdAtMillis: Long = System.currentTimeMillis()
)

// Budget entity (updated to be wallet-specific and more detailed)
@Entity(tableName = "budgets_v2")
data class BudgetV2Entity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val walletId: Long,
    val category: MoneyCategory? = null, // null means all categories
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val limitAmount: Long = 0L, // In minor units
    val currency: String = "EGP",
    val alertThreshold: Int = 80, // Alert when spending reaches this %
    val rollover: Boolean = false, // Roll over unused budget to next period
    val createdAtMillis: Long = System.currentTimeMillis()
)

// Savings goal
@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val walletId: Long,
    val title: String,
    val description: String = "",
    val targetAmount: Long = 0L, // In minor units
    val currentAmount: Long = 0L, // In minor units
    val currency: String = "EGP",
    val targetDateMillis: Long? = null,
    val category: String = "General",
    val colorHex: String = "#8B5CF6",
    val isCompleted: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

// Savings goal contribution
@Entity(tableName = "goal_contributions")
data class GoalContributionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val amount: Long = 0L, // In minor units
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

// Financial insight/summary (calculated)
@Entity(tableName = "financial_summaries")
data class FinancialSummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val walletId: Long,
    val period: String = "MONTHLY", // DAILY, WEEKLY, MONTHLY, YEARLY
    val periodStartMillis: Long,
    val periodEndMillis: Long,
    val totalIncome: Long = 0L, // In minor units
    val totalExpense: Long = 0L, // In minor units
    val netSavings: Long = 0L, // totalIncome - totalExpense
    val savingsRate: Double = 0.0, // savingsRate = netSavings / totalIncome * 100
    val topExpenseCategory: String = "",
    val averageDailyExpense: Long = 0L, // In minor units
    val createdAtMillis: Long = System.currentTimeMillis()
)

// Transfer between wallets
@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromWalletId: Long,
    val toWalletId: Long,
    val amount: Long = 0L, // In minor units
    val currency: String = "EGP",
    val description: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val createdAtMillis: Long = System.currentTimeMillis()
)
