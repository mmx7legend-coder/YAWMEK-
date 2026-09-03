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

// Financial summary (calculated/cached)
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

// Calendar Event entity
@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val startMillis: Long,
    val endMillis: Long,
    val allDay: Boolean = false,
    val location: String = "",
    val colorHex: String = "#3B82F6",
    val isRecurring: Boolean = false,
    val recurringPattern: String = "", // DAILY, WEEKLY, MONTHLY, YEARLY
    val reminderMinutesBefore: Int = 15,
    val category: String = "OTHER", // WORK, PERSONAL, HEALTH, FINANCE, EVENT
    val notes: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

// Enhanced Focus Session Models
enum class FocusAudioType(val titleEn: String, val titleAr: String) {
    SILENT("Silent", "صامت"),
    RAIN("Rain", "مطر"),
    FOREST("Forest", "غابة"),
    WHITE_NOISE("White Noise", "ضوضاء بيضاء"),
    BROWN_NOISE("Brown Noise", "ضوضاء بنية"),
    BINAURAL("Binaural", "ثنائي المسار")
}

@Entity(tableName = "focus_session_v2")
data class FocusSessionV2Entity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val durationMinutes: Int = 25,
    val actualSeconds: Int = 0,
    val mode: FocusTimerMode = FocusTimerMode.POMODORO_25,
    val isCompleted: Boolean = false,
    val startedAtMillis: Long = System.currentTimeMillis(),
    val completedAtMillis: Long? = null,
    val audioType: FocusAudioType = FocusAudioType.SILENT,
    val audioVolume: Float = 0.5f,
    val hapticsFeedback: Boolean = true,
    val notes: String = "",
    val xpEarned: Int = 0
)

// Focus Statistics
@Entity(tableName = "focus_statistics")
data class FocusStatisticsEntity(
    @PrimaryKey val id: Int = 1,
    val totalFocusMinutes: Int = 0,
    val totalSessions: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val longestSession: Int = 0,
    val averageSessionLength: Int = 0,
    val focusGoalMinutesPerDay: Int = 120,
    val dailyFocusMinutesAchieved: Int = 0,
    val lastFocusDateMillis: Long = 0L,
    val createdAtMillis: Long = System.currentTimeMillis()
)
