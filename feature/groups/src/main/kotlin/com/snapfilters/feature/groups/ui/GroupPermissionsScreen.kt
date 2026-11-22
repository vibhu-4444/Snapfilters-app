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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snapfilters.core.designsystem.theme.SnapTheme
import com.snapfilters.feature.groups.model.Capability
import com.snapfilters.feature.groups.model.CapabilitySetting
import com.snapfilters.feature.groups.model.GroupCapabilities

/**
 * GroupPermissionsScreen per PRD v3.0 Chapter 6, 7 & 12:
 * Allows group Admins to toggle capabilities between "All Members" and "Admin Only".
 */
@Composable
fun GroupPermissionsScreen(
    groupName: String,
    initialCapabilities: GroupCapabilities,
    onPermissionChanged: (Capability, CapabilitySetting) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var capabilities by remember { mutableStateOf(initialCapabilities) }
    val scrollState = rememberScrollState()

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
                text = "Permissions - $groupName",
                style = SnapTheme.typography.title,
                color = SnapTheme.colors.textPrimary
            )
        }

        Divider(color = SnapTheme.colors.surfaceRaised)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(SnapTheme.spacing.lg)
        ) {
            Text(
                text = "Member Permissions",
                style = SnapTheme.typography.label,
                color = SnapTheme.colors.accent
            )
            Text(
                text = "Turn switch ON to allow all members. Turn OFF for admin-only.",
                style = SnapTheme.typography.caption,
                color = SnapTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(SnapTheme.spacing.md))

            PermissionToggleRow(
                title = "Send Messages",
                description = "Post text in the group thread",
                isAllMembers = capabilities.msgSend == CapabilitySetting.ALL,
                onToggle = { isAll ->
                    val newSetting = if (isAll) CapabilitySetting.ALL else CapabilitySetting.ADMIN
                    capabilities = capabilities.copy(msgSend = newSetting)
                    onPermissionChanged(Capability.MSG_SEND, newSetting)
                }
            )

            PermissionToggleRow(
                title = "Share Photographs",
                description = "Upload and share cinematic photos",
                isAllMembers = capabilities.photoShare == CapabilitySetting.ALL,
                onToggle = { isAll ->
                    val newSetting = if (isAll) CapabilitySetting.ALL else CapabilitySetting.ADMIN
                    capabilities = capabilities.copy(photoShare = newSetting)
                    onPermissionChanged(Capability.PHOTO_SHARE, newSetting)
                }
            )

            PermissionToggleRow(
                title = "Upload Original Files",
                description = "Share uncompressed sensor originals",
                isAllMembers = capabilities.fileUpload == CapabilitySetting.ALL,
                onToggle = { isAll ->
                    val newSetting = if (isAll) CapabilitySetting.ALL else CapabilitySetting.ADMIN
                    capabilities = capabilities.copy(fileUpload = newSetting)
                    onPermissionChanged(Capability.FILE_UPLOAD, newSetting)
                }
            )

            PermissionToggleRow(
                title = "Download Shared Media",
                description = "Save group media to local device",
                isAllMembers = capabilities.mediaDownload == CapabilitySetting.ALL,
                onToggle = { isAll ->
                    val newSetting = if (isAll) CapabilitySetting.ALL else CapabilitySetting.ADMIN
                    capabilities = capabilities.copy(mediaDownload = newSetting)
                    onPermissionChanged(Capability.MEDIA_DOWNLOAD, newSetting)
                }
            )

            PermissionToggleRow(
                title = "Invite Members",
                description = "Generate and display invite QR codes",
                isAllMembers = capabilities.memberInvite == CapabilitySetting.ALL,
                onToggle = { isAll ->
                    val newSetting = if (isAll) CapabilitySetting.ALL else CapabilitySetting.ADMIN
                    capabilities = capabilities.copy(memberInvite = newSetting)
                    onPermissionChanged(Capability.MEMBER_INVITE, newSetting)
                }
            )

            PermissionToggleRow(
                title = "Edit Group Info",
                description = "Change name, description, or group avatar",
                isAllMembers = capabilities.editInfo == CapabilitySetting.ALL,
                onToggle = { isAll ->
                    val newSetting = if (isAll) CapabilitySetting.ALL else CapabilitySetting.ADMIN
                    capabilities = capabilities.copy(editInfo = newSetting)
                    onPermissionChanged(Capability.EDIT_INFO, newSetting)
                }
            )
        }
    }
}

@Composable
private fun PermissionToggleRow(
    title: String,
    description: String,
    isAllMembers: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SnapTheme.spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SnapTheme.typography.body,
                color = SnapTheme.colors.textPrimary
            )
            Text(
                text = description,
                style = SnapTheme.typography.caption,
                color = SnapTheme.colors.textSecondary
            )
        }

        Switch(
            checked = isAllMembers,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SnapTheme.colors.accent,
                checkedTrackColor = SnapTheme.colors.accentTint,
                uncheckedThumbColor = SnapTheme.colors.textTertiary,
                uncheckedTrackColor = SnapTheme.colors.surfaceRaised
            )
        )
    }
}
