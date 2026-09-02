package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.model.FocusSessionEntity
import com.example.data.local.model.FocusTimerMode
import com.example.data.local.model.TaskEntity
import com.example.domain.focus.FocusAudioHelper
import com.example.domain.notifications.SmartNotificationManager
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusSessionSheet(
    initialTask: TaskEntity? = null,
    pendingTasks: List<TaskEntity> = emptyList(),
    focusSessions: List<FocusSessionEntity> = emptyList(),
    todayFocusMinutes: Int = 0,
    isArabic: Boolean = false,
    onDismiss: () -> Unit,
    onSessionCompleted: (FocusSessionEntity, markTaskComplete: Boolean) -> Unit,
    onQuickCreateTask: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val audioHelper = remember { FocusAudioHelper(context) }

    // Active Task state (can be switched by user)
    var currentTask by remember { mutableStateOf(initialTask) }
    var showTaskPicker by remember { mutableStateOf(false) }

    // Navigation between Timer and Statistics
    var activeTab by remember { mutableStateOf(0) } // 0 = Timer, 1 = Statistics
    var isZenMode by remember { mutableStateOf(false) }

    // Timer Mode & Duration (25m Pomodoro / 50m Deep Focus / 5m Break / Custom)
    var selectedMode by remember { mutableStateOf(FocusTimerMode.POMODORO_25) }
    var customMinutes by remember { mutableStateOf(15) }
    var totalSeconds by remember(selectedMode, customMinutes) {
        mutableStateOf(
            when (selectedMode) {
                FocusTimerMode.CUSTOM -> customMinutes * 60
                else -> selectedMode.defaultMinutes * 60
            }
        )
    }
    var remainingSeconds by remember(selectedMode, customMinutes) {
        mutableStateOf(totalSeconds)
    }

    var isRunning by remember { mutableStateOf(false) }
    var selectedSound by remember { mutableStateOf("SILENT") }
    var showCompletedDialog by remember { mutableStateOf(false) }
    var markTaskDoneAfter by remember { mutableStateOf(true) }

    // Release audio on sheet exit
    DisposableEffect(Unit) {
        onDispose {
            audioHelper.release()
        }
    }

    // Handle Ambient Sound according to playback state
    LaunchedEffect(isRunning, selectedSound) {
        if (isRunning && selectedSound != "SILENT") {
            audioHelper.startAmbient(selectedSound, coroutineScope)
        } else {
            audioHelper.stopAmbient()
        }
    }

    // Countdown Loop
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        } else if (isRunning && remainingSeconds == 0) {
            isRunning = false
            audioHelper.stopAmbient()
            audioHelper.playSessionComplete()
            SmartNotificationManager.notifyFocusComplete(
                context = context,
                taskTitle = currentTask?.title,
                durationMinutes = totalSeconds / 60
            )
            showCompletedDialog = true
        }
    }

    // Modal Bottom Sheet Container
    ModalBottomSheet(
        onDismissRequest = {
            if (isRunning) audioHelper.playSessionPause()
            audioHelper.release()
            onDismiss()
        },
        containerColor = Color(0xFF0A111E),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF334155)) },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("focus_session_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isZenMode) 16.dp else 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Timer,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (isArabic) "جلسة التركيز وبومودورو" else "Deep Focus & Pomodoro",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = if (currentTask != null) {
                                if (isArabic) "المهمة: ${currentTask?.title}" else "Target: ${currentTask?.title}"
                            } else {
                                if (isArabic) "تركيز حر بدون مهام" else "Free Focus (No Task Linked)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Zen Mode Toggle
                    IconButton(
                        onClick = { isZenMode = !isZenMode },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isZenMode) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                            contentDescription = "Zen Mode",
                            tint = if (isZenMode) Color(0xFFF59E0B) else Color(0xFF94A3B8)
                        )
                    }

                    // Close
                    IconButton(
                        onClick = {
                            if (isRunning) audioHelper.playSessionPause()
                            audioHelper.release()
                            onDismiss()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }
            }

            // Tabs Selector (Timer vs Statistics) - Hidden in Zen Mode
            if (!isZenMode) {
                Spacer(modifier = Modifier.height(14.dp))
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color(0xFF131F33),
                    contentColor = Color(0xFFF59E0B),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = Color(0xFFF59E0B)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Filled.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(
                                    if (isArabic) "المؤقت" else "Timer",
                                    fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (activeTab == 0) Color(0xFFF8FAFC) else Color(0xFF94A3B8)
                                )
                            }
                        },
                        modifier = Modifier.testTag("focus_tab_timer")
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Filled.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(
                                    if (isArabic) "الإحصائيات والسجل" else "Focus Stats",
                                    fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (activeTab == 1) Color(0xFFF8FAFC) else Color(0xFF94A3B8)
                                )
                                if (todayFocusMinutes > 0) {
                                    Surface(
                                        color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "${todayFocusMinutes}m",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = Color(0xFFF59E0B),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag("focus_tab_stats")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TAB 0: TIMER INTERFACE
            if (activeTab == 0) {
                // Task Integration Card (Allows switching or selecting current task)
                if (!isZenMode) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { if (!isRunning) showTaskPicker = true }
                            .testTag("focus_active_task_card"),
                        color = Color(0xFF131F33),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (currentTask != null) Icons.Filled.TaskAlt else Icons.Outlined.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (currentTask != null) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = currentTask?.title ?: (if (isArabic) "جلسة حرة (بدون مهمة محددة)" else "Free Focus (No task linked)"),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color(0xFFF8FAFC),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val taskSessionsCount = focusSessions.count { it.taskId == currentTask?.id }
                                    val subtitle = if (currentTask != null) {
                                        if (isArabic) {
                                            "الأولوية: ${currentTask?.priority?.name} • $taskSessionsCount جلسات مكتملة"
                                        } else {
                                            "Priority: ${currentTask?.priority?.name} • $taskSessionsCount sessions logged"
                                        }
                                    } else {
                                        if (isArabic) "اضغط لاختيار مهمة من خطتك اليومية" else "Tap to link an existing task"
                                    }
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            if (!isRunning) {
                                OutlinedButton(
                                    onClick = { showTaskPicker = true },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                                ) {
                                    Text(
                                        text = if (currentTask != null) (if (isArabic) "تغيير" else "Switch") else (if (isArabic) "اختيار" else "Select"),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Timer Mode Selector (25m Pomodoro, 50m Deep Focus, 5m Break, Custom)
                if (!isRunning && remainingSeconds == totalSeconds && !isZenMode) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF131F33))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // 25m Pomodoro Chip
                            FocusModeSegment(
                                label = if (isArabic) "بومودورو 25د" else "25m Sprint",
                                isSelected = selectedMode == FocusTimerMode.POMODORO_25,
                                onClick = { selectedMode = FocusTimerMode.POMODORO_25 },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("focus_mode_25")
                            )

                            // 50m Deep Focus Chip
                            FocusModeSegment(
                                label = if (isArabic) "تركيز 50د" else "50m Deep",
                                isSelected = selectedMode == FocusTimerMode.DEEP_50,
                                onClick = { selectedMode = FocusTimerMode.DEEP_50 },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("focus_mode_50")
                            )

                            // 5m Short Break Chip
                            FocusModeSegment(
                                label = if (isArabic) "استراحة 5د" else "5m Break",
                                isSelected = selectedMode == FocusTimerMode.BREAK_5,
                                onClick = { selectedMode = FocusTimerMode.BREAK_5 },
                                modifier = Modifier.weight(1f)
                            )

                            // Custom Chip
                            FocusModeSegment(
                                label = if (isArabic) "مخصص" else "Custom",
                                isSelected = selectedMode == FocusTimerMode.CUSTOM,
                                onClick = { selectedMode = FocusTimerMode.CUSTOM },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Custom Slider
                        if (selectedMode == FocusTimerMode.CUSTOM) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isArabic) "المدة المخصصة: $customMinutes دقيقة" else "Custom Length: $customMinutes mins",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                            Slider(
                                value = customMinutes.toFloat(),
                                onValueChange = { customMinutes = it.toInt() },
                                valueRange = 5f..120f,
                                steps = 22,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFF59E0B),
                                    activeTrackColor = Color(0xFFF59E0B),
                                    inactiveTrackColor = Color(0xFF20324D)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Circular Progress Animated Timer
                val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60

                val activeColor = when (selectedMode) {
                    FocusTimerMode.BREAK_5, FocusTimerMode.BREAK_15 -> Color(0xFF10B981)
                    FocusTimerMode.DEEP_50 -> Color(0xFF38BDF8)
                    else -> Color(0xFFF59E0B)
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(if (isZenMode) 250.dp else 210.dp)
                        .testTag("focus_circular_timer")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 12.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = Size(diameter, diameter)

                        // Outer track
                        drawArc(
                            color = Color(0xFF1A2639),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Progress arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(activeColor.copy(alpha = 0.85f), activeColor)
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Glowing head tip indicator
                        if (progress > 0.01f) {
                            val angleRad = Math.toRadians((-90.0 + (360.0 * progress)))
                            val radius = diameter / 2
                            val centerX = size.width / 2 + (radius * cos(angleRad)).toFloat()
                            val centerY = size.height / 2 + (radius * sin(angleRad)).toFloat()

                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = Offset(centerX, centerY)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = if (isZenMode) 52.sp else 46.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color(0xFFF8FAFC)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when {
                                isRunning && (selectedMode == FocusTimerMode.BREAK_5 || selectedMode == FocusTimerMode.BREAK_15) ->
                                    if (isArabic) "استراحة مريحة ☕" else "Rest & Recharge ☕"
                                isRunning ->
                                    if (isArabic) "تركيز عميق متواصل 🔥" else "Deep Focus Active 🔥"
                                remainingSeconds < totalSeconds ->
                                    if (isArabic) "متوقف مؤقتاً ⏸" else "Paused ⏸"
                                else ->
                                    if (isArabic) "جاهز للبدء ⚡" else "Ready to Focus ⚡"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = if (isRunning) activeColor else Color(0xFF94A3B8)
                        )
                        if (currentTask != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentTask?.title ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8).copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Soundscapes Selector
                if (!isZenMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "الصوت: " else "Sound: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        listOf(
                            "SILENT" to if (isArabic) "صامت 🔇" else "Silent 🔇",
                            "RAIN" to if (isArabic) "مطر 🌧️" else "Rain 🌧️",
                            "WHITE_NOISE" to if (isArabic) "تشويش 🌊" else "Noise 🌊",
                            "BINAURAL" to if (isArabic) "ألفا 🧠" else "Alpha 🧠"
                        ).forEach { (key, label) ->
                            val isSel = selectedSound == key
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) activeColor.copy(alpha = 0.2f) else Color(0xFF131F33))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSel) activeColor else Color(0xFF20324D),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedSound = key }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSel) activeColor else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Controls Row (Reset, Play/Pause, Finish Early)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    if (remainingSeconds < totalSeconds) {
                        OutlinedButton(
                            onClick = {
                                isRunning = false
                                audioHelper.stopAmbient()
                                audioHelper.playSessionPause()
                                remainingSeconds = totalSeconds
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                            modifier = Modifier
                                .height(52.dp)
                                .padding(end = 8.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Reset")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "إعادة" else "Reset")
                        }
                    }

                    // Main Start / Pause Button
                    Button(
                        onClick = {
                            if (isRunning) {
                                isRunning = false
                                audioHelper.playSessionPause()
                            } else {
                                isRunning = true
                                audioHelper.playSessionStart()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .padding(horizontal = 6.dp)
                            .testTag("focus_timer_start_btn")
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Start",
                            tint = Color(0xFF0A111E),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) {
                                if (isArabic) "إيقاف مؤقت" else "Pause Timer"
                            } else if (remainingSeconds < totalSeconds) {
                                if (isArabic) "استئناف" else "Resume"
                            } else {
                                if (isArabic) "ابدأ التركيز" else "Start Focus"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0A111E)
                        )
                    }

                    // Finish Early / Done Button
                    if (remainingSeconds < totalSeconds) {
                        FilledTonalButton(
                            onClick = {
                                isRunning = false
                                audioHelper.stopAmbient()
                                audioHelper.playSessionComplete()
                                showCompletedDialog = true
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF131F33)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .padding(start = 8.dp)
                                .testTag("focus_finish_early_btn")
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = "Finish", tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "إنهاء" else "Finish", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // TAB 1: FOCUS STATISTICS & HISTORY
            if (activeTab == 1 && !isZenMode) {
                FocusStatisticsView(
                    focusSessions = focusSessions,
                    todayFocusMinutes = todayFocusMinutes,
                    isArabic = isArabic
                )
            }
        }
    }

    // TASK PICKER DIALOG
    if (showTaskPicker) {
        TaskPickerSheet(
            pendingTasks = pendingTasks,
            currentTaskId = currentTask?.id,
            isArabic = isArabic,
            onSelectTask = { task ->
                currentTask = task
                showTaskPicker = false
            },
            onQuickAdd = { title ->
                onQuickCreateTask?.invoke(title)
                showTaskPicker = false
            },
            onDismiss = { showTaskPicker = false }
        )
    }

    // POST-SESSION COMPLETION CELEBRATION FEEDBACK DIALOG
    if (showCompletedDialog) {
        val focusedSeconds = (totalSeconds - remainingSeconds).coerceAtLeast(60)
        val focusedMinutes = focusedSeconds / 60

        AlertDialog(
            onDismissRequest = {
                showCompletedDialog = false
                audioHelper.release()
                onDismiss()
            },
            containerColor = Color(0xFF0F1B2C),
            modifier = Modifier.testTag("focus_completion_dialog"),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🎉",
                        fontSize = 28.sp
                    )
                    Column {
                        Text(
                            text = if (isArabic) "أحسنت! اكتملت الجلسة بنجاح" else "Great Work! Session Complete",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = if (isArabic) "جلسة تركيز مثمرة وموثقة" else "Focused Flow Accomplished",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Highlight Stats Box
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1B2A40),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$focusedMinutes min",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF59E0B)
                                    )
                                )
                                Text(
                                    text = if (isArabic) "مدة التركيز" else "Focused Time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Divider(
                                modifier = Modifier
                                    .height(36.dp)
                                    .width(1.dp),
                                color = Color(0xFF334155)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "+${focusedMinutes * 2} XP",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                )
                                Text(
                                    text = if (isArabic) "نقاط إنتاجية" else "Focus XP",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Motivational Feedback
                    val encouragement = when {
                        focusedMinutes >= 45 -> if (isArabic) "طاقة وتركيز أسطوري! حافظت على ثباتك طوال 50 دقيقة." else "Exceptional discipline! 50 minutes of sustained flow state."
                        focusedMinutes >= 20 -> if (isArabic) "إنجاز ممتاز! جلسة بومودورو نموذجية تزيد من إنتاجيتك." else "Solid Pomodoro sprint! High-efficiency focus pays off."
                        else -> if (isArabic) "بداية رائعة! كل دقيقة تركيز تقربك من أهدافك." else "Great focus burst! Small consistent sprints build momentum."
                    }
                    Text(
                        text = encouragement,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )

                    // Task Integration Completion Checkbox
                    if (currentTask != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF131F33),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { markTaskDoneAfter = !markTaskDoneAfter }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = markTaskDoneAfter,
                                    onCheckedChange = { markTaskDoneAfter = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF10B981),
                                        checkmarkColor = Color(0xFF0F1B2C)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isArabic) "تحديد المهمة كمكتملة الآن" else "Mark task as completed",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFF8FAFC)
                                    )
                                    Text(
                                        text = currentTask?.title ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val session = FocusSessionEntity(
                            taskId = currentTask?.id,
                            taskTitle = currentTask?.title,
                            durationMinutes = totalSeconds / 60,
                            actualSeconds = focusedSeconds,
                            mode = selectedMode,
                            isCompleted = true,
                            completedAtMillis = System.currentTimeMillis(),
                            soundMode = selectedSound
                        )
                        showCompletedDialog = false
                        audioHelper.release()
                        onSessionCompleted(session, markTaskDoneAfter)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isArabic) "حفظ الجلسة وإغلاق" else "Save & Complete",
                        color = Color(0xFF0F1B2C),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCompletedDialog = false
                    audioHelper.release()
                    onDismiss()
                }) {
                    Text(if (isArabic) "إغلاق" else "Close", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

// Focus Mode Segment Button
@Composable
private fun FocusModeSegment(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFFF59E0B) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) Color(0xFF0F1B2C) else Color(0xFF94A3B8),
            maxLines = 1
        )
    }
}

