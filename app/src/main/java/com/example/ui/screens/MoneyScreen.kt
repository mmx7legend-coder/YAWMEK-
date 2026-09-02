package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.*
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QuickAddTab
import com.example.ui.theme.*
import com.example.ui.viewmodel.YawmekUiState
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("money_screen"),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isArabic) "المصاريف والميزانية" else "Money & Expenses",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = { onOpenQuickAdd(QuickAddTab.EXPENSE) },
                    modifier = Modifier
                        .size(40.dp)
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
        }

        // Summary Cards (Today, Month)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = if (isArabic) "مصاريف اليوم" else "Today's Spent",
                    amount = "${uiState.todaySpending}",
                    currency = currency,
                    icon = Icons.Filled.Today,
                    color = ColorFinance,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = if (isArabic) "مصاريف الشهر" else "Month's Spent",
                    amount = "${uiState.monthlySpending}",
                    currency = currency,
                    icon = Icons.Filled.DateRange,
                    color = BrandBlue,
                    modifier = Modifier.weight(1f)
                )
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

                        TextButton(onClick = { showBudgetDialog = true }) {
                            Text(
                                text = if (uiState.monthlyBudget > 0) (if (isArabic) "تعديل" else "Edit") else (if (isArabic) "تحديد ميزانية" else "Set Budget"),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (uiState.monthlyBudget > 0) {
                        val progress = (uiState.monthlySpending / uiState.monthlyBudget).coerceIn(0.0, 1.0).toFloat()
                        val isOverBudget = uiState.monthlySpending > uiState.monthlyBudget
                        val remaining = uiState.monthlyBudget - uiState.monthlySpending

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (isOverBudget) SemanticError else if (progress >= 0.8f) SemanticWarning else ColorFinance,
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
                                color = if (isOverBudget) SemanticError else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isOverBudget) SemanticError else ColorFinance
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

        // Transactions List
        item {
            Text(
                text = if (isArabic) "سجل المصاريف" else "Transactions History",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (uiState.expenses.isEmpty()) {
            item {
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
            items(uiState.expenses) { expense ->
                ExpenseRow(
                    expense = expense,
                    currency = currency,
                    onDelete = { onDeleteExpense(expense) }
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
private fun StatCard(
    title: String,
    amount: String,
    currency: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "$amount $currency",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
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
