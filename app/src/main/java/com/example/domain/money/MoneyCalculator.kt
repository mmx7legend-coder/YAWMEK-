package com.example.domain.money

import com.example.data.local.model.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

data class FinancialCategorySummary(
    val categoryName: String,
    val totalAmountMinor: Long,
    val percentage: Float,
    val transactionCount: Int
)

data class FinancialOverview(
    val totalNetWorthMinor: Long,
    val totalIncomeMinor: Long,
    val totalExpenseMinor: Long,
    val netSavingsMinor: Long,
    val savingsRatePercent: Double,
    val todaySpendingMinor: Long,
    val weekSpendingMinor: Long,
    val monthSpendingMinor: Long,
    val topCategories: List<FinancialCategorySummary>,
    val budgetHealthScore: Int,
    val isOverMonthlyBudget: Boolean,
    val remainingBudgetMinor: Long,
    val insightsEn: List<String>,
    val insightsAr: List<String>
)

object MoneyCalculator {

    /**
     * Calculates total net worth across all active accounts in minor units.
     * Credit cards are treated as liabilities (subtracting outstanding balance or debt).
     */
    fun calculateNetWorth(accounts: List<WalletAccountEntity>): Long {
        var total = 0L
        for (account in accounts) {
            if (account.isArchived) continue
            if (account.type == AccountType.CREDIT_CARD) {
                // If credit card has negative balance or represents debt
                total += account.balanceMinor
            } else {
                total += account.balanceMinor
            }
        }
        return total
    }

    /**
     * Calculates savings rate: (Income - Expenses) / Income * 100
     */
    fun calculateSavingsRate(incomeMinor: Long, expenseMinor: Long): Double {
        if (incomeMinor <= 0L) return 0.0
        val savings = incomeMinor - expenseMinor
        if (savings <= 0L) return 0.0
        return (savings.toDouble() / incomeMinor.toDouble()) * 100.0
    }

    /**
     * Computes spending for a specific date range.
     */
    fun calculateSpendingInRange(
        transactions: List<FinancialTransactionEntity>,
        startMillis: Long,
        endMillis: Long
    ): Long {
        return transactions.filter {
            it.type == TransactionType.EXPENSE && it.dateMillis in startMillis..endMillis
        }.sumOf { it.amountMinor }
    }

    fun calculateIncomeInRange(
        transactions: List<FinancialTransactionEntity>,
        startMillis: Long,
        endMillis: Long
    ): Long {
        return transactions.filter {
            it.type == TransactionType.INCOME && it.dateMillis in startMillis..endMillis
        }.sumOf { it.amountMinor }
    }

    /**
     * Categorizes spending and returns sorted list with percentages.
     */
    fun getTopSpendingCategories(
        transactions: List<FinancialTransactionEntity>,
        startMillis: Long,
        endMillis: Long
    ): List<FinancialCategorySummary> {
        val periodExpenses = transactions.filter {
            it.type == TransactionType.EXPENSE && it.dateMillis in startMillis..endMillis
        }
        val totalExpense = periodExpenses.sumOf { it.amountMinor }
        if (totalExpense == 0L) return emptyList()

        val grouped = periodExpenses.groupBy { it.category }
        return grouped.map { (cat, txList) ->
            val catTotal = txList.sumOf { it.amountMinor }
            val pct = (catTotal.toFloat() / totalExpense.toFloat()) * 100f
            FinancialCategorySummary(
                categoryName = cat,
                totalAmountMinor = catTotal,
                percentage = pct,
                transactionCount = txList.size
            )
        }.sortedByDescending { it.totalAmountMinor }
    }

    /**
     * Calculates a financial health score between 0 and 100.
     * Evaluates savings rate (40%), budget adherence (40%), and consistency (20%).
     */
    fun calculateBudgetHealthScore(
        incomeMinor: Long,
        expenseMinor: Long,
        budgetLimitMinor: Long
    ): Int {
        var score = 50 // Base score

        // Savings factor (up to +30 or -30)
        if (incomeMinor > 0L) {
            val savingsRate = calculateSavingsRate(incomeMinor, expenseMinor)
            when {
                savingsRate >= 30.0 -> score += 30
                savingsRate >= 20.0 -> score += 20
                savingsRate >= 10.0 -> score += 10
                savingsRate > 0.0 -> score += 5
                else -> score -= 20
            }
        }

        // Budget factor (up to +20 or -30)
        if (budgetLimitMinor > 0L) {
            when {
                expenseMinor > budgetLimitMinor -> score -= 30
                expenseMinor >= (budgetLimitMinor * 0.9).toLong() -> score -= 10
                expenseMinor <= (budgetLimitMinor * 0.7).toLong() -> score += 20
                else -> score += 10
            }
        }

        return score.coerceIn(0, 100)
    }

