package com.snapfilters.feature.settings.ui

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
 * SettingsScreen per PRD v3.0 Chapter 6, 7 & 22:
 * - Storage usage & cache pruning
 * - Privacy inventory & facial geometry statements
 * - Destructive account deletion with clear confirmation naming the object
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SnapTheme.colors.surfaceBase)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
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
                text = "Settings",
                style = SnapTheme.typography.title,
                color = SnapTheme.colors.textPrimary
            )
        }

        Divider(color = SnapTheme.colors.surfaceRaised)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(SnapTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SnapTheme.spacing.lg)
        ) {
            // Storage Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SnapTheme.shapes.control)
                    .background(SnapTheme.colors.surfaceRaised)
                    .padding(SnapTheme.spacing.md)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = SnapTheme.colors.accent)
                    Spacer(modifier = Modifier.padding(SnapTheme.spacing.xs))
                    Text("Storage Usage", style = SnapTheme.typography.title, color = SnapTheme.colors.textPrimary)
                }
                Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))
                val cacheMb = "%.1f MB".format(uiState.cacheSizeBytes / (1024f * 1024f))
                Text("Temporary Share Cache: $cacheMb", style = SnapTheme.typography.body, color = SnapTheme.colors.textSecondary)

                Spacer(modifier = Modifier.height(SnapTheme.spacing.md))
                Button(
                    onClick = viewModel::clearShareCache,
                    enabled = !uiState.isClearingCache,
                    colors = ButtonDefaults.buttonColors(containerColor = SnapTheme.colors.surfaceBase, contentColor = SnapTheme.colors.accent),
                    shape = SnapTheme.shapes.control
                ) {
                    Text("Clear Temporary Cache", style = SnapTheme.typography.label)
                }
            }

            // Privacy Section (Chapter 22)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SnapTheme.shapes.control)
                    .background(SnapTheme.colors.surfaceRaised)
                    .padding(SnapTheme.spacing.md)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = SnapTheme.colors.accent)
                    Spacer(modifier = Modifier.padding(SnapTheme.spacing.xs))
                    Text("Privacy & Security", style = SnapTheme.typography.title, color = SnapTheme.colors.textPrimary)
                }
                Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))
                Text("• Zero facial identity, embeddings, or biometric templates are stored or transmitted.", style = SnapTheme.typography.caption, color = SnapTheme.colors.textSecondary)
                Text("• Live face landmarks run strictly on-device in ephemeral GPU memory.", style = SnapTheme.typography.caption, color = SnapTheme.colors.textSecondary)
                Text("• Zero advertising SDKs. Zero tracking libraries.", style = SnapTheme.typography.caption, color = SnapTheme.colors.textSecondary)
            }

            // Danger Zone: Account Deletion (Chapter 6 & 22)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SnapTheme.shapes.control)
                    .background(SnapTheme.colors.surfaceRaised)
                    .padding(SnapTheme.spacing.md)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = SnapTheme.colors.danger)
                    Spacer(modifier = Modifier.padding(SnapTheme.spacing.xs))
                    Text("Danger Zone", style = SnapTheme.typography.title, color = SnapTheme.colors.danger)
                }
                Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))
                Text("Permanently removes your anonymous session, leaves all groups, and deletes all uploaded media.", style = SnapTheme.typography.caption, color = SnapTheme.colors.textSecondary)

                Spacer(modifier = Modifier.height(SnapTheme.spacing.md))
                Button(
                    onClick = viewModel::promptDeleteAccount,
                    colors = ButtonDefaults.buttonColors(containerColor = SnapTheme.colors.danger, contentColor = SnapTheme.colors.textPrimary),
                    shape = SnapTheme.shapes.control
                ) {
                    Text("Delete Account", style = SnapTheme.typography.label)
                }
            }
        }

        // Destructive Confirmation Modal
        if (uiState.showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = viewModel::dismissDeleteAccountPrompt,
                title = { Text("Delete Account?", style = SnapTheme.typography.title, color = SnapTheme.colors.textPrimary) },
                text = {
                    Text(
                        "Are you sure you want to delete your account? This action cannot be reversed. Your profile, group memberships, and recovery code will be permanently deleted.",
                        style = SnapTheme.typography.body,
                        color = SnapTheme.colors.textSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = viewModel::confirmDeleteAccount,
                        colors = ButtonDefaults.buttonColors(containerColor = SnapTheme.colors.danger)
                    ) {
                        Text("Delete Account", style = SnapTheme.typography.label)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = viewModel::dismissDeleteAccountPrompt,
                        colors = ButtonDefaults.buttonColors(containerColor = SnapTheme.colors.surfaceBase, contentColor = SnapTheme.colors.textPrimary)
                    ) {
                        Text("Cancel", style = SnapTheme.typography.label)
                    }
                },
                containerColor = SnapTheme.colors.surfaceRaised
            )
        }
    }
}
