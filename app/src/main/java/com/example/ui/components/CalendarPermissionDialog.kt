package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CalendarPermissionDialog(
    isArabic: Boolean = false,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Filled.DateRange,
                contentDescription = "Calendar",
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = if (isArabic) "ربط تقويم الجهاز بـ يومك" else "Connect Device Calendar",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = if (isArabic) {
                        "يتيح لك ربط التقويم عرض مواعيدك واجتماعاتك بجانب مهامك اليومية، وحساب أوقات فراغك بدقة لاقتراح أفضل عمل تنجزه الآن.\n\nبيانات تقويمك تبقى خاصة على جهازك تماماً."
                    } else {
                        "Connecting your calendar allows YAWMEK to display your events alongside tasks and calculate your true free time in 'What Should I Do Now?'.\n\nYour calendar data stays private on your device."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onRequestPermission()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                modifier = Modifier.testTag("btn_grant_calendar")
            ) {
                Text(
                    if (isArabic) "تفعيل الوصول للتقويم" else "Allow Calendar Access",
                    color = Color(0xFF0F1B2C),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "ليس الآن" else "Not Now", color = Color(0xFF94A3B8))
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFF0F1B2C)
    )
}
