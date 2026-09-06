package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.AppLanguage
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    language: AppLanguage,
    onComplete: (name: String, priorities: List<String>) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var userName by remember { mutableStateOf("") }
    val selectedPriorities = remember { mutableStateListOf<String>() }

    val isArabic = language == AppLanguage.ARABIC

    val steps = listOf(
        OnboardingStepData(
            title = if (isArabic) "يومك.. منظم بالكامل" else "Your day, organized.",
            subtitle = if (isArabic) "مهامك، جدولك، مصاريفك، عاداتك وأهدافك — في مكان واحد متكامل." else "Tasks, plans, money, habits, and goals — beautifully connected.",
            icon = Icons.Filled.AutoAwesome,
            accentColor = BrandBlue
        ),
        OnboardingStepData(
            title = if (isArabic) "لا تنسَ ما يهمك أبداً" else "Never forget what matters.",
            subtitle = if (isArabic) "سجل مهامك باللغة الطبيعية أو بالخطوات السريعة بنقرة واحدة." else "Capture actions instantly with natural language or smart shortcuts.",
            icon = Icons.Filled.CheckCircle,
            accentColor = SemanticSuccess
        ),
        OnboardingStepData(
            title = if (isArabic) "شاهد يومك بنظرة واحدة" else "See your day at a glance.",
            subtitle = if (isArabic) "خط زمني ذكي يوضح مواعيدك، مهامك، وأوقات فراغك الحقيقية." else "A visual timeline highlighting your events, tasks, and real free time.",
            icon = Icons.Filled.CalendarMonth,
            accentColor = BrandAmber
        ),
        OnboardingStepData(
            title = if (isArabic) "اعرف أين تذهب أموالك" else "Know where your money goes.",
            subtitle = if (isArabic) "تسجيل سريع للمصاريف ومتابعة سهلة للميزانية اليومية والشهرية." else "Fast expense logging and clear daily & monthly budgets.",
            icon = Icons.Filled.AccountBalanceWallet,
            accentColor = ColorFinance
        ),
        OnboardingStepData(
            title = if (isArabic) "إرشاد ذكي في كل لحظة" else "Intelligent daily guidance.",
            subtitle = if (isArabic) "اسأل \"أعمل إيه دلوقتي؟\" وسيقترح عليك يومك الخطوة الأنسب." else "Ask \"What should I do now?\" and let YAWMEK guide your next best step.",
            icon = Icons.Filled.Psychology,
            accentColor = ColorHabit
        ),
        OnboardingStepData(
            title = if (isArabic) "ما هو اسمك الكريم؟" else "What should we call you?",
            subtitle = if (isArabic) "خصص مساحتك الشخصية وحدد أولوياتك الأساسية." else "Personalize your workspace and choose your current focus.",
            icon = Icons.Filled.Person,
            accentColor = BrandBlue
        )
    )

    val currentData = steps[currentStep]

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Step indicators and Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Indicator dots (No Skip, Mandatory Setup)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    steps.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (index == currentStep) 24.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (index == currentStep)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Center Content: Hero Visual & Text
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                            scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                        .togetherWith(fadeOut(animationSpec = tween(90)))
                },
                label = "onboarding_step"
            ) { step ->
                val data = steps[step]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Visual Emblem
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(data.accentColor.copy(alpha = 0.12f))
                            .border(2.dp, data.accentColor.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = data.icon,
                            contentDescription = null,
                            tint = data.accentColor,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = data.subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    // If final step: Name input & priority categories
                    if (step == steps.size - 1) {
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text(if (isArabic) "اسمك الكريم (مطلوب)" else "Your Name (Required)") },
                            placeholder = { Text(if (isArabic) "مثال: أحمد، سارة، محمد" else "e.g. Alex, Sarah, Omar") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_name_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            isError = userName.isNotBlank() && userName.trim().length < 2,
                            supportingText = {
                                if (userName.trim().length >= 2) {
                                    Text(
                                        text = if (isArabic) "يسعدنا انضمامك يا ${userName.trim()}! ✨" else "Nice to meet you, ${userName.trim()}! ✨",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                } else {
                                    Text(
                                        text = if (isArabic) "يرجى كتابة اسمك للمتابعة" else "Please enter your name to continue",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isArabic) "ما هي مجالات تركيزك الحالية؟" else "What matters most to you right now?",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val focusCategories = if (isArabic) {
                            listOf("الإنتاجية", "الدراسة", "العمل", "الصحة والرياضة", "المصاريف", "العادات", "الأهداف الشخصية")
                        } else {
                            listOf("Productivity", "Study", "Work", "Health & Fitness", "Money", "Habits", "Personal Goals")
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            focusCategories.forEach { category ->
                                val isSelected = selectedPriorities.contains(category)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) selectedPriorities.remove(category)
                                        else selectedPriorities.add(category)
                                    },
                                    label = { Text(category) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val isContinueEnabled = if (currentStep == steps.size - 1) {
                userName.trim().length >= 2
            } else {
                true
            }

            // Bottom Navigation Button
            Button(
                onClick = {
                    if (currentStep < steps.size - 1) {
                        currentStep++
                    } else {
                        onComplete(userName.trim(), selectedPriorities.toList())
                    }
                },
                enabled = isContinueEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_next_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (currentStep == 0) {
                        if (isArabic) "ابدأ الآن" else "Get Started"
                    } else if (currentStep == steps.size - 1) {
                        if (isArabic) "لنبدأ ببناء يومك 🚀" else "Let's Build Your Day 🚀"
                    } else {
                        if (isArabic) "التالي" else "Next"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

private data class OnboardingStepData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color
)
