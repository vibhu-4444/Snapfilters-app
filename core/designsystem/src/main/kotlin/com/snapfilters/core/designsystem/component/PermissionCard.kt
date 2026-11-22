package com.snapfilters.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.snapfilters.core.designsystem.theme.SnapTheme

enum class PermissionState {
    Denied,
    PermanentlyDenied
}

/**
 * PermissionCard per PRD v3.0 Chapter 7 & 12:
 * - Explains the exact permission
 * - Distinct action depending on whether it is standard denied or permanently denied
 */
@Composable
fun PermissionCard(
    permissionName: String,
    rationale: String,
    state: PermissionState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(SnapTheme.shapes.sheet)
            .background(SnapTheme.colors.surfaceRaised)
            .padding(SnapTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$permissionName Permission Required",
            style = SnapTheme.typography.title,
            color = SnapTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))

        Text(
            text = rationale,
            style = SnapTheme.typography.body,
            color = SnapTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(SnapTheme.spacing.lg))

        when (state) {
            PermissionState.Denied -> {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SnapTheme.colors.accent,
                        contentColor = SnapTheme.colors.surfaceBase
                    ),
                    shape = SnapTheme.shapes.control
                ) {
                    Text(text = "Grant Permission", style = SnapTheme.typography.label)
                }
            }
            PermissionState.PermanentlyDenied -> {
                Button(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SnapTheme.colors.accent,
                        contentColor = SnapTheme.colors.surfaceBase
                    ),
                    shape = SnapTheme.shapes.control
                ) {
                    Text(text = "Open App Settings", style = SnapTheme.typography.label)
                }
            }
        }
    }
}

