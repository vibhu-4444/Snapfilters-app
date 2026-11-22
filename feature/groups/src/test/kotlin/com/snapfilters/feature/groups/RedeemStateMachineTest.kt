package com.snapfilters.feature.groups

import com.snapfilters.core.common.result.AppResult
import com.snapfilters.feature.groups.model.JoiningPolicy
import com.snapfilters.feature.groups.model.RedeemResult
import com.snapfilters.feature.groups.qr.QrTokenManager
import com.snapfilters.feature.groups.repository.GroupRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Base64

class RedeemStateMachineTest {

    private lateinit var groupRepository: GroupRepository
    private val ownerId = "owner-uuid-1"
    private val scannerId = "scanner-uuid-2"

    @Before
    fun setUp() {
        groupRepository = GroupRepository()
    }

    // FR-GRP-02: QR token is 32 bytes random (256-bit entropy)
    @Test
    fun fr_grp_02_minted_token_has_256_bits_entropy_and_no_group_id_in_payload() {
        val token = QrTokenManager.mintToken()
        val decoded = Base64.getUrlDecoder().decode(token)
        assertEquals("Token must be exactly 32 random bytes (256 bits)", 32, decoded.size)

        val payload = QrTokenManager.buildQrPayload(token)
        assertTrue(payload.startsWith("https://app.snapfilters.local/join?t="))
        // Assert QR payload never carries raw group ID or keys
        assertTrue(!payload.contains("group_id"))
    }

    // FR-GRP-01 & Chapter 11: Direct policy join
    @Test
    fun fr_grp_01_create_group_and_redeem_direct_join() = runTest {
        val groupResult = groupRepository.createGroup("Alpha Shooters", "Photo club", ownerId, JoiningPolicy.DIRECT)
        assertTrue(groupResult is AppResult.Success)
        val group = (groupResult as AppResult.Success).data

        // Mint invite
        val inviteResult = groupRepository.mintInvitation(group.id, ownerId)
        assertTrue(inviteResult is AppResult.Success)
        val (qrPayload, invitation) = (inviteResult as AppResult.Success).data
        val token = invitation.rawToken

        // Scanner redeems
        val redeemResult = groupRepository.redeemInvitation(token, scannerId, "Scanner User")
        assertTrue(redeemResult is RedeemResult.Joined)
        assertEquals(group.id, (redeemResult as RedeemResult.Joined).group.id)
    }

    // FR-GRP-03: Approval policy creates pending request
    @Test
    fun fr_grp_03_approval_policy_returns_requested_state() = runTest {
        val group = (groupRepository.createGroup("Private Circle", null, ownerId, JoiningPolicy.APPROVAL) as AppResult.Success).data
        val invitation = (groupRepository.mintInvitation(group.id, ownerId) as AppResult.Success).data.second

        val redeemResult = groupRepository.redeemInvitation(invitation.rawToken, scannerId, "Candidate")
        assertTrue(redeemResult is RedeemResult.Requested)

        // Duplicate scan returns ALREADY_PENDING
        val secondScan = groupRepository.redeemInvitation(invitation.rawToken, scannerId, "Candidate")
        assertTrue(secondScan is RedeemResult.AlreadyPending)
    }

    // Chapter 11: Revoked invitation returns INVALID
    @Test
    fun revoked_invitation_returns_invalid_state() = runTest {
        val group = (groupRepository.createGroup("Studio Crew", null, ownerId) as AppResult.Success).data
        val invitation = (groupRepository.mintInvitation(group.id, ownerId) as AppResult.Success).data.second

        // Admin revokes
        groupRepository.revokeInvitation(invitation.tokenHashHex, ownerId)

        val result = groupRepository.redeemInvitation(invitation.rawToken, scannerId, "Newbie")
        assertEquals(RedeemResult.Invalid, result)
    }

    // Chapter 11: Already member returns ALREADY_MEMBER
    @Test
    fun already_member_returns_already_member_state() = runTest {
        val group = (groupRepository.createGroup("Street Photography", null, ownerId, JoiningPolicy.DIRECT) as AppResult.Success).data
        val invitation = (groupRepository.mintInvitation(group.id, ownerId) as AppResult.Success).data.second

        // Owner (who is already member) scans
        val result = groupRepository.redeemInvitation(invitation.rawToken, ownerId, "Owner")
        assertTrue(result is RedeemResult.AlreadyMember)
    }
}
