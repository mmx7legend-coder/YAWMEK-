package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.model.*
import com.example.domain.community.BattleSimulationResult
import com.example.ui.theme.*
import com.example.ui.viewmodel.YawmekUiState
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class CommunityTab(val titleEn: String, val titleAr: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    FRIENDS("Friends", "الأصدقاء", Icons.Filled.People),
    ONLINE("Online", "المتصلون", Icons.Filled.Wifi),
    REQUESTS("Requests", "الطلبات", Icons.Filled.PersonAdd),
    BATTLES("Battles", "المعارك", Icons.Filled.SportsKabaddi),
    LEADERBOARD("Ranking", "المتصدرون", Icons.Filled.Leaderboard),
    PROFILE("My Profile", "ملفي الشخصي", Icons.Filled.AccountCircle),
    SEARCH("Search", "البحث", Icons.Filled.Search),
    CONNECTION("Connection", "الربط الآمن", Icons.Filled.Key)
}

private val AVATAR_OPTIONS = listOf(
    "warrior" to "⚔️ Warrior",
    "paladin" to "🛡️ Paladin",
    "mage" to "🔮 Mage",
    "monk" to "🧘 Monk",
    "archer" to "🏹 Archer",
    "alchemist" to "⚗️ Alchemist",
    "shadow" to "🗡️ Rogue"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    uiState: YawmekUiState,
    onSetupProfile: (username: String, displayName: String, avatarId: String, bio: String) -> Unit,
    onUpdateProfile: (displayName: String, avatarId: String, bio: String) -> Unit,
    onChangeUsername: (newUsername: String) -> Unit,
    onSendFriendRequest: (friendUsername: String) -> Unit,
    onAcceptFriendRequest: (CommunityFriendEntity) -> Unit,
    onRejectFriendRequest: (CommunityFriendEntity) -> Unit,
    onRemoveFriend: (CommunityFriendEntity) -> Unit,
    onStartBattle: (CommunityFriendEntity, BattleType) -> Unit,
    onDismissBattleSimulation: () -> Unit,
    onTestConnection: () -> Unit,
    onRegenerateConnectionId: () -> Unit,
    onBackToHome: () -> Unit
) {
    val isArabic = uiState.userSettings.language == AppLanguage.ARABIC
    var selectedTab by remember { mutableStateOf(CommunityTab.FRIENDS) }

    // Dialog States
    var showSetupDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangeUsernameDialog by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var selectedFriendForBattle by remember { mutableStateOf<CommunityFriendEntity?>(null) }
    var battleTypeSelection by remember { mutableStateOf(BattleType.RPG_ARENA) }

    val myProfile = uiState.communityProfile
    val isSetupDone = myProfile?.isInitialSetupDone == true

    // Prompt setup if not done
    LaunchedEffect(isSetupDone) {
        if (!isSetupDone) {
            showSetupDialog = true
        }
    }

    // Active battle simulation result popup
    uiState.activeBattleSimulation?.let { simResult ->
        BattleResultDialog(
            isArabic = isArabic,
            result = simResult,
            onDismiss = onDismissBattleSimulation
        )
    }

    // Challenge dialog
    selectedFriendForBattle?.let { friend ->
        BattleChallengeDialog(
            isArabic = isArabic,
            friend = friend,
            selectedType = battleTypeSelection,
            onSelectType = { battleTypeSelection = it },
            onConfirmBattle = {
                onStartBattle(friend, battleTypeSelection)
                selectedFriendForBattle = null
            },
            onDismiss = { selectedFriendForBattle = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isArabic) "مجتمع يومك" else "YAWMEK Community",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isArabic) "ساحة الإنتاجية الاجتماعية وتحديات الأبطال" else "Social Productivity & RPG Arena",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Connection Status indicator button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (uiState.isCommunityConnected) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                        modifier = Modifier
                            .clickable { selectedTab = CommunityTab.CONNECTION }
                            .padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.isCommunityConnected) Color(0xFF10B981) else Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isCommunityConnected) (if (isArabic) "متصل" else "Online") else (if (isArabic) "منفصل" else "Offline"),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (uiState.isCommunityConnected) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        }
                    }

                    IconButton(onClick = { showAddFriendDialog = true }) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "Add Friend", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("community_screen")
        ) {
            // Tab Selector Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) }
            ) {
                CommunityTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val pendingCount = uiState.communityFriends.count { it.status == FriendshipStatus.PENDING_RECEIVED }
                    val onlineCount = uiState.communityFriends.count { it.status == FriendshipStatus.FRIEND && it.isOnline }

                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isArabic) tab.titleAr else tab.titleEn)
                                if (tab == CommunityTab.REQUESTS && pendingCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                                        Text("$pendingCount", color = Color.White)
                                    }
                                } else if (tab == CommunityTab.ONLINE && onlineCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Badge(containerColor = Color(0xFF10B981)) {
                                        Text("$onlineCount", color = Color.White)
                                    }
                                }
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                }
            }

            // Tab Content
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "community_tab_content",
                modifier = Modifier.fillMaxSize()
            ) { tab ->
                when (tab) {
                    CommunityTab.FRIENDS -> FriendsTabContent(
                        isArabic = isArabic,
                        friends = uiState.communityFriends.filter { it.status == FriendshipStatus.FRIEND },
                        onChallenge = { friend -> selectedFriendForBattle = friend },
                        onRemove = onRemoveFriend,
                        onAddFriendClick = { showAddFriendDialog = true }
                    )
                    CommunityTab.ONLINE -> OnlineFriendsTabContent(
                        isArabic = isArabic,
                        friends = uiState.communityFriends.filter { it.status == FriendshipStatus.FRIEND && it.isOnline },
                        onChallenge = { friend -> selectedFriendForBattle = friend }
                    )
                    CommunityTab.REQUESTS -> FriendRequestsTabContent(
                        isArabic = isArabic,
                        incoming = uiState.communityFriends.filter { it.status == FriendshipStatus.PENDING_RECEIVED },
                        outgoing = uiState.communityFriends.filter { it.status == FriendshipStatus.PENDING_SENT },
                        onAccept = onAcceptFriendRequest,
                        onReject = onRejectFriendRequest
                    )
                    CommunityTab.BATTLES -> BattlesTabContent(
                        isArabic = isArabic,
                        friends = uiState.communityFriends.filter { it.status == FriendshipStatus.FRIEND },
                        battleHistory = uiState.communityBattles,
                        onChallenge = { friend -> selectedFriendForBattle = friend }
                    )
                    CommunityTab.LEADERBOARD -> LeaderboardTabContent(
                        isArabic = isArabic,
                        leaderboard = uiState.leaderboardUsers
                    )
                    CommunityTab.PROFILE -> CommunityProfileTabContent(
                        isArabic = isArabic,
                        profile = myProfile,
                        rpgCharacter = uiState.rpgCharacter,
                        todayTasks = uiState.tasks.count { it.isCompleted },
                        todayFocus = uiState.todayFocusMinutes,
                        onEditClick = { showEditProfileDialog = true },
                        onChangeUsernameClick = { showChangeUsernameDialog = true }
                    )
                    CommunityTab.SEARCH -> SearchUsersTabContent(
                        isArabic = isArabic,
                        friends = uiState.communityFriends,
                        onSendRequest = onSendFriendRequest
                    )
                    CommunityTab.CONNECTION -> ConnectionSetupTabContent(
                        isArabic = isArabic,
                        connectionId = myProfile?.connectionId ?: "YAWMEK-NET-DEFAULT",
                        isConnected = uiState.isCommunityConnected,
                        onTestConnection = onTestConnection,
                        onRegenerate = onRegenerateConnectionId
                    )
                }
            }
        }
    }

    // First-Time Setup Dialog
    if (showSetupDialog) {
        SetupProfileDialog(
            isArabic = isArabic,
            initialName = myProfile?.displayName ?: "Hero",
            onComplete = { u, d, a, b ->
                onSetupProfile(u, d, a, b)
                showSetupDialog = false
            }
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            isArabic = isArabic,
            currentDisplay = myProfile?.displayName ?: "",
            currentAvatar = myProfile?.avatarId ?: "warrior",
            currentBio = myProfile?.bio ?: "",
            onSave = { d, a, b ->
                onUpdateProfile(d, a, b)
                showEditProfileDialog = false
            },
            onDismiss = { showEditProfileDialog = false }
        )
    }

    // Change Username Dialog
    if (showChangeUsernameDialog) {
        ChangeUsernameDialog(
            isArabic = isArabic,
            currentUsername = myProfile?.username ?: "",
            onConfirm = { newU ->
                onChangeUsername(newU)
                showChangeUsernameDialog = false
            },
            onDismiss = { showChangeUsernameDialog = false }
        )
    }

    // Send Friend Request Dialog
    if (showAddFriendDialog) {
        AddFriendDialog(
            isArabic = isArabic,
            onSend = { targetU ->
                onSendFriendRequest(targetU)
                showAddFriendDialog = false
            },
            onDismiss = { showAddFriendDialog = false }
        )
    }
}

