package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.*
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.ZoneId

enum class QuickAddTab {
    TASK, EXPENSE, HABIT, GOAL, NOTE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomSheet(
    initialTab: QuickAddTab = QuickAddTab.TASK,
    language: AppLanguage,
    currency: String,
    onDismiss: () -> Unit,
    onNaturalLanguageTaskSubmit: (String) -> Unit,
    onCreateTask: (String, String, Long?, Int?, Int, Priority, TaskCategory, List<String>) -> Unit,
    onCreateExpense: (Double, ExpenseCategory, String) -> Unit,
    onCreateHabit: (String, String, HabitFrequency, String) -> Unit,
    onCreateGoal: (String, String, String, List<String>) -> Unit,
    onCreateNote: (String, String, String, Boolean) -> Unit
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val isArabic = language == AppLanguage.ARABIC

    // Natural Language Input State
    var naturalInput by remember { mutableStateOf("") }

    // Task Form States
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var taskPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var taskCategory by remember { mutableStateOf(TaskCategory.PERSONAL) }
    var taskDurationMinutes by remember { mutableIntStateOf(30) }
    var taskSubtasksText by remember { mutableStateOf("") }

    // Expense Form States
    var expenseAmountText by remember { mutableStateOf("") }
    var expenseCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var expenseNote by remember { mutableStateOf("") }

    // Habit Form States
    var habitTitle by remember { mutableStateOf("") }
    var habitCategory by remember { mutableStateOf("Personal") }
    var habitFrequency by remember { mutableStateOf(HabitFrequency.DAILY) }
    var habitColorHex by remember { mutableStateOf("#3B82F6") }

    // Goal Form States
    var goalTitle by remember { mutableStateOf("") }
    var goalDescription by remember { mutableStateOf("") }
    var goalCategory by remember { mutableStateOf("General") }
    var goalMilestonesText by remember { mutableStateOf("") }

    // Note Form States
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var noteTag by remember { mutableStateOf("") }
    var notePinned by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("quick_add_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title & Tab Chips
            Text(
                text = if (isArabic) "إضافة جديدة" else "Quick Create",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    QuickAddTab.TASK to (if (isArabic) "مهمة" else "Task"),
                    QuickAddTab.EXPENSE to (if (isArabic) "مصروف" else "Expense"),
                    QuickAddTab.HABIT to (if (isArabic) "عادة" else "Habit"),
                    QuickAddTab.GOAL to (if (isArabic) "هدف" else "Goal"),
                    QuickAddTab.NOTE to (if (isArabic) "ملاحظة" else "Note")
                ).forEach { (tab, label) ->
                    val isSelected = selectedTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                QuickAddTab.TASK -> {
                    // Natural Language Quick Input
                    OutlinedTextField(
                        value = naturalInput,
                        onValueChange = { naturalInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("natural_task_input"),
                        label = { Text(if (isArabic) "اكتب باللغة الطبيعية (مثال: دراسة رياضيات غدا الساعة 5)" else "Type naturally (e.g. Study math tomorrow at 5 PM for 45m)") },
                        placeholder = { Text(if (isArabic) "اكتب هنا واضغط إدخال…" else "Type here and press add…") },
                        trailingIcon = {
                            if (naturalInput.isNotBlank()) {
                                IconButton(onClick = {
                                    onNaturalLanguageTaskSubmit(naturalInput)
                                    onDismiss()
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Send,
                                        contentDescription = "Submit",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (naturalInput.isNotBlank()) {
                                onNaturalLanguageTaskSubmit(naturalInput)
                                onDismiss()
                            }
                        }),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text = if (isArabic) " أو بالتفصيل " else " or detailed ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text(if (isArabic) "عنوان المهمة" else "Task Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = taskDescription,
                        onValueChange = { taskDescription = it },
                        label = { Text(if (isArabic) "الوصف (اختياري)" else "Description (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Priority Selector
                    Text(
                        text = if (isArabic) "الأولوية" else "Priority",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Priority.values().forEach { pri ->
                            val isSel = taskPriority == pri
                            val label = when (pri) {
                                Priority.HIGH -> if (isArabic) "عالية" else "High"
                                Priority.MEDIUM -> if (isArabic) "متوسطة" else "Medium"
                                Priority.LOW -> if (isArabic) "منخفضة" else "Low"
                            }
                            FilterChip(
                                selected = isSel,
                                onClick = { taskPriority = pri },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Selector
                    Text(
                        text = if (isArabic) "التصنيف" else "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(TaskCategory.WORK, TaskCategory.STUDY, TaskCategory.PERSONAL, TaskCategory.HEALTH).forEach { cat ->
                            val isSel = taskCategory == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { taskCategory = cat },
                                label = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = taskSubtasksText,
                        onValueChange = { taskSubtasksText = it },
                        label = { Text(if (isArabic) "مهام فرعية (مفصولة بفاصلة أو سطر جديد)" else "Subtasks (separated by commas or new lines)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (taskTitle.isNotBlank()) {
                                val subs = taskSubtasksText.split(",", "\n").map { it.trim() }.filter { it.isNotBlank() }
                                val todayMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                onCreateTask(
                                    taskTitle,
                                    taskDescription,
                                    todayMillis,
                                    null,
                                    taskDurationMinutes,
                                    taskPriority,
                                    taskCategory,
                                    subs
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = taskTitle.isNotBlank()
                    ) {
                        Text(if (isArabic) "حفظ المهمة" else "Save Task")
                    }
                }

                QuickAddTab.EXPENSE -> {
                    OutlinedTextField(
                        value = expenseAmountText,
                        onValueChange = { expenseAmountText = it },
                        label = { Text(if (isArabic) "المبلغ ($currency)" else "Amount ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isArabic) "التصنيف" else "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(ExpenseCategory.FOOD, ExpenseCategory.TRANSPORT, ExpenseCategory.SHOPPING, ExpenseCategory.BILLS).forEach { cat ->
                            val isSel = expenseCategory == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { expenseCategory = cat },
                                label = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = expenseNote,
                        onValueChange = { expenseNote = it },
                        label = { Text(if (isArabic) "ملاحظة (مثال: غداء، تاكسي)" else "Note (e.g. Lunch, Taxi)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val amount = expenseAmountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0.0) {
                                onCreateExpense(amount, expenseCategory, expenseNote)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = (expenseAmountText.toDoubleOrNull() ?: 0.0) > 0.0
                    ) {
                        Text(if (isArabic) "تسجيل المصروف" else "Log Expense")
                    }
                }

                QuickAddTab.HABIT -> {
                    OutlinedTextField(
                        value = habitTitle,
                        onValueChange = { habitTitle = it },
                        label = { Text(if (isArabic) "اسم العادة (مثال: قراءة 15 دقيقة، شرب ماء)" else "Habit Title (e.g. Read 15m, Drink water)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = habitCategory,
                        onValueChange = { habitCategory = it },
                        label = { Text(if (isArabic) "المجال (صحة، دراسة، تطوير ذات)" else "Category (Health, Study, Growth)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (habitTitle.isNotBlank()) {
                                onCreateHabit(habitTitle, habitCategory, habitFrequency, habitColorHex)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = habitTitle.isNotBlank()
                    ) {
                        Text(if (isArabic) "إنشاء العادة" else "Create Habit")
                    }
                }

                QuickAddTab.GOAL -> {
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text(if (isArabic) "عنوان الهدف (مثال: تعلم بايثون)" else "Goal Title (e.g. Learn Python)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = goalDescription,
                        onValueChange = { goalDescription = it },
                        label = { Text(if (isArabic) "لماذا هذا الهدف مهم؟" else "Why is this goal important?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = goalMilestonesText,
                        onValueChange = { goalMilestonesText = it },
                        label = { Text(if (isArabic) "محطات رئيسية (مفصولة بفاصلة أو سطر جديد)" else "Milestones (separated by commas or new lines)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (goalTitle.isNotBlank()) {
                                val milestones = goalMilestonesText.split(",", "\n").map { it.trim() }.filter { it.isNotBlank() }
                                onCreateGoal(goalTitle, goalDescription, goalCategory, milestones)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = goalTitle.isNotBlank()
                    ) {
                        Text(if (isArabic) "إنشاء الهدف" else "Create Goal")
                    }
                }

                QuickAddTab.NOTE -> {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text(if (isArabic) "عنوان الملاحظة" else "Note Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text(if (isArabic) "نص الملاحظة" else "Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = noteTag,
                        onValueChange = { noteTag = it },
                        label = { Text(if (isArabic) "وسم / تصنيف (اختياري)" else "Tag (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (noteTitle.isNotBlank() || noteContent.isNotBlank()) {
                                onCreateNote(noteTitle.ifBlank { "Untitled Note" }, noteContent, noteTag, notePinned)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = noteTitle.isNotBlank() || noteContent.isNotBlank()
                    ) {
                        Text(if (isArabic) "حفظ الملاحظة" else "Save Note")
                    }
                }
            }
        }
    }
}
