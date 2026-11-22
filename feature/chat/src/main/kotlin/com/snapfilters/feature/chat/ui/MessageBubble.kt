package com.snapfilters.feature.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.snapfilters.core.designsystem.theme.SnapTheme
import com.snapfilters.feature.chat.model.ChatMessage
import com.snapfilters.feature.chat.model.MessageKind
import com.snapfilters.feature.chat.model.MessageStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * MessageBubble per PRD v3.0 Chapter 7 & 18:
 * - States: Sending, Sent, Delivered, Failed, Deleted
 * - Failed state carries an inline retry affordance
 * - Incoming bubble uses surface.raised, outgoing uses surface.base with subtle border
 * - Tombstone author rendered as "Former member"
 */
@Composable
fun MessageBubble(
    message: ChatMessage,
    isOutgoing: Boolean,
    onRetry: (clientMessageId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    val bubbleBackground = if (isOutgoing) {
        SnapTheme.colors.surfaceRaised
    } else {
        SnapTheme.colors.surfaceRaised.copy(alpha = 0.6f)
    }

    val borderModifier = if (isOutgoing) {
        Modifier.border(1.dp, SnapTheme.colors.accent.copy(alpha = 0.25f), bubbleShape)
    } else {
        Modifier
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.createdAtEpochMillis))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        // Sender display name (for group incoming messages)
        if (!isOutgoing) {
            Text(
                text = message.effectiveSenderName,
                style = SnapTheme.typography.caption,
                color = if (message.isSenderDeleted) SnapTheme.colors.textTertiary else SnapTheme.colors.accent,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .then(borderModifier)
                .background(bubbleBackground)
                .padding(SnapTheme.spacing.md)
        ) {
            Column {
                if (message.kind == MessageKind.PHOTO && message.mediaStoragePath != null) {
                    // Photo Attachment Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(SnapTheme.shapes.control)
                            .background(SnapTheme.colors.surfaceSunken),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📸 Cinematic Photo",
                            style = SnapTheme.typography.label,
                            color = SnapTheme.colors.accent
                        )
                    }
                    Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))
                }

                if (!message.body.isNullOrBlank()) {
                    Text(
                        text = message.body,
                        style = SnapTheme.typography.body,
                        color = SnapTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Footer: Timestamp + Delivery State
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formattedTime,
                        style = SnapTheme.typography.caption,
                        color = SnapTheme.colors.textSecondary
                    )

                    if (isOutgoing) {
                        when (message.status) {
                            MessageStatus.FAILED -> {
                                Row(
                                    modifier = Modifier
                                        .clickable { onRetry(message.clientMessageId) }
                                        .padding(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Failed. Tap to retry",
                                        tint = SnapTheme.colors.danger,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Retry",
                                        style = SnapTheme.typography.caption,
                                        color = SnapTheme.colors.danger
                                    )
                                }
                            }
                            MessageStatus.SENT -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Sent",
                                    tint = SnapTheme.colors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            MessageStatus.DELIVERED -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Delivered",
                                    tint = SnapTheme.colors.accent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }
}
