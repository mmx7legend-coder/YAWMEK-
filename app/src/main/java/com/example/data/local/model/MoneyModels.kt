package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class AccountType(val titleEn: String, val titleAr: String, val defaultIcon: String) {
    CASH("Cash", "نقدي", "wallet"),
    BANK("Bank Account", "حساب بنكي", "account_balance"),
    SAVINGS("Savings Vault", "خزنة التوفير", "savings"),
    CREDIT_CARD("Credit Card", "بطاقة ائتمان", "credit_card"),
    DIGITAL_WALLET("Digital Wallet", "محفظة إلكترونية", "phone_android"),
    INVESTMENT("Investment", "استثمار", "trending_up")
}

@Entity(tableName = "wallet_accounts")
data class WalletAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType = AccountType.CASH,
    val balanceMinor: Long = 0L, // Stored in minor currency units (cents / piasters)
    val currency: String = "EGP",
    val colorHex: String = "#10B981",
    val iconName: String = "wallet",
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

enum class TransactionType(val titleEn: String, val titleAr: String) {
    EXPENSE("Expense", "مصروف"),
    INCOME("Income", "دخل"),
    TRANSFER("Transfer", "تحويل")
}

@Entity(tableName = "financial_transactions")
data class FinancialTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long, // Source account
    val toAccountId: Long? = null, // Destination account for transfers
    val type: TransactionType = TransactionType.EXPENSE,
    val amountMinor: Long, // Precise integer minor units (always positive)
    val currency: String = "EGP",
    val category: String = "OTHER",
    val subcategory: String = "",
    val note: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val recurringRuleId: Long? = null,
    val goalId: Long? = null, // Set if this is a contribution to a financial goal
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "financial_categories")
data class FinancialCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameEn: String,
    val nameAr: String,
    val type: TransactionType = TransactionType.EXPENSE,
    val iconName: String = "category",
    val colorHex: String = "#3B82F6",
    val parentCategoryId: Long? = null,
    val isCustom: Boolean = false
)

enum class RecurringFrequency(val titleEn: String, val titleAr: String) {
    DAILY("Daily", "يومي"),
    WEEKLY("Weekly", "أسبوعي"),
    MONTHLY("Monthly", "شهري"),
    YEARLY("Yearly", "سنوي")
}

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val toAccountId: Long? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    val amountMinor: Long,
    val currency: String = "EGP",
    val category: String = "OTHER",
    val subcategory: String = "",
    val note: String = "",
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val nextDueDateMillis: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "financial_goals")
data class FinancialGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmountMinor: Long,
    val currentSavedMinor: Long = 0L,
    val currency: String = "EGP",
    val targetDateMillis: Long? = null,
    val colorHex: String = "#8B5CF6",
    val iconName: String = "savings",
    val isReached: Boolean = false,
    val notes: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

object MoneyUtils {
    /**
     * Converts a user-typed double or decimal to integer minor units (e.g. 10.50 -> 1050).
     */
    fun toMinor(amount: Double): Long {
        return Math.round(amount * 100.0)
    }

    fun toMinor(amountStr: String): Long {
        val clean = amountStr.trim().replace(",", ".")
        val doubleVal = clean.toDoubleOrNull() ?: 0.0
        return toMinor(doubleVal)
    }

    /**
     * Converts minor integer units back to standard double for display or calculations.
     */
    fun fromMinor(minor: Long): Double {
        return minor / 100.0
    }

    /**
     * Formats minor integer units safely with currency string.
     * Example: 15050L -> "150.50 EGP"
     */
    fun formatMinor(minor: Long, currency: String = "EGP"): String {
        val units = minor / 100
        val decimals = Math.abs(minor % 100)
        val df = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))
        val formattedUnits = df.format(units)
        return if (decimals == 0L) {
            "$formattedUnits $currency"
        } else {
            val decStr = String.format(Locale.US, "%02d", decimals)
            "$formattedUnits.$decStr $currency"
        }
    }

    fun formatDouble(amount: Double, currency: String = "EGP"): String {
        return formatMinor(toMinor(amount), currency)
    }
}