    /**
     * Generates actionable insights based on real financial patterns.
     */
    fun generateFinancialInsights(
        incomeMinor: Long,
        expenseMinor: Long,
        budgetLimitMinor: Long,
        topCategories: List<FinancialCategorySummary>
    ): Pair<List<String>, List<String>> {
        val insightsEn = mutableListOf<String>()
        val insightsAr = mutableListOf<String>()

        val savingsRate = calculateSavingsRate(incomeMinor, expenseMinor)
        if (savingsRate >= 20.0) {
            insightsEn.add("Excellent savings rate of ${savingsRate.roundToInt()}%. You are building strong financial resilience.")
            insightsAr.add("معدل ادخار ممتاز بنسبة ${savingsRate.roundToInt()}%. أنت تبني أماناً مالياً متيناً.")
        } else if (savingsRate > 0) {
            insightsEn.add("You are currently saving ${savingsRate.roundToInt()}% of income. Aim for 20% to accelerate your goals.")
            insightsAr.add("توفر حالياً ${savingsRate.roundToInt()}% من دخلك. حاول الوصول إلى 20% لتسريع أهدافك.")
        } else if (expenseMinor > incomeMinor && incomeMinor > 0) {
            insightsEn.add("Warning: Expenses exceed income by ${MoneyUtils.formatMinor(expenseMinor - incomeMinor)}. Review discretionary spending.")
            insightsAr.add("تنبيه: المصاريف تفوق الدخل بمقدار ${MoneyUtils.formatMinor(expenseMinor - incomeMinor)}. راجع المصاريف غير الضرورية.")
        }

        if (budgetLimitMinor > 0L) {
            if (expenseMinor > budgetLimitMinor) {
                val over = expenseMinor - budgetLimitMinor
                insightsEn.add("Monthly budget exceeded by ${MoneyUtils.formatMinor(over)}. Pause non-essential purchases.")
                insightsAr.add("تجاوزت الميزانية الشهرية بمقدار ${MoneyUtils.formatMinor(over)}. ينصح بإيقاف المشتريات الترفيهية.")
            } else {
                val remaining = budgetLimitMinor - expenseMinor
                insightsEn.add("You have ${MoneyUtils.formatMinor(remaining)} left in your monthly budget.")
                insightsAr.add("متبقٍ لديك ${MoneyUtils.formatMinor(remaining)} من ميزانيتك الشهرية.")
            }
        }

        if (topCategories.isNotEmpty()) {
            val top = topCategories.first()
            insightsEn.add("Top spending category is ${top.categoryName} (${top.percentage.roundToInt()}% of total expenses).")
            insightsAr.add("أعلى بند إنفاق هو ${top.categoryName} بنسبة ${top.percentage.roundToInt()}% من إجمالي مصاريفك.")
        }

        if (insightsEn.isEmpty()) {
            insightsEn.add("Keep logging transactions to unlock personalized smart spending insights.")
            insightsAr.add("استمر في تسجيل المعاملات لتفعيل تحليلات الإنفاق الذكية.")
        }

        return Pair(insightsEn, insightsAr)
    }

    /**
     * Builds comprehensive financial overview.
     */
    fun buildFinancialOverview(
        accounts: List<WalletAccountEntity>,
        transactions: List<FinancialTransactionEntity>,
        monthlyBudgetMinor: Long,
        targetDate: LocalDate = LocalDate.now()
    ): FinancialOverview {
        val zone = ZoneId.systemDefault()
        val startOfMonth = targetDate.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfMonth = targetDate.plusMonths(1).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

        val startOfDay = targetDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfDay = targetDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

        val startOfWeek = targetDate.minusDays(targetDate.dayOfWeek.value.toLong() - 1).atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfWeek = startOfWeek + (7 * 86400000L) - 1

        val netWorth = calculateNetWorth(accounts)
        val monthIncome = calculateIncomeInRange(transactions, startOfMonth, endOfMonth)
        val monthExpense = calculateSpendingInRange(transactions, startOfMonth, endOfMonth)
        val todaySpending = calculateSpendingInRange(transactions, startOfDay, endOfDay)
        val weekSpending = calculateSpendingInRange(transactions, startOfWeek, endOfWeek)

        val netSavings = monthIncome - monthExpense
        val savingsRate = calculateSavingsRate(monthIncome, monthExpense)
        val topCategories = getTopSpendingCategories(transactions, startOfMonth, endOfMonth)
        val healthScore = calculateBudgetHealthScore(monthIncome, monthExpense, monthlyBudgetMinor)
        val isOverBudget = monthlyBudgetMinor > 0 && monthExpense > monthlyBudgetMinor
        val remainingBudget = if (monthlyBudgetMinor > 0) (monthlyBudgetMinor - monthExpense).coerceAtLeast(0L) else 0L

        val (insightsEn, insightsAr) = generateFinancialInsights(
            monthIncome,
            monthExpense,
            monthlyBudgetMinor,
            topCategories
        )

        return FinancialOverview(
            totalNetWorthMinor = netWorth,
            totalIncomeMinor = monthIncome,
            totalExpenseMinor = monthExpense,
            netSavingsMinor = netSavings,
            savingsRatePercent = savingsRate,
            todaySpendingMinor = todaySpending,
            weekSpendingMinor = weekSpending,
            monthSpendingMinor = monthExpense,
            topCategories = topCategories,
            budgetHealthScore = healthScore,
            isOverMonthlyBudget = isOverBudget,
            remainingBudgetMinor = remainingBudget,
            insightsEn = insightsEn,
            insightsAr = insightsAr
        )
    }
}
