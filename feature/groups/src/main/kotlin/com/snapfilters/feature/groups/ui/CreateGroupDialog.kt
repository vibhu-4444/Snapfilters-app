package com.snapfilters.feature.groups.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.snapfilters.core.designsystem.theme.SnapTheme
import com.snapfilters.feature.groups.model.JoiningPolicy

@Composable
fun CreateGroupDialog(
    onConfirm: (name: String, description: String?, policy: JoiningPolicy) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var policy by remember { mutableStateOf(JoiningPolicy.APPROVAL) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(SnapTheme.shapes.sheet)
            .background(SnapTheme.colors.surfaceRaised)
            .padding(SnapTheme.spacing.lg)
    ) {
        Text(
            text = "Create Group",
            style = SnapTheme.typography.title,
            color = SnapTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(SnapTheme.spacing.md))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(60) },
            label = { Text("Group Name (Required)") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SnapTheme.colors.accent,
                unfocusedBorderColor = SnapTheme.colors.surfaceBase,
                focusedTextColor = SnapTheme.colors.textPrimary,
                unfocusedTextColor = SnapTheme.colors.textPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it.take(300) },
            label = { Text("Description (Optional)") },
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SnapTheme.colors.accent,
                unfocusedBorderColor = SnapTheme.colors.surfaceBase,
                focusedTextColor = SnapTheme.colors.textPrimary,
                unfocusedTextColor = SnapTheme.colors.textPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(SnapTheme.spacing.md))

        Text(
            text = "Joining Policy",
            style = SnapTheme.typography.label,
            color = SnapTheme.colors.textSecondary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = SnapTheme.spacing.xs)
        ) {
            RadioButton(
                selected = policy == JoiningPolicy.APPROVAL,
                onClick = { policy = JoiningPolicy.APPROVAL },
                colors = RadioButtonDefaults.colors(selectedColor = SnapTheme.colors.accent)
            )
            Spacer(modifier = Modifier.width(SnapTheme.spacing.xs))
            Text("Admin Approval Required", style = SnapTheme.typography.body, color = SnapTheme.colors.textPrimary)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = policy == JoiningPolicy.DIRECT,
                onClick = { policy = JoiningPolicy.DIRECT },
                colors = RadioButtonDefaults.colors(selectedColor = SnapTheme.colors.accent)
            )
            Spacer(modifier = Modifier.width(SnapTheme.spacing.xs))
            Text("Direct Join via QR", style = SnapTheme.typography.body, color = SnapTheme.colors.textPrimary)
        }

        Spacer(modifier = Modifier.height(SnapTheme.spacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SnapTheme.spacing.md)
        ) {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SnapTheme.colors.surfaceBase,
                    contentColor = SnapTheme.colors.textSecondary
                ),
                shape = SnapTheme.shapes.control,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel", style = SnapTheme.typography.label)
            }

            Button(
                onClick = { onConfirm(name, description.ifBlank { null }, policy) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SnapTheme.colors.accent,
                    contentColor = SnapTheme.colors.surfaceBase
                ),
                shape = SnapTheme.shapes.control,
                modifier = Modifier.weight(1f)
            ) {
                Text("Create", style = SnapTheme.typography.label)
            }
        }
    }
}
