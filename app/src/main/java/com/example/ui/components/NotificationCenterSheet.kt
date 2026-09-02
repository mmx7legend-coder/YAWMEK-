package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.notifications.InAppNotification
import com.example.domain.notifications.SmartNotificationManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterSheet(
    notifications: List<InAppNotification>,
    isArabic: Boolean = false,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F1B2C),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF334155)) },
        modifier = Modifier.testTag("notification_center_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFFF59E0B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "مركز التنبيهات والإشعارات" else "Notification Center",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFF8FAFC)
                    )
                }

                if (notifications.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text(if (isArabic) "مسح الكل" else "Clear All", color = Color(0xFF94A3B8))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Caught up",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isArabic) "لا توجد إشعارات جديدة" else "All caught up!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = if (isArabic) "ستظهر التنبيهات الذكية ومواعيدك القادمة هنا" else "Smart reminders and schedule alerts will appear here",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(notifications) { notif ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A40)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                val (icon, iconColor) = when (notif.channelId) {
                                    SmartNotificationManager.CHANNEL_TASKS -> Icons.Filled.CheckCircle to Color(0xFFF59E0B)
                                    SmartNotificationManager.CHANNEL_BUDGET -> Icons.Filled.Warning to Color(0xFFEF4444)
                                    SmartNotificationManager.CHANNEL_FOCUS -> Icons.Filled.Star to Color(0xFF38BDF8)
                                    SmartNotificationManager.CHANNEL_HABITS -> Icons.Filled.Favorite to Color(0xFFEC4899)
                                    else -> Icons.Filled.Info to Color(0xFF10B981)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(iconColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = notif.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFFF8FAFC)
                                        )
                                        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(notif.timestampMillis))
                                        Text(
                                            text = timeStr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = notif.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFCBD5E1)
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
