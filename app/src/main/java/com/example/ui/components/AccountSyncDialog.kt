package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.local.model.UserAccountEntity
import com.example.domain.sync.SyncState

@Composable
fun AccountSyncDialog(
    account: UserAccountEntity?,
    syncState: SyncState,
    isArabic: Boolean = false,
    onDismiss: () -> Unit,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String) -> Unit,
    onSignOut: () -> Unit,
    onSyncNow: () -> Unit,
    onResetPassword: (String) -> Unit,
    onDeleteAccount: () -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showForgotPassword by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F1B2C),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("account_sync_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (account?.isLoggedIn == true) {
                            if (isArabic) "الحساب والمزامنة السحابية" else "Account & Cloud Sync"
                        } else {
                            if (isSignUpMode) {
                                if (isArabic) "إنشاء حساب جديد" else "Create YAWMEK Account"
                            } else {
                                if (isArabic) "تسجيل الدخول" else "Sign In"
                            }
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFF8FAFC)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (account != null && account.isLoggedIn) {
                    // Logged In View
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = account.displayName.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F1B2C)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = account.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = account.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sync Status Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A40)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (isArabic) "حالة المزامنة السحابية" else "Cloud Sync Status",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                                val statusText = when (syncState) {
                                    is SyncState.Syncing -> if (isArabic) "جارِ المزامنة الآن..." else "Syncing now..."
                                    is SyncState.Synced -> if (isArabic) "تمت المزامنة بنجاح ✅" else "All data synced ✅"
                                    is SyncState.OfflinePending -> if (isArabic) "${syncState.pendingCount} تغيير بانتظار الاتصال" else "${syncState.pendingCount} changes queued offline"
                                    is SyncState.Error -> if (isArabic) "خطأ في المزامنة" else "Sync Error: ${syncState.message}"
                                    else -> if (isArabic) "محدث" else "Up to date"
                                }
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFFFBBF24)
                                )
                            }
                            FilledTonalButton(
                                onClick = onSyncNow,
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF243754))
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Sync", tint = Color(0xFFF59E0B))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isArabic) "مزامنة" else "Sync", color = Color(0xFFF59E0B))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sign Out & Delete buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = onSignOut,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isArabic) "تسجيل الخروج" else "Sign Out")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        TextButton(
                            onClick = onDeleteAccount,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF94A3B8))
                        ) {
                            Text(if (isArabic) "حذف الحساب" else "Delete Account")
                        }
                    }
                } else {
                    // Auth Form (Sign In or Sign Up)
                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text(if (isArabic) "الاسم" else "Name") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF59E0B),
                                focusedLabelColor = Color(0xFFF59E0B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(if (isArabic) "البريد الإلكتروني" else "Email") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF59E0B),
                            focusedLabelColor = Color(0xFFF59E0B)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(if (isArabic) "كلمة المرور" else "Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF59E0B),
                            focusedLabelColor = Color(0xFFF59E0B)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (isSignUpMode) {
                                onSignUp(email, displayName, password)
                            } else {
                                onSignIn(email, password)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (isSignUpMode) {
                                if (isArabic) "إنشاء الحساب والمزامنة" else "Create Account & Sync"
                            } else {
                                if (isArabic) "تسجيل الدخول" else "Sign In"
                            },
                            color = Color(0xFF0F1B2C),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Switch between Sign In and Sign Up
                    TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                        Text(
                            text = if (isSignUpMode) {
                                if (isArabic) "لديك حساب بالفعل؟ سجل دخولك" else "Already have an account? Sign In"
                            } else {
                                if (isArabic) "ليس لديك حساب؟ اشترك الآن" else "Don't have an account? Sign Up"
                            },
                            color = Color(0xFFFBBF24)
                        )
                    }

                    if (!isSignUpMode) {
                        TextButton(onClick = { onResetPassword(email) }) {
                            Text(
                                text = if (isArabic) "نسيت كلمة المرور؟" else "Forgot password?",
                                color = Color(0xFF94A3B8),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
