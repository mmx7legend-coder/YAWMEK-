package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.local.model.AppLanguage
import com.example.ui.theme.*
import com.example.ui.viewmodel.GlobalSearchResults

@Composable
fun GlobalSearchDialog(
    query: String,
    results: GlobalSearchResults,
    language: AppLanguage,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isArabic = language == AppLanguage.ARABIC

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .testTag("global_search_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(if (isArabic) "ابحث في المهام، الملاحظات، المصاريف…" else "Search tasks, notes, expenses…") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                val hasAnyResults = results.tasks.isNotEmpty() || results.expenses.isNotEmpty() ||
                        results.habits.isNotEmpty() || results.goals.isNotEmpty() || results.notes.isNotEmpty()

                if (query.isBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isArabic) "اكتب للبحث في جميع عناصر يومك" else "Type to search across all your YAWMEK items",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (!hasAnyResults) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isArabic) "لا توجد نتائج مطابقة لـ \"$query\"" else "No matches found for \"$query\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (results.tasks.isNotEmpty()) {
                            item {
                                SectionHeader(if (isArabic) "المهام" else "Tasks", BrandBlue)
                            }
                            items(results.tasks) { task ->
                                SearchResultRow(
                                    icon = Icons.Filled.CheckCircle,
                                    iconTint = BrandBlue,
                                    title = task.title,
                                    subtitle = if (task.isCompleted) "Completed" else "Pending"
                                )
                            }
                        }

                        if (results.expenses.isNotEmpty()) {
                            item {
                                SectionHeader(if (isArabic) "المصاريف" else "Expenses", ColorFinance)
                            }
                            items(results.expenses) { exp ->
                                SearchResultRow(
                                    icon = Icons.Filled.AccountBalanceWallet,
                                    iconTint = ColorFinance,
                                    title = "${exp.amount} ${exp.currency} - ${exp.category.name}",
                                    subtitle = exp.note.ifBlank { "Expense" }
                                )
                            }
                        }

                        if (results.habits.isNotEmpty()) {
                            item {
                                SectionHeader(if (isArabic) "العادات" else "Habits", ColorHabit)
                            }
                            items(results.habits) { hab ->
                                SearchResultRow(
                                    icon = Icons.Filled.Repeat,
                                    iconTint = ColorHabit,
                                    title = hab.title,
                                    subtitle = hab.category
                                )
                            }
                        }

                        if (results.goals.isNotEmpty()) {
                            item {
                                SectionHeader(if (isArabic) "الأهداف" else "Goals", ColorGoal)
                            }
                            items(results.goals) { goal ->
                                SearchResultRow(
                                    icon = Icons.Filled.Flag,
                                    iconTint = ColorGoal,
                                    title = goal.title,
                                    subtitle = goal.category
                                )
                            }
                        }

                        if (results.notes.isNotEmpty()) {
                            item {
                                SectionHeader(if (isArabic) "الملاحظات" else "Notes", BrandAmber)
                            }
                            items(results.notes) { note ->
                                SearchResultRow(
                                    icon = Icons.Filled.Description,
                                    iconTint = BrandAmber,
                                    title = note.title,
                                    subtitle = note.content.take(40)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = color,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SearchResultRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
