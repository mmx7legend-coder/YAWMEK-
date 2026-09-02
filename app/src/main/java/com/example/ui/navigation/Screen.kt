package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestination(
    val route: String,
    val titleEn: String,
    val titleAr: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Home", "الرئيسية", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    PLAN("plan", "Plan", "الجدول", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    MONEY("money", "Money", "المصاريف", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    HABITS("habits", "Habits", "العادات", Icons.Filled.Repeat, Icons.Outlined.Repeat),
    GOALS("goals", "Goals", "الأهداف", Icons.Filled.Flag, Icons.Outlined.Flag),
    NOTES("notes", "Notes", "الملاحظات", Icons.Filled.Description, Icons.Outlined.Description),
    GAMES("games", "Games", "الألعاب", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
    AI("ai", "YAWMEK AI", "مساعد يومك", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    PROFILE("profile", "Profile", "الحساب", Icons.Filled.Person, Icons.Outlined.Person)
}
