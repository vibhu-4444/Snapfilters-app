package com.snapfilters.feature.groups.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.snapfilters.core.designsystem.theme.SnapTheme
import com.snapfilters.feature.groups.model.RedeemResult

/**
 * JoinPreviewDialog handles the visual feedback for all QR redeem states
 * defined in PRD v3.0 Chapter 6 (State matrix) and Chapter 11.
 */
@Composable
fun JoinPreviewDialog(
    redeemResult: RedeemResult,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(SnapTheme.shapes.sheet)
            .background(SnapTheme.colors.surfaceRaised)
            .padding(SnapTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (redeemResult) {
            is RedeemResult.Joined -> {
                Text(
                    text = "Welcome to ${redeemResult.group.name}!",
                    style = SnapTheme.typography.title,
                    color = SnapTheme.colors.success,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))
                Text(
                    text = "You joined directly via QR code. You can now share cinematic photos and message the group.",
                    style = SnapTheme.typography.body,
                    color = SnapTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
            is RedeemResult.Requested -> {
                Text(
                    text = "Join Request Sent",
                    style = SnapTheme.typography.title,
                    color = SnapTheme.colors.accent,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))
                Text(
                    text = "Your request to join '${redeemResult.group.name}' is pending admin approval.",
                    style = SnapTheme.typography.body,
                    color = SnapTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
            is RedeemResult.AlreadyMember -> {
                Text(
                    text = "Already a Member",
                    style = SnapTheme.typography.title,
                    color = SnapTheme.colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))
                Text(
                    text = "You are already a member of this group.",
                    style = SnapTheme.typography.body,
                    color = SnapTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
            is RedeemResult.AlreadyPending -> {
                Text(
                    text = "Request Pending",
                    style = SnapTheme.typography.title,
                    color = SnapTheme.colors.accent,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))
                Text(
                    text = "You already have a pending join request for this group. Please wait for an admin to approve.",
                    style = SnapTheme.typography.body,
                    color = SnapTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
            is RedeemResult.Expired -> {
                Text(
                    text = "Invitation Expired",
                    style = SnapTheme.typography.title,
                    color = SnapTheme.colors.danger,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))
                Text(
                    text = "This QR invitation code has expired. Please ask a group admin for a new code.",
                    style = SnapTheme.typography.body,
                    color = SnapTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
            is RedeemResult.Exhausted -> {
                Text(
                    text = "Invitation Limit Reached",
                    style = SnapTheme.typography.title,
                    color = SnapTheme.colors.danger,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))
                Text(
                    text = "This invitation has reached its maximum usage limit.",
                    style = SnapTheme.typography.body,
                    color = SnapTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
            is RedeemResult.Invalid -> {
                Text(
                    text = "Invalid Invitation",
                    style = SnapTheme.typography.title,
                    color = SnapTheme.colors.danger,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))
                Text(
                    text = "This invitation QR code is invalid or has been revoked.",
                    style = SnapTheme.typography.body,
                    color = SnapTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
            is RedeemResult.GroupGone -> {
                Text(
                    text = "Group No Longer Exists",
                    style = SnapTheme.typography.title,
                    color = SnapTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(SnapTheme.spacing.xl))

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
                containerColor = SnapTheme.colors.accent,
                contentColor = SnapTheme.colors.surfaceBase
            ),
            shape = SnapTheme.shapes.control,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done", style = SnapTheme.typography.label)
        }
    }
}
