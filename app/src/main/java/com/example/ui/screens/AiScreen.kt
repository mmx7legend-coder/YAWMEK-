package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.AppLanguage
import com.example.data.remote.AiDailyPlanItem
import com.example.domain.ai.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.YawmekUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScreen(
    uiState: YawmekUiState,
    onSendPrompt: (String) -> Unit,
    onApplyPlan: (List<AiDailyPlanItem>) -> Unit,
    onApplyGoalRoadmap: (GoalRoadmapProposal) -> Unit = {},
    onApplyReschedule: (RescheduleProposal) -> Unit = {},
    onApplyHabitStack: (String, String) -> Unit = { _, _ -> },
    onStartFocus: (String, Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val language = uiState.userSettings.language
    val isArabic = language == AppLanguage.ARABIC
    val listState = rememberLazyListState()
    var showMemorySheet by remember { mutableStateOf(false) }
    var memories by remember { mutableStateOf(AiMemoryManager.getAllMemories(context)) }

    val quickPrompts = if (isArabic) {
        listOf(
            "خططلي يومي 🗓️",
            "إيه أهم حاجة أعملها دلوقتي؟ 🎯",
            "عايز أتعلم بايثون في 60 يوم 🚀",
            "حلل إنتاجيتي الأسبوع ده 📊",
            "أعد جدولة المهام المتأخرة ⏱️",
            "ليه أنا متأخر؟ 🔍"
        )
    } else {
        listOf(
            "Plan my day 🗓️",
            "What should I do now? 🎯",
            "I want to learn Python in 60 days 🚀",
            "Analyze my productivity 📊",
            "Reschedule my missed tasks ⏱️",
            "Why am I falling behind? 🔍"
        )
    }

    LaunchedEffect(uiState.chatMessages.size, uiState.isAiThinking) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(BrandAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = BrandAmber,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isArabic) "محرك ذكاء يومك" else "YAWMEK Intelligence",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isArabic) "عقل تخطيطي وتحليلي للإنتاجية" else "Context-Aware Productivity Engine",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // AI Memory / Preferences button
            IconButton(
                onClick = {
                    memories = AiMemoryManager.getAllMemories(context)
                    showMemorySheet = true
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Outlined.Psychology,
                    contentDescription = "AI Memory",
                    tint = ColorStudy,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Messages List or Starter View
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (uiState.chatMessages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            BrandAmber.copy(alpha = 0.25f),
                                            ColorStudy.copy(alpha = 0.15f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Psychology,
                                contentDescription = null,
                                tint = BrandAmber,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isArabic) "مرحباً بك في محرك إنتاجيتك الشخصي" else "Personal Productivity Intelligence",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isArabic)
                                "أنا متصل مباشرة بمهامك (${uiState.tasks.count { !it.isCompleted }} معلقة)، عاداتك (${uiState.habits.size})، وتقويمك. اطلب مني تخطيط يومك، تحليل إنتاجيتك، أو تفكيك أي هدف معقد إلى خطوات عملية."
                            else
                                "Connected live to your ${uiState.tasks.count { !it.isCompleted }} pending tasks, ${uiState.habits.size} habits, and schedule. Ask me to plan your day, analyze productivity patterns, or build full goal roadmaps.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 19.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            } else {
                items(uiState.chatMessages) { msg ->
                    ChatBubble(
                        message = msg,
                        isArabic = isArabic,
                        onApplyPlan = onApplyPlan,
                        onApplyGoalRoadmap = onApplyGoalRoadmap,
                        onApplyReschedule = onApplyReschedule,
                        onApplyHabitStack = onApplyHabitStack,
                        onStartFocus = onStartFocus
                    )
                }
            }

            if (uiState.isAiThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = BrandAmber)
                        Text(
                            text = if (isArabic) "محرك يومك يحلل الأولويات والبيانات…" else "YAWMEK AI is analyzing your workspace…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                SuggestionChip(
                    onClick = { onSendPrompt(prompt) },
                    label = { Text(prompt, style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(12.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                )
            }
        }

        // Input Field Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text(if (isArabic) "اكتب رسالة أو أمر (عربي أو إنجليزي)…" else "Ask YAWMEK AI or give a command…") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotBlank()) {
                            onSendPrompt(inputText)
                            inputText = ""
                        }
                    }),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendPrompt(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // AI Intelligence Memory Sheet (Requirement 11)
    if (showMemorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showMemorySheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "ذاكرة ذكاء يومك" else "YAWMEK AI Memory",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isArabic) "التفضيلات والأنماط المستخلصة من نشاطك" else "Learned preferences & working patterns",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (memories.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                AiMemoryManager.clearAllMemories(context)
                                memories = emptyList()
                            }
                        ) {
                            Text(if (isArabic) "مسح الكل" else "Clear All", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (memories.isEmpty()) {
                    Text(
                        text = if (isArabic) "لا توجد تفضيلات مخزنة حالياً." else "No remembered preferences yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    memories.forEach { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.key,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = item.value,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = item.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        AiMemoryManager.deleteMemory(context, item.id)
                                        memories = AiMemoryManager.getAllMemories(context)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isArabic: Boolean,
    onApplyPlan: (List<AiDailyPlanItem>) -> Unit,
    onApplyGoalRoadmap: (GoalRoadmapProposal) -> Unit,
    onApplyReschedule: (RescheduleProposal) -> Unit,
    onApplyHabitStack: (String, String) -> Unit,
    onStartFocus: (String, Int) -> Unit
) {
    val isUser = message.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            ),
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Plan Health Badge if present
                if (message.planHealth != null) {
                    val healthColor: Color
                    val healthText: String
                    when (message.planHealth) {
                        PlanHealthStatus.HEALTHY -> {
                            healthColor = SemanticSuccess
                            healthText = if (isArabic) "حالة الخطة: ممتازة وفي المسار" else "Plan Health: HEALTHY"
                        }
                        PlanHealthStatus.AT_RISK -> {
                            healthColor = BrandAmber
                            healthText = if (isArabic) "حالة الخطة: تحتاج انتباه" else "Plan Health: AT RISK"
                        }
                        PlanHealthStatus.BEHIND -> {
                            healthColor = Color(0xFFF97316)
                            healthText = if (isArabic) "حالة الخطة: متأخرة" else "Plan Health: BEHIND"
                        }
                        PlanHealthStatus.CRITICAL -> {
                            healthColor = MaterialTheme.colorScheme.error
                            healthText = if (isArabic) "حالة الخطة: حرجة" else "Plan Health: CRITICAL"
                        }
                        PlanHealthStatus.COMPLETED -> {
                            healthColor = BrandBlue
                            healthText = if (isArabic) "حالة الخطة: مكتملة" else "Plan Health: COMPLETED"
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = healthColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = healthText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = healthColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Main Message Text
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 21.sp
                )

                // 1. If message has structured plan items
                if (message.planItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isArabic) "الجدول المقترح:" else "Proposed Daily Schedule:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    message.planItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.timeFormatted} • ${item.title}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "⏱ ${item.durationMinutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onApplyPlan(message.planItems) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "تطبيق على جدول اليوم" else "Apply to Today's Schedule")
                    }
                }

                // 2. If message has Structured Action
                when (val action = message.structuredAction) {
                    is StructuredAiAction.GoalRoadmapAction -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isArabic) "🎯 تطبيق خارطة الطريق:" else "🎯 Roadmap Ready to Deploy:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ColorStudy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${action.roadmap.phases.size} Phases • ${action.roadmap.milestones.size} Milestones • ${action.roadmap.tasks.size} Starter Tasks",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onApplyGoalRoadmap(action.roadmap) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorStudy),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.AddRoad, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isArabic) "إنشاء الهدف والمراحل والمهام" else "Deploy Roadmap to Workspace")
                                }
                            }
                        }
                    }

                    is StructuredAiAction.RescheduleAction -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isArabic) "⏱️ مقترح إعادة الجدولة:" else "⏱️ Reschedule Distribution:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BrandAmber
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${action.proposal.items.size} tasks to redistribute cleanly across free slots.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onApplyReschedule(action.proposal) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandAmber),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isArabic) "تنفيذ إعادة الجدولة الآن" else "Execute Rescheduling", color = Color.Black)
                                }
                            }
                        }
                    }

                    is StructuredAiAction.FocusSessionAction -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onStartFocus(action.taskTitle, action.durationMinutes) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "بدء جلسة تركيز (${action.durationMinutes} د)" else "Start Focus Session (${action.durationMinutes}m)")
                        }
                    }

                    is StructuredAiAction.HabitStackAction -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onApplyHabitStack(action.newHabitTitle, "Personal") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SemanticSuccess),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Checklist, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "إضافة العادة إلى متتبع العادات" else "Add Habit to Tracker")
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}
