package com.example.ui.screens.rpg

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.*
import com.example.domain.rpg.RpgProgressionCalculator
import com.example.ui.viewmodel.YawmekUiState

enum class AdventureTab(val titleEn: String, val titleAr: String) {
    HERO("Hero & Stats", "البطل والقدرات"),
    MAP("World Map", "خريطة العالم"),
    QUESTS("Quests", "المهمات اليومية"),
    STORE("Store", "متجر العتاد"),
    SKILLS("Skill Tree", "شجرة المهارات"),
    BOSSES("Boss Battle", "زعماء التحدي")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventureScreen(
    uiState: YawmekUiState,
    isArabic: Boolean,
    onBackToHome: () -> Unit,
    onClaimQuest: (RpgQuestEntity) -> Unit,
    onPurchaseEquipment: (EquipmentItemEntity) -> Unit,
    onEquipItem: (EquipmentItemEntity) -> Unit,
    onUnlockSkill: (SkillNodeEntity) -> Unit,
    onDamageBoss: (bossId: Long, damage: Int) -> Unit,
    onCreateBoss: (nameEn: String, nameAr: String, hp: Int, rewardXp: Long, goalId: Long?) -> Unit
) {
    var selectedTab by remember { mutableStateOf(AdventureTab.HERO) }
    val character = uiState.rpgCharacter

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isArabic) "مغامرة يومك (RPG)" else "YAWMEK Adventure (RPG)",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFB300)
                        ) {
                            Text(
                                text = "Lv.${character.level}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1E1B4B),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToHome) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${character.currentXp} XP",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                AdventureTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = if (isArabic) tab.titleAr else tab.titleEn,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                AdventureTab.HERO -> HeroOverviewTab(
                    character = character,
                    equipment = uiState.rpgEquipment,
                    isArabic = isArabic,
                    onEquipItem = onEquipItem
                )
                AdventureTab.MAP -> AdventureWorldMapTab(
                    character = character,
                    isArabic = isArabic
                )
                AdventureTab.QUESTS -> QuestsTab(
                    quests = uiState.rpgQuests,
                    isArabic = isArabic,
                    onClaimQuest = onClaimQuest
                )
                AdventureTab.STORE -> AdventureStoreTab(
                    character = character,
                    equipment = uiState.rpgEquipment,
                    isArabic = isArabic,
                    onPurchase = onPurchaseEquipment,
                    onEquip = onEquipItem
                )
                AdventureTab.SKILLS -> SkillTreeTab(
                    character = character,
                    skills = uiState.rpgSkills,
                    isArabic = isArabic,
                    onUnlockSkill = onUnlockSkill
                )
                AdventureTab.BOSSES -> BossBattleTab(
                    bosses = uiState.rpgBosses,
                    goals = uiState.goals,
                    isArabic = isArabic,
                    onDamageBoss = onDamageBoss,
                    onCreateBoss = onCreateBoss
                )
            }
        }
    }
}