// TAB 1: Statistics & History View
@Composable
private fun FocusStatisticsView(
    focusSessions: List<FocusSessionEntity>,
    todayFocusMinutes: Int,
    isArabic: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Daily Focus & Streak Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131F33)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "تركيز اليوم" else "Today's Focus",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "$todayFocusMinutes min",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        )
                        val targetMinutes = 60
                        val percentage = (todayFocusMinutes.toFloat() / targetMinutes * 100).toInt().coerceAtMost(100)
                        Text(
                            text = if (isArabic) "$percentage% من الهدف اليومي (60د)" else "$percentage% of daily 60m goal",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF38BDF8)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isArabic) "إجمالي الجلسات" else "Total Sessions",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "${focusSessions.size}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF8FAFC)
                            )
                        )
                        Text(
                            text = if (isArabic) "جلسات موثقة" else "Tracked Sprints",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // Mode Distribution: 25m vs 50m
        item {
            val pomodoro25Count = focusSessions.count { it.mode == FocusTimerMode.POMODORO_25 }
            val deep50Count = focusSessions.count { it.mode == FocusTimerMode.DEEP_50 }
            val otherCount = focusSessions.size - pomodoro25Count - deep50Count

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131F33)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isArabic) "توزيع أوقات التركيز (25د مقابل 50د)" else "Timer Modes Breakdown",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFF8FAFC)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FocusMetricChip(
                            title = if (isArabic) "25د بومودورو" else "25m Sprints",
                            value = "$pomodoro25Count",
                            tint = Color(0xFFF59E0B)
                        )
                        FocusMetricChip(
                            title = if (isArabic) "50د تركيز عميق" else "50m Deep",
                            value = "$deep50Count",
                            tint = Color(0xFF38BDF8)
                        )
                        FocusMetricChip(
                            title = if (isArabic) "جلسات أخرى" else "Custom/Other",
                            value = "$otherCount",
                            tint = Color(0xFF10B981)
                        )
                    }
                }
            }
        }

        // Top Tasks with Invested Focus Time
        val taskBreakdown = focusSessions
            .filter { !it.taskTitle.isNullOrBlank() }
            .groupBy { it.taskTitle!! }
            .mapValues { (_, list) -> list.sumOf { it.actualSeconds / 60 } }
            .toList()
            .sortedByDescending { it.second }
            .take(4)

        if (taskBreakdown.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131F33)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isArabic) "أبرز المهام حسب وقت التركيز" else "Top Tasks by Focus Time",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF8FAFC)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        taskBreakdown.forEach { (taskName, mins) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                    Text(
                                        text = taskName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFE2E8F0),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 200.dp)
                                    )
                                }
                                Text(
                                    text = "$mins min",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Sessions Log
        item {
            Text(
                text = if (isArabic) "سجل الجلسات الأخيرة" else "Recent Session History",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFF8FAFC)
            )
        }

        if (focusSessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isArabic) "لا توجد جلسات تركيز سابقة بعد" else "No completed focus sessions yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        } else {
            items(focusSessions.take(8)) { session ->
                SessionHistoryRow(session = session, isArabic = isArabic)
            }
        }
    }
}