// =========================================================================
// 1. FRIENDS TAB
// =========================================================================
@Composable
private fun FriendsTabContent(
    isArabic: Boolean,
    friends: List<CommunityFriendEntity>,
    onChallenge: (CommunityFriendEntity) -> Unit,
    onRemove: (CommunityFriendEntity) -> Unit,
    onAddFriendClick: () -> Unit
) {
    if (friends.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("👥", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isArabic) "لا يوجد أصدقاء بعد" else "No Friends Added Yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isArabic) "ابحث عن رفاقك أو أرسل لهم معرفك لتبدأ التحديات والسباقات الإنتاجية!"
                        else "Search for companions or share your ID to start productive challenges together!",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onAddFriendClick,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isArabic) "إضافة صديق" else "Add Friend")
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if (isArabic) "قائمة الأصدقاء (${friends.size})" else "Friends List (${friends.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(friends, key = { it.id }) { friend ->
                FriendCard(
                    isArabic = isArabic,
                    friend = friend,
                    onChallenge = { onChallenge(friend) },
                    onRemove = { onRemove(friend) }
                )
            }
        }
    }
}

@Composable
private fun FriendCard(
    isArabic: Boolean,
    friend: CommunityFriendEntity,
    onChallenge: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Avatar with online badge
                Box {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(getAvatarEmoji(friend.avatarId), fontSize = 22.sp)
                        }
                    }
                    if (friend.isOnline) {
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        )
                    }
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = friend.friendDisplayName,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Lv.${friend.level}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    Text(
                        text = "@${friend.friendUsername} • ${friend.rankTitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${if (isArabic) "تقييم" else "Rating"}: ${friend.rating} • ${friend.focusMinutes}m focus",
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorRpg
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalButton(
                    onClick = onChallenge,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.SportsKabaddi, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isArabic) "بارز" else "Battle")
                }

                IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.PersonRemove, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// =========================================================================