@Composable
private fun HeroOverviewTab(
    character: CharacterRpgEntity,
    equipment: List<EquipmentItemEntity>,
    isArabic: Boolean,
    onEquipItem: (EquipmentItemEntity) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_idle")
    val idleScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_avatar_idle"
    )

    val xpNeeded = RpgProgressionCalculator.getXpRequiredForNextLevel(character.level)
    val xpRatio = (character.currentXp.toFloat() / xpNeeded.coerceAtLeast(1L)).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(idleScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFFFD54F), Color(0xFF1E3A8A), Color(0xFF0F172A))
                                )
                            )
                            .border(3.dp, Color(0xFFFFC107), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = character.avatarSkin, fontSize = 52.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isArabic) character.titleAr else character.titleEn,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = if (isArabic) "المستوى ${character.level} • متتالية إنتاج: ${character.productiveStreakDays} أيام"
                        else "Level ${character.level} • ${character.productiveStreakDays}-Day Productivity Streak",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "التقدم للمستوى التالي" else "Next Level Progress",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${character.currentXp} / $xpNeeded XP",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { xpRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFFFB300),
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF312E81).copy(alpha = 0.4f),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = character.dragonStage.iconEmoji, fontSize = 24.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "التنين الأسطوري: ${character.dragonStage.titleAr}"
                                    else "Legendary Dragon: ${character.dragonStage.titleEn}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isArabic) character.dragonStage.descriptionAr else character.dragonStage.descriptionEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = if (isArabic) "إحصائيات الشخصية الحقيقية" else "Real Productivity Stats",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatMetricCard(
                        title = if (isArabic) "التركيز (Focus)" else "Focus",
                        desc = if (isArabic) "جلسات بومودورو" else "Deep Pomodoro Work",
                        value = character.statFocus,
                        emoji = "🎯",
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = if (isArabic) "الانضباط (Discipline)" else "Discipline",
                        desc = if (isArabic) "إنجاز المهام الصعبة" else "Completing Tough Tasks",
                        value = character.statDiscipline,
                        emoji = "🛡️",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatMetricCard(
                        title = if (isArabic) "المعرفة (Knowledge)" else "Knowledge",
                        desc = if (isArabic) "جلسات القراءة والتعلم" else "Reading & Notes",
                        value = character.statKnowledge,
                        emoji = "📖",
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = if (isArabic) "الاستمرارية (Consistency)" else "Consistency",
                        desc = if (isArabic) "تثبيت العادات اليومية" else "Daily Habit Streaks",
                        value = character.statConsistency,
                        emoji = "🔥",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatMetricCard(
                        title = if (isArabic) "التخطيط (Planning)" else "Planning",
                        desc = if (isArabic) "الأهداف وجدول اليوم" else "Goals & Planning",
                        value = character.statPlanning,
                        emoji = "🗺️",
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = if (isArabic) "الشجاعة (Courage)" else "Courage",
                        desc = if (isArabic) "المهام ذات الأولوية العالية" else "High-Priority Quests",
                        value = character.statCourage,
                        emoji = "⚔️",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Text(
                text = if (isArabic) "العتاد المجهّز" else "Equipped Loadout",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            val purchasedGear = equipment.filter { it.isPurchased }
            if (purchasedGear.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isArabic) "لم تجهز أي عتاد بعد. زر المتجر لشراء العتاد بنقاط الخبرة الحقيقية!"
                        else "No equipped gear yet. Visit the store to unlock gear using productivity XP!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    purchasedGear.forEach { item ->
                        val isEquipped = when (item.slot) {
                            EquipmentSlot.WEAPON -> character.equippedWeaponId == item.id
                            EquipmentSlot.ARMOR -> character.equippedArmorId == item.id
                            EquipmentSlot.HELMET -> character.equippedHelmetId == item.id
                            EquipmentSlot.GLOVES -> character.equippedGlovesId == item.id
                            EquipmentSlot.BOOTS -> character.equippedBootsId == item.id
                            EquipmentSlot.CAPE -> character.equippedCapeId == item.id
                            EquipmentSlot.ACCESSORY -> character.equippedAccessoryId == item.id
                            EquipmentSlot.FRAME -> character.equippedFrameId == item.id
                            EquipmentSlot.BACKGROUND -> character.equippedBackgroundId == item.id
                        }
                        EquipmentItemRow(
                            item = item,
                            isEquipped = isEquipped,
                            isArabic = isArabic,
                            onEquip = { onEquipItem(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    desc: String,
    value: Int,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$value",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AdventureWorldMapTab(
    character: CharacterRpgEntity,
    isArabic: Boolean
) {
    val allAreas = AdventureArea.values()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = if (isArabic) "خريطة تقدم العالم (تعتمد على إنجازاتك الحقيقية)"
                else "World Adventure Map (Driven by Real Life Milestones)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isArabic) "كل منطقة تتطلب مستويات وتثبيت عادات حقيقية لفتحها"
                else "Each area unlocks strictly as you maintain productivity levels and habits.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(allAreas) { area ->
            val isUnlocked = character.level >= area.unlockLevel
            val isCurrent = character.currentAdventureArea == area

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    else if (isUnlocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                border = if (isCurrent) CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFFFFB300), MaterialTheme.colorScheme.primary))
                ) else CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isUnlocked) area.iconEmoji else "🔒",
                            fontSize = 24.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isArabic) area.titleAr else area.titleEn,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            if (isCurrent) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFFB300)
                                ) {
                                    Text(
                                        text = if (isArabic) "المنطقة الحالية" else "Current",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF1E1B4B),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isArabic) area.descriptionAr else area.descriptionEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isUnlocked) (if (isArabic) "متاحة ✓" else "Unlocked ✓")
                            else (if (isArabic) "تتطلب المستوى ${area.unlockLevel}" else "Requires Level ${area.unlockLevel}"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isUnlocked) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestsTab(
    quests: List<RpgQuestEntity>,
    isArabic: Boolean,
    onClaimQuest: (RpgQuestEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = if (isArabic) "المهمات اليومية والأسبوعية" else "Daily & Weekly RPG Quests",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isArabic) "تكتمل تلقائياً عند قيامك بأعمال إنتاجية حقيقية في التطبيق"
                else "Completed automatically as you work on tasks, focus, and habits.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(quests) { quest ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (quest.isCompleted && !quest.isClaimed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) quest.titleAr else quest.titleEn,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isArabic) quest.descriptionAr else quest.descriptionEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFB300).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "+${quest.rewardXp} XP",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFFB300),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val progressRatio = (quest.currentCount.toFloat() / quest.targetCount.coerceAtLeast(1)).coerceIn(0f, 1f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${quest.currentCount} / ${quest.targetCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (quest.isClaimed) {
                            Text(
                                text = if (isArabic) "تم الاستلام ✓" else "Claimed ✓",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFFFB300),
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    if (quest.isCompleted && !quest.isClaimed) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onClaimQuest(quest) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                        ) {
                            Text(
                                text = if (isArabic) "استلم الجائزة ⭐" else "Claim Reward ⭐",
                                color = Color(0xFF1E1B4B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdventureStoreTab(
    character: CharacterRpgEntity,
    equipment: List<EquipmentItemEntity>,
    isArabic: Boolean,
    onPurchase: (EquipmentItemEntity) -> Unit,
    onEquip: (EquipmentItemEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = if (isArabic) "متجر العتاد والمظهر" else "Gear & Appearance Store",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isArabic) "أنفق نقاط الخبرة الإنتاجية لتجهيز بطل يومك"
                else "Spend hard-earned productivity XP to upgrade your hero's armor and weapons.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(equipment) { item ->
            val canAfford = character.currentXp >= item.xpStoreCost && character.level >= item.requiredLevel
            val isEquipped = when (item.slot) {
                EquipmentSlot.WEAPON -> character.equippedWeaponId == item.id
                EquipmentSlot.ARMOR -> character.equippedArmorId == item.id
                EquipmentSlot.HELMET -> character.equippedHelmetId == item.id
                EquipmentSlot.GLOVES -> character.equippedGlovesId == item.id
                EquipmentSlot.BOOTS -> character.equippedBootsId == item.id
                EquipmentSlot.CAPE -> character.equippedCapeId == item.id
                EquipmentSlot.ACCESSORY -> character.equippedAccessoryId == item.id
                EquipmentSlot.FRAME -> character.equippedFrameId == item.id
                EquipmentSlot.BACKGROUND -> character.equippedBackgroundId == item.id
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.iconEmoji, fontSize = 26.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) item.nameAr else item.nameEn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isArabic) item.descriptionAr else item.descriptionEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) "مستوى مطلوب: ${item.requiredLevel}" else "Req. Level ${item.requiredLevel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (character.level >= item.requiredLevel) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if (item.isPurchased) {
                            if (isEquipped) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (isArabic) "مجهّز ✓" else "Equipped ✓",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { onEquip(item) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(text = if (isArabic) "تجهيز" else "Equip", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        } else {
                            Button(
                                onClick = { onPurchase(item) },
                                enabled = canAfford,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${item.xpStoreCost} XP",
                                    color = Color(0xFF1E1B4B),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
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
private fun SkillTreeTab(
    character: CharacterRpgEntity,
    skills: List<SkillNodeEntity>,
    isArabic: Boolean,
    onUnlockSkill: (SkillNodeEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isArabic) "شجرة مهارات البطل" else "Productivity Skill Tree",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isArabic) "نقاط المهارة المتاحة: ${character.availableSkillPoints}"
                        else "Available Skill Points: ${character.availableSkillPoints}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFB300)
                    )
                }
            }
        }

        items(skills) { skill ->
            val canUnlock = !skill.isUnlocked &&
                    character.availableSkillPoints >= skill.pointCost &&
                    character.level >= skill.requiredCharacterLevel

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (skill.isUnlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (skill.isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = skill.iconEmoji, fontSize = 22.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) skill.nameAr else skill.nameEn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isArabic) skill.descriptionAr else skill.descriptionEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (skill.isUnlocked) (if (isArabic) "مفعّلة ✓" else "Unlocked ✓")
                            else (if (isArabic) "تتطلب مستوى ${skill.requiredCharacterLevel} و ${skill.pointCost} نقطة"
                            else "Req. Lv.${skill.requiredCharacterLevel}, ${skill.pointCost} pt"),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (skill.isUnlocked) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!skill.isUnlocked) {
                        Button(
                            onClick = { onUnlockSkill(skill) },
                            enabled = canUnlock,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = if (isArabic) "فتح" else "Unlock", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BossBattleTab(
    bosses: List<BossChallengeEntity>,
    goals: List<GoalEntity>,
    isArabic: Boolean,
    onDamageBoss: (bossId: Long, damage: Int) -> Unit,
    onCreateBoss: (nameEn: String, nameAr: String, hp: Int, rewardXp: Long, goalId: Long?) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "معارك الزعماء (تحويل الأهداف لتحديات)"
                        else "Boss Battles (Turn Real Goals into Bosses)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isArabic) "كل إنجاز حقيقي يوجه ضربة قاضية للزعيم!"
                        else "Every milestone completed deals direct damage to the Boss!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isArabic) "زعيم جديد" else "New Boss", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        if (bosses.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "👾", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isArabic) "لا يوجد زعماء نشطون حالياً" else "No active Bosses yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) "اربط أهدافك الكبيرة بزعيم واقضه عليه بإنجازاتك اليومية!"
                            else "Bind your long-term goals to a Boss and defeat it through consistent real actions!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        items(bosses) { boss ->
            val hpRatio = (boss.currentHp.toFloat() / boss.maxHp.coerceAtLeast(1)).coerceIn(0f, 1f)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (boss.isDefeated) Color(0xFF064E3B).copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = boss.iconEmoji, fontSize = 38.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) boss.bossNameAr else boss.bossNameEn,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isArabic) boss.titleAr else boss.titleEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (boss.isDefeated) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981)
                            ) {
                                Text(
                                    text = if (isArabic) "تمت هزيمته 🏆" else "DEFEATED 🏆",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "نقاط الحياة (HP)" else "Health (HP)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${boss.currentHp} / ${boss.maxHp} HP",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (boss.currentHp < boss.maxHp / 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { hpRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (boss.currentHp < boss.maxHp / 3) Color(0xFFEF4444) else Color(0xFFFFB300),
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    if (!boss.isDefeated) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onDamageBoss(boss.id, 25) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = if (isArabic) "ضربة مهمة (-25)" else "Task Strike (-25)")
                            }

                            Button(
                                onClick = { onDamageBoss(boss.id, 50) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (isArabic) "ضربة هدف (-50)" else "Goal Strike (-50)",
                                    color = Color(0xFF1E1B4B),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var bossName by remember { mutableStateOf("") }
        var bossHpStr by remember { mutableStateOf("200") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(text = if (isArabic) "إنشاء زعيم تحدي جديد" else "Create Goal Boss Challenge")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = bossName,
                        onValueChange = { bossName = it },
                        label = { Text(if (isArabic) "اسم الزعيم (مثال: عملاق التسويف)" else "Boss Name (e.g. Master Procrastination)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bossHpStr,
                        onValueChange = { bossHpStr = it },
                        label = { Text(if (isArabic) "نقاط الحياة (HP)" else "Total HP") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hp = bossHpStr.toIntOrNull() ?: 200
                        if (bossName.isNotBlank()) {
                            onCreateBoss(bossName, bossName, hp, (hp * 1.5).toLong(), null)
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text(text = if (isArabic) "إنشاء" else "Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(text = if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun EquipmentItemRow(
    item: EquipmentItemEntity,
    isEquipped: Boolean,
    isArabic: Boolean,
    onEquip: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = item.iconEmoji, fontSize = 22.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isArabic) item.nameAr else item.nameEn,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${item.slot.name} • ${item.rarity.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isEquipped) {
                Text(
                    text = if (isArabic) "مجهّز ✓" else "Active ✓",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF10B981)
                )
            } else {
                TextButton(onClick = onEquip) {
                    Text(text = if (isArabic) "تجهيز" else "Equip")
                }
            }
        }
    }
}
