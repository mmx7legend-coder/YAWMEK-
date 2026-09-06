package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.AppLanguage
import com.example.data.local.model.ThemeMode
import com.example.domain.audio.SoundHapticManager
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.SemanticError
import com.example.ui.viewmodel.YawmekUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    uiState: YawmekUiState,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onResetAllData: () -> Unit,
    onOpenAccountSync: () -> Unit = {},
    onUpdateNotificationPreferences: (Boolean, Boolean, Boolean, Boolean) -> Unit = { _, _, _, _ -> },
    onUpdateUserName: (String) -> Unit = {}
) {
    val language = uiState.userSettings.language
    val isArabic = language == AppLanguage.ARABIC
    val userName = uiState.userSettings.userName.ifBlank { if (isArabic) "صديقي" else "User" }
    var showResetDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editingName by remember(userName) { mutableStateOf(userName) }

    val currencies = listOf("EGP", "USD", "EUR", "SAR", "AED", "KWD", "QAR", "GBP")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Header
        item {
            Text(
                text = if (isArabic) "الحساب والإعدادات" else "Profile & Settings",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Profile Avatar Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(BrandBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BrandBlue
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isArabic) "مساحتك اليومية في يومك" else "Personal YAWMEK Workspace",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            editingName = userName
                            showEditNameDialog = true
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = if (isArabic) "تعديل الاسم" else "Edit Name",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Account & Cloud Sync Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B2C)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.CloudSync, contentDescription = null, tint = Color(0xFF38BDF8))
                            Column {
                                Text(
                                    text = if (isArabic) "المزامنة السحابية والحساب" else "Cloud Sync & Account",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFF8FAFC)
                                )
                                val statusText = if (uiState.activeAccount?.isLoggedIn == true) {
                                    uiState.activeAccount?.email ?: "Synced"
                                } else {
                                    if (isArabic) "وضع محلي بدون حساب" else "Guest Mode (Offline-first)"
                                }
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Button(
                            onClick = onOpenAccountSync,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                        ) {
                            Text(
                                text = if (uiState.activeAccount?.isLoggedIn == true) (if (isArabic) "إدارة" else "Manage") else (if (isArabic) "تسجيل" else "Sign In"),
                                color = Color(0xFF0F172A),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Focus Mode Performance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Timer, contentDescription = null, tint = BrandAmber)
                        Text(
                            text = if (isArabic) "التركيز والإنتاجية" else "Deep Focus Productivity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isArabic) "دقائق التركيز اليوم" else "Today's Focus Time", style = MaterialTheme.typography.bodyMedium)
                        Text("${uiState.todayFocusMinutes} min", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = BrandAmber))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isArabic) "إجمالي جلسات التركيز" else "Total Focus Sessions", style = MaterialTheme.typography.bodyMedium)
                        Text("${uiState.focusSessions.size}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Notification Preferences
        item {
            var taskReminders by remember { mutableStateOf(true) }
            var budgetWarnings by remember { mutableStateOf(true) }
            var habitAlerts by remember { mutableStateOf(true) }
            var quietHours by remember { mutableStateOf(true) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = BrandAmber)
                        Text(
                            text = if (isArabic) "إعدادات الإشعارات الذكية" else "Smart Notifications",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isArabic) "تنبيهات المهام المقترحة" else "Task Reminders", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = taskReminders,
                            onCheckedChange = {
                                taskReminders = it
                                onUpdateNotificationPreferences(quietHours, taskReminders, budgetWarnings, habitAlerts)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isArabic) "تحذيرات الميزانية (80%)" else "Budget Warnings (80%)", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = budgetWarnings,
                            onCheckedChange = {
                                budgetWarnings = it
                                onUpdateNotificationPreferences(quietHours, taskReminders, budgetWarnings, habitAlerts)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isArabic) "تذكيرات العادات اليومية" else "Daily Habit Reminders", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = habitAlerts,
                            onCheckedChange = {
                                habitAlerts = it
                                onUpdateNotificationPreferences(quietHours, taskReminders, budgetWarnings, habitAlerts)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isArabic) "وضع الهدوء الليلي (10م - 7ص)" else "Quiet Hours (10PM - 7AM)", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = quietHours,
                            onCheckedChange = {
                                quietHours = it
                                onUpdateNotificationPreferences(quietHours, taskReminders, budgetWarnings, habitAlerts)
                            }
                        )
                    }
                }
            }
        }

        // Language Setting
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Language, contentDescription = null, tint = BrandBlue)
                        Text(
                            text = if (isArabic) "لغة التطبيق" else "App Language",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = language == AppLanguage.ENGLISH,
                            onClick = { onLanguageChange(AppLanguage.ENGLISH) },
                            label = { Text("English") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = language == AppLanguage.ARABIC,
                            onClick = { onLanguageChange(AppLanguage.ARABIC) },
                            label = { Text("العربية") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Theme Mode Setting
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.DarkMode, contentDescription = null, tint = BrandAmber)
                        Text(
                            text = if (isArabic) "المظهر" else "Theme Mode",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.values().forEach { mode ->
                            val isSel = uiState.userSettings.themeMode == mode
                            val label = when (mode) {
                                ThemeMode.SYSTEM -> if (isArabic) "تلقائي" else "System"
                                ThemeMode.LIGHT -> if (isArabic) "فاتح" else "Light"
                                ThemeMode.DARK -> if (isArabic) "داكن" else "Dark"
                            }
                            FilterChip(
                                selected = isSel,
                                onClick = { onThemeChange(mode) },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Sound & Haptic Feedback Setting
        item {
            val context = LocalContext.current
            val soundManager = remember { SoundHapticManager.getInstance(context) }
            var isSoundEnabled by remember { mutableStateOf(soundManager.isSoundEnabled) }
            var isHapticsEnabled by remember { mutableStateOf(soundManager.isHapticsEnabled) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = BrandAmber)
                        Text(
                            text = if (isArabic) "الأصوات والتفاعل اللمسي" else "Sound & Haptic Feedback",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "المؤثرات الصوتية الدقيقة" else "Subtle Sound Effects",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = if (isArabic) "نغمات هادئة عند إنجاز المهام والعادات" else "Calm feedback when finishing tasks & habits",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isSoundEnabled,
                            onCheckedChange = {
                                isSoundEnabled = it
                                soundManager.isSoundEnabled = it
                                if (it) soundManager.playTaskCompleted()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "الاهتزاز اللمسي (Haptics)" else "Haptic Feedback",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = if (isArabic) "نبضات لمسية خفيفة مع كل نقرة وإنجاز" else "Gentle tactile pulse on taps & achievements",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isHapticsEnabled,
                            onCheckedChange = {
                                isHapticsEnabled = it
                                soundManager.isHapticsEnabled = it
                                if (it) soundManager.playTaskCompleted()
                            }
                        )
                    }
                }
            }
        }

        // Currency Setting
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.AttachMoney, contentDescription = null, tint = BrandBlue)
                        Text(
                            text = if (isArabic) "العملة الافتراضية" else "Default Currency",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencies.forEach { curr ->
                            val isSel = uiState.userSettings.currency == curr
                            FilterChip(
                                selected = isSel,
                                onClick = { onCurrencyChange(curr) },
                                label = { Text(curr) }
                            )
                        }
                    }
                }
            }
        }

        // Workspace Statistics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "إحصائيات مساحتك" else "Workspace Summary",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isArabic) "إجمالي المهام" else "Total Tasks", style = MaterialTheme.typography.bodyMedium)
                        Text("${uiState.tasks.size}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isArabic) "العادات النشطة" else "Active Habits", style = MaterialTheme.typography.bodyMedium)
                        Text("${uiState.habits.size}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isArabic) "الأهداف" else "Goals", style = MaterialTheme.typography.bodyMedium)
                        Text("${uiState.goals.size}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Reset Data Action
        item {
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SemanticError)
            ) {
                Icon(Icons.Filled.DeleteSweep, contentDescription = null, tint = SemanticError)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isArabic) "إعادة تعيين مساحة العمل بالكامل" else "Reset Workspace to Clean State")
            }
        }
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text(if (isArabic) "تعديل اسمك" else "Edit Your Name") },
            text = {
                Column {
                    Text(
                        text = if (isArabic) "اكتب الاسم الذي تود أن يناديك به التطبيق:" else "Enter the name you'd like YAWMEK to call you:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editingName,
                        onValueChange = { editingName = it },
                        singleLine = true,
                        placeholder = { Text(if (isArabic) "اسمك الكريم" else "Your name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = editingName.trim()
                        if (trimmed.isNotBlank()) {
                            onUpdateUserName(trimmed)
                            showEditNameDialog = false
                        }
                    },
                    enabled = editingName.isNotBlank()
                ) {
                    Text(if (isArabic) "حفظ التغيير" else "Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(if (isArabic) "تأكيد إعادة التعيين؟" else "Reset Workspace?") },
            text = {
                Text(
                    if (isArabic)
                        "سيتم مسح جميع المهام، المصاريف، العادات، والأهداف لتبدأ بصفحة نظيفة تماماً. لا يمكن التراجع عن هذه الخطوة."
                    else
                        "This will permanently delete all tasks, expenses, habits, goals, and notes to return to a fresh clean slate."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticError)
                ) {
                    Text(if (isArabic) "نعم، ابدأ من جديد" else "Yes, Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }
}