@Composable
private fun FocusMetricChip(title: String, value: String, tint: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A2639))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = tint)
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
    }
}

@Composable
private fun SessionHistoryRow(session: FocusSessionEntity, isArabic: Boolean) {
    val durationMin = session.actualSeconds / 60
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF131F33),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = when (session.mode) {
                        FocusTimerMode.DEEP_50 -> Color(0xFF38BDF8).copy(alpha = 0.15f)
                        else -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Timer,
                            contentDescription = null,
                            tint = if (session.mode == FocusTimerMode.DEEP_50) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = session.taskTitle ?: (if (isArabic) "جلسة حرة" else "Free Focus"),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFF8FAFC),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (session.mode == FocusTimerMode.DEEP_50) "50m Deep Flow" else "25m Sprint",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Surface(
                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${durationMin.coerceAtLeast(1)}m",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// TASK PICKER SHEET DIALOG
@Composable
private fun TaskPickerSheet(
    pendingTasks: List<TaskEntity>,
    currentTaskId: Long?,
    isArabic: Boolean,
    onSelectTask: (TaskEntity?) -> Unit,
    onQuickAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newTaskTitle by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F1B2C),
        title = {
            Text(
                text = if (isArabic) "ربط مهمة بجلسة التركيز" else "Select Task for Focus",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFF8FAFC)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
            ) {
                // Quick add task inline
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        placeholder = { Text(if (isArabic) "مهمة سريعة جديدة..." else "New quick task...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    IconButton(
                        onClick = {
                            if (newTaskTitle.isNotBlank()) {
                                onQuickAdd(newTaskTitle.trim())
                                newTaskTitle = ""
                            }
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF59E0B))
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color(0xFF0F1B2C))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Free focus unlinked option
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectTask(null) }
                        .padding(vertical = 4.dp),
                    color = if (currentTaskId == null) Color(0xFF1E3A5F) else Color(0xFF131F33),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Spa, contentDescription = null, tint = Color(0xFF38BDF8))
                        Text(
                            text = if (isArabic) "جلسة حرة (بدون ربط مهمة)" else "Free Focus (No task linked)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFFF8FAFC)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List of pending tasks
                if (pendingTasks.isEmpty()) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isArabic) "لا توجد مهام معلقة. يمكنك إضافة مهمة بالأعلى!" else "No pending tasks. Add one above!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(pendingTasks) { task ->
                            val isSel = task.id == currentTaskId
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectTask(task) },
                                color = if (isSel) Color(0xFF1E3A5F) else Color(0xFF131F33),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color(0xFFF8FAFC),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${task.category.name} • ${task.priority.name}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    if (isSel) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