// 2. ONLINE FRIENDS TAB
// =========================================================================
@Composable
private fun OnlineFriendsTabContent(
    isArabic: Boolean,
    friends: List<CommunityFriendEntity>,
    onChallenge: (CommunityFriendEntity) -> Unit
) {
    if (friends.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isArabic) "لا يوجد أصدقاء متصلون الآن 💤" else "No friends currently online 💤",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "الأصدقاء المتصلون حالياً (${friends.size})" else "Active Friends (${friends.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            items(friends) { friend ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(getAvatarEmoji(friend.avatarId), fontSize = 24.sp)
                            Column {
                                Text(friend.friendDisplayName, fontWeight = FontWeight.Bold)
                                Text("@${friend.friendUsername} • Ready for challenge", style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981))
                            }
                        }
                        Button(
                            onClick = { onChallenge(friend) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(if (isArabic) "تحدي فوري ⚔️" else "Duel Now ⚔️")
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. FRIEND REQUESTS TAB
// =========================================================================
@Composable
private fun FriendRequestsTabContent(
    isArabic: Boolean,
    incoming: List<CommunityFriendEntity>,
    outgoing: List<CommunityFriendEntity>,
    onAccept: (CommunityFriendEntity) -> Unit,
    onReject: (CommunityFriendEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Incoming
        item {
            Text(
                text = if (isArabic) "الطلبات الواردة (${incoming.size})" else "Incoming Requests (${incoming.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (incoming.isEmpty()) {
            item {
                Text(
                    text = if (isArabic) "لا توجد طلبات جديدة واردة" else "No pending incoming requests",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(incoming, key = { it.id }) { req ->
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(getAvatarEmoji(req.avatarId), fontSize = 24.sp)
                            Column {
                                Text(req.friendDisplayName, fontWeight = FontWeight.Bold)
                                Text("@${req.friendUsername} • Level ${req.level}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { onAccept(req) },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.2f), contentColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (isArabic) "قبول" else "Accept")
                            }
                            OutlinedButton(
                                onClick = { onReject(req) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (isArabic) "رفض" else "Decline")
                            }
                        }
                    }
                }
            }
        }

        // Outgoing
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isArabic) "الطلبات المرسلة (${outgoing.size})" else "Sent Requests (${outgoing.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary
            )
        }
        if (outgoing.isEmpty()) {
            item {
                Text(
                    text = if (isArabic) "لا توجد طلبات معلقة مرسلة" else "No pending outgoing requests",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(outgoing, key = { it.id }) { req ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("@${req.friendUsername}", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (isArabic) "قيد الانتظار ⏳" else "Pending ⏳",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 4. BATTLES & ARENA TAB
// =========================================================================
@Composable
private fun BattlesTabContent(
    isArabic: Boolean,
    friends: List<CommunityFriendEntity>,
    battleHistory: List<CommunityBattleEntity>,
    onChallenge: (CommunityFriendEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Challenge Header Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ColorRpg.copy(alpha = 0.12f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚔️", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) "ساحة المعارك والمبارزات" else "Battle & Duel Arena",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ColorRpg
                            )
                            Text(
                                text = if (isArabic) "تحدَّ أصدقاءك في ساحة RPG، صراع التركيز، أو سباق المهام اليومية!"
                                else "Challenge friends in RPG Arena, Focus Clash, or Task Sprint!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (friends.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isArabic) "اختر خصماً للمبارزة:" else "Select an opponent to duel:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(friends) { friend ->
                                FilledTonalButton(
                                    onClick = { onChallenge(friend) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(getAvatarEmoji(friend.avatarId))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(friend.friendDisplayName)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Battle Logs
        item {
            Text(
                text = if (isArabic) "سجل المبارزات السابقة (${battleHistory.size})" else "Recent Duel History (${battleHistory.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (battleHistory.isEmpty()) {
            item {
                Text(
                    text = if (isArabic) "لم تشارك في أي مبارزة بعد. ابدأ أول تحدٍ لك واربح نقاط خبرة RPG!"
                    else "No battles logged yet. Start your first duel and earn RPG XP!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(battleHistory, key = { it.id }) { battle ->
                val isWin = battle.result == BattleResult.VICTORY
                val isDraw = battle.result == BattleResult.DRAW
                val resultColor = when {
                    isWin -> Color(0xFF10B981)
                    isDraw -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = resultColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = when (battle.result) {
                                            BattleResult.VICTORY -> if (isArabic) "فوز 🏆" else "VICTORY 🏆"
                                            BattleResult.DEFEAT -> if (isArabic) "هزيمة 🛡️" else "DEFEAT 🛡️"
                                            BattleResult.DRAW -> if (isArabic) "تعادل 🤝" else "DRAW 🤝"
                                        },
                                        color = resultColor,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "vs ${battle.opponentDisplayName}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Text(
                                text = "${if (battle.ratingDelta >= 0) "+" else ""}${battle.ratingDelta} Rating",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = resultColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${battle.battleType.name.replace("_", " ")}: ${battle.myScore} pts vs ${battle.opponentScore} pts",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = battle.summaryLog,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 5. LEADERBOARD TAB
// =========================================================================
@Composable
private fun LeaderboardTabContent(
    isArabic: Boolean,
    leaderboard: List<LeaderboardUserItem>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = if (isArabic) "لوحة صدارة الأبطال والمجتمع" else "Hall of Champions & Community Ranking",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isArabic) "الترتيب مبني على تقييم المبارزات، الاستمرارية، ودقائق التركيز الحقيقية."
                else "Ranking based on duel ratings, consistency, and verified focus minutes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(leaderboard, key = { it.username }) { item ->
            val isCurrent = item.isCurrentUser
            val medal = when (item.rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "#${item.rank}"
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = if (isCurrent) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ColorRpg, MaterialTheme.colorScheme.primary)))
                else CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = medal,
                            fontSize = if (item.rank <= 3) 22.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center
                        )

                        Text(getAvatarEmoji(item.avatarId), fontSize = 24.sp)

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.displayName,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = if (isArabic) "أنت" else "YOU",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "@${item.username} • Level ${item.level}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${item.rating} Elo",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ColorRpg
                        )
                        Text(
                            text = "${item.focusMinutes}m focus • ${item.battleWins}W",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 6. MY PROFILE TAB
// =========================================================================
@Composable
private fun CommunityProfileTabContent(
    isArabic: Boolean,
    profile: CommunityProfileEntity?,
    rpgCharacter: CharacterRpgEntity?,
    todayTasks: Int,
    todayFocus: Int,
    onEditClick: () -> Unit,
    onChangeUsernameClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Profile Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(getAvatarEmoji(profile?.avatarId ?: "warrior"), fontSize = 40.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = profile?.displayName ?: "Hero",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "@${profile?.username ?: "adventurer"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = onChangeUsernameClick, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Edit, contentDescription = "Change Username", modifier = Modifier.size(14.dp))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ColorRpg.copy(alpha = 0.15f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = if (isArabic) (profile?.rankTitleAr ?: "مغامر مبتدئ") else (profile?.rankTitleEn ?: "Novice Adventurer"),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ColorRpg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (!profile?.bio.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = profile!!.bio,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "تعديل الملف والرمز" else "Edit Profile & Avatar")
                    }
                }
            }
        }

        // Productivity & Battle Stats Grid
        item {
            Text(
                text = if (isArabic) "إحصائيات الأداء والمبارزات" else "Productivity & Combat Stats",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCardMini(
                    title = if (isArabic) "التقييم (Elo)" else "Duel Rating",
                    value = "${profile?.rating ?: 1000}",
                    color = ColorRpg,
                    modifier = Modifier.weight(1f)
                )
                StatCardMini(
                    title = if (isArabic) "سجل المعارك" else "Win / Loss",
                    value = "${profile?.battleWins ?: 0}W / ${profile?.battleLosses ?: 0}L",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCardMini(
                    title = if (isArabic) "مهام اليوم" else "Tasks Done Today",
                    value = "$todayTasks",
                    color = ColorProductivity,
                    modifier = Modifier.weight(1f)
                )
                StatCardMini(
                    title = if (isArabic) "دقائق التركيز" else "Focus Today",
                    value = "${todayFocus}m",
                    color = ColorFocus,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Equipped Gear Snapshot
        item {
            Text(
                text = if (isArabic) "العتاد المجهز للأبطال" else "Equipped RPG Loadout",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LoadoutRow(label = if (isArabic) "السلاح" else "Weapon", value = rpgCharacter?.equippedWeaponId ?: "starter_stick", emoji = "⚔️")
                    LoadoutRow(label = if (isArabic) "الدرع" else "Armor", value = rpgCharacter?.equippedArmorId ?: "ragged_cloth", emoji = "🛡️")
                    LoadoutRow(label = if (isArabic) "المرافق" else "Companion", value = rpgCharacter?.equippedCompanionId ?: "none", emoji = "🐾")
                }
            }
        }
    }
}

@Composable
private fun StatCardMini(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = color)
        }
    }
}

@Composable
private fun LoadoutRow(label: String, value: String, emoji: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value.replace("_", " ").replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
    }
}

// =========================================================================
// 7. SEARCH USERS TAB
// =========================================================================
@Composable
private fun SearchUsersTabContent(
    isArabic: Boolean,
    friends: List<CommunityFriendEntity>,
    onSendRequest: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (isArabic) "ابحث باسم المستخدم أو المعرف" else "Search by username or ID") },
            placeholder = { Text("e.g. ziyad_paladin") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        // Pre-discovered or searched users
        Text(
            text = if (isArabic) "مستخدمون مقترحون في الشبكة" else "Suggested Network Adventurers",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        val sampleUsers = listOf(
            Triple("khalid_crusader", "Khalid Crusader", "paladin"),
            Triple("layla_wisdom", "Layla The Wise", "mage"),
            Triple("hasan_runner", "Hasan Speedster", "archer"),
            Triple("mariam_healer", "Mariam Alchemist", "alchemist")
        ).filter { (u, _, _) ->
            if (searchQuery.isBlank()) true else u.contains(searchQuery.lowercase().trim())
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(sampleUsers) { (u, d, a) ->
                val isAlreadyFriend = friends.any { it.friendUsername == u && it.status == FriendshipStatus.FRIEND }
                val isPending = friends.any { it.friendUsername == u && it.status == FriendshipStatus.PENDING_SENT }

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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(getAvatarEmoji(a), fontSize = 24.sp)
                            Column {
                                Text(d, fontWeight = FontWeight.Bold)
                                Text("@$u", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        when {
                            isAlreadyFriend -> {
                                Text(if (isArabic) "صديق ✓" else "Friend ✓", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                            isPending -> {
                                Text(if (isArabic) "معلق..." else "Pending...", color = MaterialTheme.colorScheme.secondary)
                            }
                            else -> {
                                Button(
                                    onClick = { onSendRequest(u) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isArabic) "إضافة" else "Add")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 8. SECURE CONNECTION / API SETUP TAB
// =========================================================================
@Composable
private fun ConnectionSetupTabContent(
    isArabic: Boolean,
    connectionId: String,
    isConnected: Boolean,
    onTestConnection: () -> Unit,
    onRegenerate: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedToast by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) "إعدادات الاتصال الآمن بالمجتمع" else "Secure Community Connection",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isArabic) "اتصال نظير إلى نظير (P2P) مشفر دون مشاركة مفاتيح خاصة"
                                else "Encrypted P2P connection without exposing private keys",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isArabic) "معرف الربط الخاص بك (Connection ID):" else "Your Unique Connection ID:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = connectionId,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(connectionId))
                                    copiedToast = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    if (copiedToast) {
                        Text(
                            text = if (isArabic) "تم نسخ المعرف إلى الحافظة! ✓" else "Connection ID copied! ✓",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onTestConnection,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "اختبار الاتصال" else "Ping Network")
                        }

                        OutlinedButton(
                            onClick = onRegenerate,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "تجديد المعرف" else "Regenerate ID")
                        }
                    }
                }
            }
        }

        // Security & Privacy Info Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "🔒 معايير الأمان والخصوصية" else "🔒 Privacy & Security Protocol",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isArabic)
                            "• لا يتم إرسال أي مفاتيح API خاصة إطلاقاً عبر الشبكة.\n• يتم تبادل إحصائيات الأداء والمبارزات عبر معرف الربط المشفر فقط.\n• يمكنك تجديد معرفك في أي وقت لقطع أي اتصالات غير مرغوبة فوراً."
                        else
                            "• No private API keys are ever exposed or transmitted over the network.\n• Performance and battle stats are exchanged strictly through your encrypted Connection ID.\n• You can regenerate your ID at any time to sever unwanted connections immediately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// =========================================================================
// DIALOGS & ACTION MODALS
// =========================================================================

@Composable
private fun SetupProfileDialog(
    isArabic: Boolean,
    initialName: String,
    onComplete: (String, String, String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf(initialName) }
    var selectedAvatar by remember { mutableStateOf("warrior") }
    var bio by remember { mutableStateOf("Ready to conquer my goals!") }

    Dialog(onDismissRequest = { /* mandatory first time */ }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🛡️", fontSize = 42.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isArabic) "مرحباً بك في مجتمع يومك!" else "Welcome to YAWMEK Community!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (isArabic) "أنشئ هويتك الفريدة في ساحة الإنتاجية الاجتماعية" else "Create your unique identity in the productivity arena",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.filter { c -> c.isLetterOrDigit() || c == '_' }.lowercase() },
                    label = { Text(if (isArabic) "اسم المستخدم الفريد (@username)" else "Unique Username (@username)") },
                    prefix = { Text("@") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text(if (isArabic) "الاسم المعروض" else "Display Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isArabic) "اختر شخصية البطل (Avatar):" else "Choose Hero Avatar:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AVATAR_OPTIONS) { (id, label) ->
                        FilterChip(
                            selected = selectedAvatar == id,
                            onClick = { selectedAvatar = id },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (username.length >= 3) {
                            onComplete(username, displayName, selectedAvatar, bio)
                        }
                    },
                    enabled = username.length >= 3,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isArabic) "انضم إلى المجتمع 🚀" else "Enter Community 🚀")
                }
            }
        }
    }
}

@Composable
private fun EditProfileDialog(
    isArabic: Boolean,
    currentDisplay: String,
    currentAvatar: String,
    currentBio: String,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var displayName by remember { mutableStateOf(currentDisplay) }
    var selectedAvatar by remember { mutableStateOf(currentAvatar) }
    var bio by remember { mutableStateOf(currentBio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isArabic) "تعديل الملف الشخصي" else "Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text(if (isArabic) "الاسم المعروض" else "Display Name") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text(if (isArabic) "نبذة عنك" else "Bio") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(if (isArabic) "الرمز الشخصي:" else "Avatar:", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AVATAR_OPTIONS) { (id, label) ->
                        FilterChip(
                            selected = selectedAvatar == id,
                            onClick = { selectedAvatar = id },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(displayName, selectedAvatar, bio) }) {
                Text(if (isArabic) "حفظ" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}

@Composable
private fun ChangeUsernameDialog(
    isArabic: Boolean,
    currentUsername: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newUsername by remember { mutableStateOf(currentUsername) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isArabic) "تغيير اسم المستخدم" else "Change Username") },
        text = {
            Column {
                Text(
                    text = if (isArabic) "يجب أن يكون اسم المستخدم 3 أحرف على الأقل وبدون مسافات."
                    else "Username must be at least 3 characters without spaces.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it.filter { c -> c.isLetterOrDigit() || c == '_' }.lowercase() },
                    label = { Text("@username") },
                    prefix = { Text("@") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newUsername) },
                enabled = newUsername.length >= 3 && newUsername != currentUsername
            ) {
                Text(if (isArabic) "تأكيد" else "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}

@Composable
private fun AddFriendDialog(
    isArabic: Boolean,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var targetUsername by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isArabic) "إرسال طلب صداقة" else "Send Friend Request") },
        text = {
            Column {
                Text(
                    text = if (isArabic) "أدخل اسم مستخدم الصديق لإرسال دعوة مبارزة وصداقة:"
                    else "Enter your friend's username to send a friend invitation:",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = targetUsername,
                    onValueChange = { targetUsername = it.trim().lowercase().removePrefix("@") },
                    label = { Text(if (isArabic) "اسم المستخدم" else "Username") },
                    prefix = { Text("@") },
                    placeholder = { Text("e.g. sarah_chrono") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(targetUsername) },
                enabled = targetUsername.length >= 3
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isArabic) "إرسال الطلب" else "Send Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}

@Composable
private fun BattleChallengeDialog(
    isArabic: Boolean,
    friend: CommunityFriendEntity,
    selectedType: BattleType,
    onSelectType: (BattleType) -> Unit,
    onConfirmBattle: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚔️", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isArabic) "تحدي ${friend.friendDisplayName}" else "Challenge ${friend.friendDisplayName}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Level ${friend.level} • Rating ${friend.rating}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isArabic) "اختر نوع المنافسة:" else "Select Competition Type:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                listOf(
                    BattleType.RPG_ARENA to (if (isArabic) "ساحة RPG الأسطورية (قوة العتاد والمهارات)" else "RPG Arena (Gear & Skill Stats)"),
                    BattleType.FOCUS_SHOWDOWN to (if (isArabic) "صراع التركيز العميق (دقائق تركيز اليوم)" else "Focus Showdown (Today's Focus Minutes)"),
                    BattleType.TASK_RACE to (if (isArabic) "سباق المهام السريع (عدد المهام المنجزة)" else "Task Blitz Race (Completed Tasks Count)")
                ).forEach { (bType, label) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedType == bType) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (selectedType == bType) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ColorRpg, MaterialTheme.colorScheme.primary))) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectType(bType) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedType == bType, onClick = { onSelectType(bType) })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text(if (isArabic) "إلغاء" else "Cancel")
                    }
                    Button(onClick = onConfirmBattle, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text(if (isArabic) "بدء النزال! ⚡" else "Start Duel! ⚡")
                    }
                }
            }
        }
    }
}

@Composable
private fun BattleResultDialog(
    isArabic: Boolean,
    result: BattleSimulationResult,
    onDismiss: () -> Unit
) {
    val isWin = result.result == BattleResult.VICTORY
    val isDraw = result.result == BattleResult.DRAW
    val bannerColor = when {
        isWin -> Color(0xFF10B981)
        isDraw -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(if (isWin) "🏆" else if (isDraw) "🤝" else "🛡️", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when (result.result) {
                        BattleResult.VICTORY -> if (isArabic) "انتصار ساحق!" else "VICTORY!"
                        BattleResult.DEFEAT -> if (isArabic) "هزيمة قريبة!" else "DEFEAT!"
                        BattleResult.DRAW -> if (isArabic) "تعادل مشرف!" else "DRAW!"
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = bannerColor
                )

                Text(
                    text = "${result.myScore} pts vs ${result.opponentScore} pts",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Combat log box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isArabic) "مجريات النزال:" else "Combat Highlights:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.summaryLog,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rewards & Rating change
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isArabic) "تغير التقييم" else "Rating Change", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "${if (result.ratingDelta >= 0) "+" else ""}${result.ratingDelta}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = bannerColor
                        )
                    }

                    if (result.xpGained > 0) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (isArabic) "خبرة مكتسبة" else "XP Gained", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "+${result.xpGained} XP",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = ColorRpg
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isArabic) "متابعة" else "Continue")
                }
            }
        }
    }
}

private fun getAvatarEmoji(avatarId: String): String {
    return when (avatarId.lowercase()) {
        "warrior" -> "⚔️"
        "paladin" -> "🛡️"
        "mage" -> "🔮"
        "monk" -> "🧘"
        "archer" -> "🏹"
        "alchemist" -> "⚗️"
        "shadow", "rogue" -> "🗡️"
        else -> "🧙"
    }
}
