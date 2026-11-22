package com.snapfilters.feature.groups.model

enum class CapabilitySetting {
    ALL,
    ADMIN
}

/**
 * 14-Capability Matrix per PRD v3.0 Chapter 12:
 * An admin bypasses the setting entirely.
 * A member's capability is determined by the group's row setting ('all' vs 'admin').
 */
data class GroupCapabilities(
    val msgSend: CapabilitySetting = CapabilitySetting.ALL,
    val msgReply: CapabilitySetting = CapabilitySetting.ALL,
    val msgReact: CapabilitySetting = CapabilitySetting.ALL,
    val photoShare: CapabilitySetting = CapabilitySetting.ALL,
    val fileUpload: CapabilitySetting = CapabilitySetting.ALL,
    val mediaDownload: CapabilitySetting = CapabilitySetting.ALL,
    val memberInvite: CapabilitySetting = CapabilitySetting.ADMIN,
    val joinApprove: CapabilitySetting = CapabilitySetting.ADMIN,
    val memberRemove: CapabilitySetting = CapabilitySetting.ADMIN,
    val editInfo: CapabilitySetting = CapabilitySetting.ADMIN,
    val manageInvitations: CapabilitySetting = CapabilitySetting.ADMIN,
    val managePermissions: CapabilitySetting = CapabilitySetting.ADMIN,
    val promoteAdmin: CapabilitySetting = CapabilitySetting.ADMIN,
    val deleteGroup: CapabilitySetting = CapabilitySetting.ADMIN
)

enum class Capability {
    MSG_SEND,
    MSG_REPLY,
    MSG_REACT,
    PHOTO_SHARE,
    FILE_UPLOAD,
    MEDIA_DOWNLOAD,
    MEMBER_INVITE,
    JOIN_APPROVE,
    MEMBER_REMOVE,
    EDIT_INFO,
    MANAGE_INVITATIONS,
    MANAGE_PERMISSIONS,
    PROMOTE_ADMIN,
    DELETE_GROUP
}

object PermissionEvaluator {

    /**
     * Client-side permission evaluation mirroring backend Chapter 10 & 12 resolution algorithm:
     * 1. authenticated? no -> false
     * 2. member? no -> false
     * 3. admin? yes -> true
     * 4. setting == ALL? yes -> true
     * 5. otherwise -> false
     */
    fun hasPermission(
        role: GroupRole?,
        capability: Capability,
        capabilities: GroupCapabilities
    ): Boolean {
        if (role == null) return false
        if (role == GroupRole.ADMIN) return true

        val setting = when (capability) {
            Capability.MSG_SEND -> capabilities.msgSend
            Capability.MSG_REPLY -> capabilities.msgReply
            Capability.MSG_REACT -> capabilities.msgReact
            Capability.PHOTO_SHARE -> capabilities.photoShare
            Capability.FILE_UPLOAD -> capabilities.fileUpload
            Capability.MEDIA_DOWNLOAD -> capabilities.mediaDownload
            Capability.MEMBER_INVITE -> capabilities.memberInvite
            Capability.JOIN_APPROVE -> capabilities.joinApprove
            Capability.MEMBER_REMOVE -> capabilities.memberRemove
            Capability.EDIT_INFO -> capabilities.editInfo
            Capability.MANAGE_INVITATIONS -> capabilities.manageInvitations
            Capability.MANAGE_PERMISSIONS -> capabilities.managePermissions
            Capability.PROMOTE_ADMIN -> capabilities.promoteAdmin
            Capability.DELETE_GROUP -> capabilities.deleteGroup
        }

        return setting == CapabilitySetting.ALL
    }
}
