package com.example

import com.example.data.local.model.*
import com.example.domain.money.MoneyCalculator
import org.junit.Assert.*
import org.junit.Test

class MoneyCalculatorTest {

    @Test
    fun testNetWorthCalculation() {
        val accounts = listOf(
            WalletAccountEntity(
                id = 1L,
                name = "Main Checking",
                type = AccountType.BANK,
                balanceMinor = 250000L, // 2,500.00
                currency = "USD",
                iconName = "account_balance",
                colorHex = "#10B981"
            ),
            WalletAccountEntity(
                id = 2L,
                name = "Savings Vault",
                type = AccountType.SAVINGS,
                balanceMinor = 1000000L, // 10,000.00
                currency = "USD",
                iconName = "savings",
                colorHex = "#3B82F6"
            ),
            WalletAccountEntity(
                id = 3L,
                name = "Old Account",
                type = AccountType.CASH,
                balanceMinor = 50000L,
                currency = "USD",
                iconName = "money",
                colorHex = "#9CA3AF",
                isArchived = true // Archived must be skipped
            )
        )

        val totalNetWorth = MoneyCalculator.calculateNetWorth(accounts)
        assertEquals(1250000L, totalNetWorth)
    }

    @Test
    fun testSavingsRateCalculation() {
        val incomeMinor = 500000L // 5000
        val expenseMinor = 300000L // 3000

        val rate = MoneyCalculator.calculateSavingsRate(incomeMinor, expenseMinor)
        assertEquals(40.0, rate, 0.001)

        val zeroIncomeRate = MoneyCalculator.calculateSavingsRate(0L, 500L)
        assertEquals(0.0, zeroIncomeRate, 0.001)

        val overspendingRate = MoneyCalculator.calculateSavingsRate(1000L, 2000L)
        assertEquals(0.0, overspendingRate, 0.001)
    }

    @Test
    fun testCalculateSpendingInRange() {
        val now = 1000000L
        val transactions = listOf(
            FinancialTransactionEntity(
                id = 1L,
                accountId = 1L,
                type = TransactionType.EXPENSE,
                amountMinor = 1500L,
                currency = "USD",
                category = "Food",
                dateMillis = now - 500
            ),
            FinancialTransactionEntity(
                id = 2L,
                accountId = 1L,
                type = TransactionType.EXPENSE,
                amountMinor = 2500L,
                currency = "USD",
                category = "Transit",
                dateMillis = now + 100
            ),
            FinancialTransactionEntity(
                id = 3L,
                accountId = 1L,
                type = TransactionType.INCOME,
                amountMinor = 50000L,
                currency = "USD",
                category = "Salary",
                dateMillis = now
            )
        )

        val spending = MoneyCalculator.calculateSpendingInRange(transactions, now - 1000, now)
        assertEquals(1500L, spending)
    }
}
