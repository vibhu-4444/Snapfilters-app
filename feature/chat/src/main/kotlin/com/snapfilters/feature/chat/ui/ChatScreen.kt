package com.snapfilters.feature.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.snapfilters.core.designsystem.theme.SnapTheme

/**
 * ChatScreen per PRD v3.0 Chapter 6, 7 & 18:
 * - Thread message list
 * - Text and photo bubbles
 * - Input composer with photo attachment affordance
 */
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    onPickPhotoToShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SnapTheme.colors.surfaceBase)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SnapTheme.spacing.md, vertical = SnapTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = SnapTheme.colors.textPrimary
                )
            }
            Text(
                text = uiState.conversationTitle,
                style = SnapTheme.typography.title,
                color = SnapTheme.colors.textPrimary
            )
        }

        Divider(color = SnapTheme.colors.surfaceRaised)

        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = SnapTheme.spacing.md),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(SnapTheme.spacing.xs)
        ) {
            items(uiState.messages, key = { it.clientMessageId }) { msg ->
                val isOutgoing = msg.senderId == currentUserId
                MessageBubble(
                    message = msg,
                    isOutgoing = isOutgoing,
                    onRetry = viewModel::retryMessage
                )
            }
        }

        Divider(color = SnapTheme.colors.surfaceRaised)

        // Bottom composer row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SnapTheme.colors.surfaceRaised)
                .padding(horizontal = SnapTheme.spacing.md, vertical = SnapTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPickPhotoToShare) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Attach photo",
                    tint = SnapTheme.colors.accent
                )
            }

            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = viewModel::onInputTextChanged,
                placeholder = { Text("Message...", color = SnapTheme.colors.textSecondary) },
                maxLines = 4,
                shape = SnapTheme.shapes.control,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SnapTheme.colors.accent,
                    unfocusedBorderColor = SnapTheme.colors.surfaceBase,
                    focusedTextColor = SnapTheme.colors.textPrimary,
                    unfocusedTextColor = SnapTheme.colors.textPrimary
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(SnapTheme.spacing.sm))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (uiState.inputText.isNotBlank()) SnapTheme.colors.accent else SnapTheme.colors.surfaceBase)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = viewModel::sendTextMessage,
                    enabled = uiState.inputText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (uiState.inputText.isNotBlank()) SnapTheme.colors.surfaceBase else SnapTheme.colors.textTertiary
                    )
                }
            }
        }
    }
}
