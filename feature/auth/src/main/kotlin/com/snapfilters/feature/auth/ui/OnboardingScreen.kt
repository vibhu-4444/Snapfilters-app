package com.snapfilters.feature.auth.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.snapfilters.core.designsystem.theme.SnapTheme

/**
 * OnboardingScreen implements PRD v3.0 Chapter 6, 8 (Journey J1) & 19:
 * - Simple display name prompt (FR-IDN-02: zero phone/email fields)
 * - Single-use 12-word recovery code card with copy action (FR-IDN-04)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SnapTheme.colors.surfaceBase)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(SnapTheme.spacing.xl)
    ) {
        if (uiState.recoveryCode == null) {
            // Step 1: Display Name Entry
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to Snapfilters",
                    style = SnapTheme.typography.display,
                    color = SnapTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))

                Text(
                    text = "Enter a display name to get started. No phone number or email required.",
                    style = SnapTheme.typography.body,
                    color = SnapTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(SnapTheme.spacing.xl))

                OutlinedTextField(
                    value = uiState.displayNameInput,
                    onValueChange = viewModel::onDisplayNameChanged,
                    label = { Text("Display Name", color = SnapTheme.colors.textSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SnapTheme.colors.accent,
                        unfocusedBorderColor = SnapTheme.colors.surfaceRaised,
                        focusedTextColor = SnapTheme.colors.textPrimary,
                        unfocusedTextColor = SnapTheme.colors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(SnapTheme.spacing.lg))

                Button(
                    onClick = viewModel::submitDisplayName,
                    enabled = !uiState.isLoading && uiState.displayNameInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SnapTheme.colors.accent,
                        contentColor = SnapTheme.colors.surfaceBase
                    ),
                    shape = SnapTheme.shapes.control,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (uiState.isLoading) "Connecting..." else "Continue",
                        style = SnapTheme.typography.label
                    )
                }
            }
        } else {
            // Step 2: 12-Word Recovery Code Presentation (FR-IDN-04)
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Save Your Recovery Code",
                    style = SnapTheme.typography.title,
                    color = SnapTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))

                Text(
                    text = "Keep this 12-word code safe. If you ever reinstall or switch devices, it is the only way to recover your account and groups.",
                    style = SnapTheme.typography.body,
                    color = SnapTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(SnapTheme.spacing.lg))

                // Words Grid
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SnapTheme.shapes.control)
                        .background(SnapTheme.colors.surfaceRaised)
                        .border(1.dp, SnapTheme.colors.accent.copy(alpha = 0.3f), SnapTheme.shapes.control)
                        .padding(SnapTheme.spacing.md)
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.recoveryCode?.words?.forEachIndexed { index, word ->
                            Row(
                                modifier = Modifier
                                    .clip(SnapTheme.shapes.control)
                                    .background(SnapTheme.colors.surfaceBase)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. ",
                                    style = SnapTheme.typography.caption,
                                    color = SnapTheme.colors.accent
                                )
                                Text(
                                    text = word,
                                    style = SnapTheme.typography.label,
                                    color = SnapTheme.colors.textPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SnapTheme.spacing.md))

                // Copy button
                Button(
                    onClick = {
                        val wordsText = uiState.recoveryCode?.words?.joinToString(" ") ?: ""
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Recovery Code", wordsText))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SnapTheme.colors.surfaceRaised,
                        contentColor = SnapTheme.colors.accent
                    ),
                    shape = SnapTheme.shapes.control
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(SnapTheme.spacing.sm))
                    Text(text = "Copy Recovery Code", style = SnapTheme.typography.label)
                }

                Spacer(modifier = Modifier.height(SnapTheme.spacing.xl))

                Button(
                    onClick = {
                        viewModel.confirmRecoveryCodeSaved()
                        onComplete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SnapTheme.colors.accent,
                        contentColor = SnapTheme.colors.surfaceBase
                    ),
                    shape = SnapTheme.shapes.control,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "I've Saved My Code", style = SnapTheme.typography.label)
                }
            }
        }
    }
}
