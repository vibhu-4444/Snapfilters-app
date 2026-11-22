package com.snapfilters.feature.groups.repository

import com.snapfilters.core.common.result.AppResult
import com.snapfilters.feature.groups.model.Group
import com.snapfilters.feature.groups.model.GroupInvitation
import com.snapfilters.feature.groups.model.GroupRole
import com.snapfilters.feature.groups.model.JoinRequest
import com.snapfilters.feature.groups.model.JoinRequestState
import com.snapfilters.feature.groups.model.JoiningPolicy
import com.snapfilters.feature.groups.model.RedeemResult
import com.snapfilters.feature.groups.qr.QrTokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * GroupRepository manages group lifecycle and the QR invitation state machine per Chapter 11.
 */
class GroupRepository {

    private val groups = mutableMapOf<String, Group>()
    private val memberships = mutableMapOf<Pair<String, String>, GroupRole>() // (groupId, userId) -> Role
    private val invitations = mutableMapOf<String, GroupInvitation>() // tokenHash -> Invitation
    private val joinRequests = mutableMapOf<String, JoinRequest>() // requestId -> JoinRequest

    private val _userGroups = MutableStateFlow<List<Group>>(emptyList())
    val userGroups: StateFlow<List<Group>> = _userGroups.asStateFlow()

    suspend fun createGroup(
        name: String,
        description: String?,
        ownerId: String,
        policy: JoiningPolicy = JoiningPolicy.APPROVAL
    ): AppResult<Group> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty() || trimmedName.length > 60) {
            return AppResult.Error(IllegalArgumentException("Group name must be between 1 and 60 characters."))
        }

        val groupId = UUID.randomUUID().toString()
        val group = Group(
            id = groupId,
            name = trimmedName,
            description = description?.take(300),
            imagePath = null,
            ownerId = ownerId,
            joiningPolicy = policy,
            memberCount = 1
        )

        // Atomic group + owner membership
        groups[groupId] = group
        memberships[Pair(groupId, ownerId)] = GroupRole.ADMIN
        refreshUserGroups(ownerId)

        return AppResult.Success(group)
    }

    suspend fun mintInvitation(groupId: String, creatorId: String): AppResult<Pair<String, GroupInvitation>> {
        val group = groups[groupId] ?: return AppResult.Error(NoSuchElementException("Group not found."))
        val role = memberships[Pair(groupId, creatorId)]
        if (role == null) {
            return AppResult.Error(IllegalAccessException("Only members can create invitations."))
        }

        val rawToken = QrTokenManager.mintToken()
        val tokenHash = QrTokenManager.hashToken(rawToken)
        val invitationId = UUID.randomUUID().toString()

        val invitation = GroupInvitation(
            id = invitationId,
            groupId = groupId,
            rawToken = rawToken,
            tokenHashHex = tokenHash,
            expiresAtEpochMillis = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000, // 7 days
            maxUses = 200,
            usedCount = 0,
            isRevoked = false
        )

        invitations[tokenHash] = invitation
        val qrPayload = QrTokenManager.buildQrPayload(rawToken)
        return AppResult.Success(Pair(qrPayload, invitation))
    }

    suspend fun revokeInvitation(tokenHash: String, adminId: String): AppResult<Unit> {
        val inv = invitations[tokenHash] ?: return AppResult.Error(NoSuchElementException("Invitation not found."))
        val role = memberships[Pair(inv.groupId, adminId)]
        if (role != GroupRole.ADMIN) {
            return AppResult.Error(IllegalAccessException("Only admins can revoke invitations."))
        }

        invitations[tokenHash] = inv.copy(isRevoked = true)
        return AppResult.Success(Unit)
    }

    /**
     * Executes the exact Redeem State Machine defined in PRD v3.0 Chapter 11.
     */
    suspend fun redeemInvitation(rawToken: String, scannerUserId: String, scannerDisplayName: String): RedeemResult {
        val tokenHash = QrTokenManager.hashToken(rawToken)
        val invitation = invitations[tokenHash] ?: return RedeemResult.Invalid

        // 1. Check revoked
        if (invitation.isRevoked) {
            return RedeemResult.Invalid
        }

        // 2. Check expired
        if (System.currentTimeMillis() > invitation.expiresAtEpochMillis) {
            return RedeemResult.Expired
        }

        // 3. Check exhausted
        if (invitation.maxUses != null && invitation.usedCount >= invitation.maxUses) {
            return RedeemResult.Exhausted
        }

        // 4. Check group exists
        val group = groups[invitation.groupId] ?: return RedeemResult.GroupGone

        // 5. Already a member
        if (memberships.containsKey(Pair(group.id, scannerUserId))) {
            return RedeemResult.AlreadyMember(group.id)
        }

        // 6. Check existing pending request
        val hasPending = joinRequests.values.any {
            it.groupId == group.id && it.userId == scannerUserId && it.state == JoinRequestState.PENDING
        }
        if (hasPending) {
            return RedeemResult.AlreadyPending(group.id)
        }

        // Increment use count
        invitations[tokenHash] = invitation.copy(usedCount = invitation.usedCount + 1)

        // 7. Policy branch
        return when (group.joiningPolicy) {
            JoiningPolicy.DIRECT -> {
                // Immediate membership
                memberships[Pair(group.id, scannerUserId)] = GroupRole.MEMBER
                val updatedGroup = group.copy(memberCount = group.memberCount + 1)
                groups[group.id] = updatedGroup
                refreshUserGroups(scannerUserId)
                RedeemResult.Joined(updatedGroup)
            }
            JoiningPolicy.APPROVAL -> {
                // Pending request
                val reqId = UUID.randomUUID().toString()
                joinRequests[reqId] = JoinRequest(
                    id = reqId,
                    groupId = group.id,
                    userId = scannerUserId,
                    userDisplayName = scannerDisplayName,
                    state = JoinRequestState.PENDING
                )
                RedeemResult.Requested(group)
            }
        }
    }

    suspend fun approveJoinRequest(requestId: String, adminId: String): AppResult<Unit> {
        val req = joinRequests[requestId] ?: return AppResult.Error(NoSuchElementException("Request not found."))
        val role = memberships[Pair(req.groupId, adminId)]
        if (role != GroupRole.ADMIN) {
            return AppResult.Error(IllegalAccessException("Only admins can approve join requests."))
        }

        joinRequests[requestId] = req.copy(state = JoinRequestState.APPROVED)
        memberships[Pair(req.groupId, req.userId)] = GroupRole.MEMBER

        val group = groups[req.groupId]
        if (group != null) {
            groups[req.groupId] = group.copy(memberCount = group.memberCount + 1)
        }
        refreshUserGroups(req.userId)

        return AppResult.Success(Unit)
    }

    private fun refreshUserGroups(userId: String) {
        val userGroupIds = memberships.filter { it.key.second == userId }.map { it.key.first }
        _userGroups.value = groups.values.filter { it.id in userGroupIds }
    }
}
