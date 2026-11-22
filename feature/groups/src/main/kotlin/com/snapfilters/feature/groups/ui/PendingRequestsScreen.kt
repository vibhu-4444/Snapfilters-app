package com.snapfilters.feature.groups.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.snapfilters.core.designsystem.theme.SnapTheme
import com.snapfilters.feature.groups.model.JoinRequest

/**
 * PendingRequestsScreen per PRD v3.0 Chapter 6, 8 (Journey J2) & Chapter 12:
 * Allows group Admins to review, approve, or reject pending join requests (FR-GRP-04).
 */
@Composable
fun PendingRequestsScreen(
    requests: List<JoinRequest>,
    onApproveRequest: (requestId: String) -> Unit,
    onRejectRequest: (requestId: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SnapTheme.colors.surfaceBase)
            .statusBarsPadding()
            .navigationBarsPadding()
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
                text = "Join Requests (${requests.size})",
                style = SnapTheme.typography.title,
                color = SnapTheme.colors.textPrimary
            )
        }

        Divider(color = SnapTheme.colors.surfaceRaised)

        if (requests.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(SnapTheme.spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No Pending Requests",
                    style = SnapTheme.typography.body,
                    color = SnapTheme.colors.textSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = SnapTheme.spacing.lg, vertical = SnapTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(SnapTheme.spacing.sm)
            ) {
                items(requests) { req ->
                    RequestRow(
                        request = req,
                        onApprove = { onApproveRequest(req.id) },
                        onReject = { onRejectRequest(req.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestRow(
    request: JoinRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SnapTheme.shapes.control)
            .background(SnapTheme.colors.surfaceRaised)
            .padding(SnapTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User icon placeholder
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SnapTheme.colors.surfaceSunken),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = SnapTheme.colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.width(SnapTheme.spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = request.userDisplayName,
                style = SnapTheme.typography.body,
                color = SnapTheme.colors.textPrimary
            )
            Text(
                text = "Wants to join via QR invite",
                style = SnapTheme.typography.caption,
                color = SnapTheme.colors.textSecondary
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(SnapTheme.spacing.xs)) {
            // Reject
            IconButton(onClick = onReject) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Reject",
                    tint = SnapTheme.colors.danger
                )
            }

            // Approve
            IconButton(onClick = onApprove) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Approve",
                    tint = SnapTheme.colors.success
                )
            }
        }
    }
}
