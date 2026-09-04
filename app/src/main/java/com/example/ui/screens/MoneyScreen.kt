package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.*
import com.example.domain.money.FinancialOverview
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QuickAddTab
import com.example.ui.theme.*
import com.example.ui.viewmodel.YawmekUiState
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class FinanceTab(val titleEn: String, val titleAr: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OVERVIEW("Overview", "النظرة العامة", Icons.Filled.AccountBalance),
    TRANSACTIONS("Transactions", "المعاملات", Icons.Filled.ReceiptLong),
    GOALS("Savings Goals", "أهداف الادخار", Icons.Filled.Savings),
    RECURRING("Recurring", "المتكررة", Icons.Filled.Autorenew)
}

@Composable
fun MoneyScreen(
    uiState: YawmekUiState,
    onOpenQuickAdd: (QuickAddTab) -> Unit,
    onSetBudget: (BudgetPeriod, Double) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit
) {
    val language = uiState.userSettings.language
    val isArabic = language == AppLanguage.ARABIC
    val currency = uiState.userSettings.currency

    var selectedTab by remember { mutableStateOf(FinanceTab.OVERVIEW) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("money_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (isArabic) "المالية والمصاريف" else "Money & Finances",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isArabic) "إدارة المحافظ، الميزانيات وأهداف الادخار" else "Wallets, Budgets & Savings Goals",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { onOpenQuickAdd(QuickAddTab.EXPENSE) },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(ColorFinance.copy(alpha = 0.15f))
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Expense",
                    tint = ColorFinance
                )
            }
        }

        // Section Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 20.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)) }
        ) {
            FinanceTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(tab.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) tab.titleAr else tab.titleEn)
                        }
                    }
                )
            }
        }

        // Tab Content
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "finance_tabs_anim"
        ) { tab ->
            when (tab) {
                FinanceTab.OVERVIEW -> FinanceOverviewContent(
                    isArabic = isArabic,
                    currency = currency,
                    uiState = uiState,
                    onOpenQuickAdd = onOpenQuickAdd,
                    onSetBudgetClick = { showBudgetDialog = true }
                )
                FinanceTab.TRANSACTIONS -> FinanceTransactionsContent(
                    isArabic = isArabic,
                    currency = currency,
                    expenses = uiState.expenses,
                    onDeleteExpense = onDeleteExpense,
                    onOpenQuickAdd = onOpenQuickAdd
                )
                FinanceTab.GOALS -> FinanceGoalsContent(
                    isArabic = isArabic,
                    currency = currency,
                    goals = uiState.financialGoals,
                    onOpenQuickAdd = onOpenQuickAdd
                )
                FinanceTab.RECURRING -> FinanceRecurringContent(
                    isArabic = isArabic,
                    currency = currency,
                    recurring = uiState.recurringTransactions,
                    onOpenQuickAdd = onOpenQuickAdd
                )
            }
        }
    }

    // Set Budget Dialog
    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text(if (isArabic) "تحديد الميزانية الشهرية" else "Set Monthly Budget") },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = { Text(if (isArabic) "المبلغ ($currency)" else "Budget ($currency)") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val amount = budgetInput.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onSetBudget(BudgetPeriod.MONTHLY, amount)
                        showBudgetDialog = false
                    }
                }) {
                    Text(if (isArabic) "حفظ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun FinanceOverviewContent(
    isArabic: Boolean,
    currency: String,
    uiState: YawmekUiState,
    onOpenQuickAdd: (QuickAddTab) -> Unit,
    onSetBudgetClick: () -> Unit
) {
    val overview = uiState.financialOverview
    val isOverBudget = overview?.isOverMonthlyBudget == true

    val netSavingsAmount = if (overview != null) overview.netSavingsMinor / 100.0 else (uiState.monthlyBudget - uiState.monthlySpending)
    val totalIncomeAmount = if (overview != null) overview.totalIncomeMinor / 100.0 else 0.0
    val totalExpenseAmount = if (overview != null) overview.totalExpenseMinor / 100.0 else uiState.monthlySpending

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overspending Warning Alert
        if (isOverBudget) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SemanticErrorContainer),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = SemanticError)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) "تنبيه: تجاوزت الميزانية المحددة!" else "Alert: Budget Overspending Detected!",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = SemanticError
                            )
                            Text(
                                text = if (isArabic) "مصاريفك تجاوزت الميزانية المقررة. راجع مصروفاتك لضبط التوازن."
                                else "Your current spending has exceeded your set budget. Review expenses to restore balance.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Net Balance Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (isArabic) "صافي الرصيد الكلي" else "Net Total Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format("%.2f", netSavingsAmount)} $currency",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (netSavingsAmount >= 0) Color(0xFF10B981) else SemanticError
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(if (isArabic) "الدخل" else "Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("+${String.format("%.2f", totalIncomeAmount)} $currency", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF10B981))
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SemanticError.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = SemanticError, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(if (isArabic) "المصاريف" else "Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("-${String.format("%.2f", totalExpenseAmount)} $currency", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = SemanticError)
                            }
                        }
                    }
                }
            }
        }

        // Wallets / Accounts Row
        if (uiState.accounts.isNotEmpty()) {
            item {
                Text(
                    text = if (isArabic) "المحافظ والحسابات المالية (${uiState.accounts.size})" else "Accounts & Wallets (${uiState.accounts.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.accounts) { acc ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.width(160.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = ColorFinance)
                                    Text(
                                        text = acc.type.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${String.format("%.2f", acc.balanceMinor / 100.0)} $currency",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Monthly Budget Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.PieChart, contentDescription = null, tint = BrandAmber)
                            Text(
                                text = if (isArabic) "الميزانية الشهرية" else "Monthly Budget",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        TextButton(onClick = onSetBudgetClick) {
                            Text(
                                text = if (uiState.monthlyBudget > 0) (if (isArabic) "تعديل" else "Edit") else (if (isArabic) "تحديد ميزانية" else "Set Budget"),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (uiState.monthlyBudget > 0) {
                        val progress = (uiState.monthlySpending / uiState.monthlyBudget).coerceIn(0.0, 1.0).toFloat()
                        val isOver = uiState.monthlySpending > uiState.monthlyBudget
                        val remaining = uiState.monthlyBudget - uiState.monthlySpending

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (isOver) SemanticError else if (progress >= 0.8f) SemanticWarning else ColorFinance,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isArabic) "المتبقي: $remaining $currency" else "Remaining: $remaining $currency",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isOver) SemanticError else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isOver) SemanticError else ColorFinance
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isArabic) "حدد هدفاً شهرياً لمصاريفك لتتبع استهلاكك وتوفير المال بسهولة." else "Set a monthly spending limit to keep your personal finances in healthy balance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceTransactionsContent(
    isArabic: Boolean,
    currency: String,
    expenses: List<ExpenseEntity>,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onOpenQuickAdd: (QuickAddTab) -> Unit
) {
    if (expenses.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateCard(
                icon = Icons.Filled.Savings,
                iconTint = ColorFinance,
                title = if (isArabic) "لا توجد مصاريف مسجلة" else "No expenses logged yet",
                subtitle = if (isArabic) "سجل مصاريفك اليومية مثل القهوة، المواصلات أو الفواتير بضغطة زر." else "Log your coffee, commute, groceries, or bills to see your spending insights.",
                actionButtonText = if (isArabic) "+ تسجيل مصروف" else "+ Log Expense",
                onActionClick = { onOpenQuickAdd(QuickAddTab.EXPENSE) }
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = if (isArabic) "المعاملات السابقة (${expenses.size})" else "Logged Transactions (${expenses.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            items(expenses, key = { it.id }) { expense ->
                ExpenseRow(
                    expense = expense,
                    currency = currency,
                    onDelete = { onDeleteExpense(expense) }
                )
            }
        }
    }
}

@Composable
private fun FinanceGoalsContent(
    isArabic: Boolean,
    currency: String,
    goals: List<FinancialGoalEntity>,
    onOpenQuickAdd: (QuickAddTab) -> Unit
) {
    if (goals.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isArabic) "لا توجد أهداف ادخار حالياً 🎯" else "No savings goals created yet 🎯",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if (isArabic) "أهداف الادخار المالية (${goals.size})" else "Active Savings Goals (${goals.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            items(goals, key = { it.id }) { goal ->
                val progress = if (goal.targetAmountMinor > 0) (goal.currentSavedMinor.toFloat() / goal.targetAmountMinor).coerceIn(0f, 1f) else 0f

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Savings, contentDescription = null, tint = Color(0xFF10B981))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(goal.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                    Text(
                                        text = "${goal.currentSavedMinor / 100} / ${goal.targetAmountMinor / 100} $currency",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF10B981)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF10B981),
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceRecurringContent(
    isArabic: Boolean,
    currency: String,
    recurring: List<RecurringTransactionEntity>,
    onOpenQuickAdd: (QuickAddTab) -> Unit
) {
    if (recurring.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isArabic) "لا توجد معاملات متكررة مجدولة 🔄" else "No recurring transactions scheduled 🔄",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = if (isArabic) "الاشتراكات والرواتب المتكررة (${recurring.size})" else "Recurring Cash Flow (${recurring.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            items(recurring, key = { it.id }) { item ->
                val isIncome = item.type == TransactionType.INCOME

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(if (item.note.isNotBlank()) item.note else item.category, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${item.frequency.name} • ${item.category}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${if (isIncome) "+" else "-"}${item.amountMinor / 100.0} $currency",
                            fontWeight = FontWeight.Bold,
                            color = if (isIncome) Color(0xFF10B981) else SemanticError
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: ExpenseEntity,
    currency: String,
    onDelete: () -> Unit
) {
    val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(expense.dateMillis), ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("MMM d, HH:mm")
    val dateStr = date.format(formatter)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ColorFinance.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        tint = ColorFinance,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = expense.category.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (expense.note.isNotBlank()) "${expense.note} • $dateStr" else dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "-${expense.amount} $currency",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SemanticError
                )

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
