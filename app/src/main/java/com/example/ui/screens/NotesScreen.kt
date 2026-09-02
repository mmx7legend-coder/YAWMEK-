package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.AppLanguage
import com.example.data.local.model.NoteEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QuickAddTab
import com.example.ui.theme.BrandAmber
import com.example.ui.viewmodel.YawmekUiState

@Composable
fun NotesScreen(
    uiState: YawmekUiState,
    onOpenQuickAdd: (QuickAddTab) -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit
) {
    val language = uiState.userSettings.language
    val isArabic = language == AppLanguage.ARABIC

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("notes_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isArabic) "الملاحظات والأفكار" else "Quick Notes",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = { onOpenQuickAdd(QuickAddTab.NOTE) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BrandAmber.copy(alpha = 0.15f))
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add Note",
                        tint = BrandAmber
                    )
                }
            }
        }

        if (uiState.notes.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.Description,
                    iconTint = BrandAmber,
                    title = if (isArabic) "سجل أفكارك وملاحظاتك" else "Capture thoughts on the fly",
                    subtitle = if (isArabic) "احفظ الأفكار السريعة، القوائم، أو الملاحظات الهامة في مكان واحد آمن." else "Keep quick thoughts, reference links, and brainstorming notes handy.",
                    actionButtonText = if (isArabic) "+ إضافة ملاحظة" else "+ Create a Note",
                    onActionClick = { onOpenQuickAdd(QuickAddTab.NOTE) }
                )
            }
        } else {
            items(uiState.notes) { note ->
                NoteCard(
                    note = note,
                    language = language,
                    onTogglePin = { onTogglePin(note) },
                    onDelete = { onDeleteNote(note) }
                )
            }
        }
    }
}

@Composable
fun NoteCard(
    note: NoteEntity,
    language: AppLanguage,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val isArabic = language == AppLanguage.ARABIC

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (note.isPinned) BrandAmber.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = if (note.isPinned) BrandAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            if (note.tag.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "#${note.tag}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
