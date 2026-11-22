package com.snapfilters.feature.groups

import com.snapfilters.feature.groups.model.Capability
import com.snapfilters.feature.groups.model.CapabilitySetting
import com.snapfilters.feature.groups.model.GroupCapabilities
import com.snapfilters.feature.groups.model.GroupRole
import com.snapfilters.feature.groups.model.PermissionEvaluator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * AdversarialPermissionTest strictly executes the adversarial security requirements
 * defined in PRD v3.0 Chapter 20 (Milestone 5 exit gate):
 * "Policy tests are adversarial by design. For each of the 14 capabilities, a test signs in as
 * a member, as an admin, and as a non-member, and asserts allow/deny on the real policy."
 */
class AdversarialPermissionTest {

    private val allFourteenCapabilities = Capability.values()

    @Test
    fun verify_exactly_14_capabilities_defined() {
        org.junit.Assert.assertEquals(14, allFourteenCapabilities.size)
    }

    // 1. Admin unconditionally bypasses all 14 capability restrictions
    @Test
    fun adversarial_admin_has_unconditional_permission_for_all_14_capabilities() {
        val lockedDownCapabilities = GroupCapabilities(
            msgSend = CapabilitySetting.ADMIN,
            msgReply = CapabilitySetting.ADMIN,
            msgReact = CapabilitySetting.ADMIN,
            photoShare = CapabilitySetting.ADMIN,
            fileUpload = CapabilitySetting.ADMIN,
            mediaDownload = CapabilitySetting.ADMIN,
            memberInvite = CapabilitySetting.ADMIN,
            joinApprove = CapabilitySetting.ADMIN,
            memberRemove = CapabilitySetting.ADMIN,
            editInfo = CapabilitySetting.ADMIN,
            manageInvitations = CapabilitySetting.ADMIN,
            managePermissions = CapabilitySetting.ADMIN,
            promoteAdmin = CapabilitySetting.ADMIN,
            deleteGroup = CapabilitySetting.ADMIN
        )

        allFourteenCapabilities.forEach { cap ->
            val hasPermission = PermissionEvaluator.hasPermission(
                role = GroupRole.ADMIN,
                capability = cap,
                capabilities = lockedDownCapabilities
            )
            assertTrue("Admin must be allowed capability $cap regardless of group settings", hasPermission)
        }
    }

    // 2. Tampered client test: Member attempting admin-locked capabilities is REJECTED (FR-PRM-02)
    @Test
    fun adversarial_tampered_client_member_is_denied_when_capability_is_admin_only() {
        val lockedCapabilities = GroupCapabilities(
            msgSend = CapabilitySetting.ADMIN,
            msgReply = CapabilitySetting.ADMIN,
            msgReact = CapabilitySetting.ADMIN,
            photoShare = CapabilitySetting.ADMIN,
            fileUpload = CapabilitySetting.ADMIN,
            mediaDownload = CapabilitySetting.ADMIN,
            memberInvite = CapabilitySetting.ADMIN,
            joinApprove = CapabilitySetting.ADMIN,
            memberRemove = CapabilitySetting.ADMIN,
            editInfo = CapabilitySetting.ADMIN,
            manageInvitations = CapabilitySetting.ADMIN,
            managePermissions = CapabilitySetting.ADMIN,
            promoteAdmin = CapabilitySetting.ADMIN,
            deleteGroup = CapabilitySetting.ADMIN
        )

        allFourteenCapabilities.forEach { cap ->
            val hasPermission = PermissionEvaluator.hasPermission(
                role = GroupRole.MEMBER,
                capability = cap,
                capabilities = lockedCapabilities
            )
            assertFalse(
                "SECURITY VIOLATION: Tampered client as member was NOT denied capability $cap!",
                hasPermission
            )
        }
    }

    // 3. Member allowed when setting is ALL
    @Test
    fun adversarial_member_allowed_when_setting_is_all() {
        val openCapabilities = GroupCapabilities(
            msgSend = CapabilitySetting.ALL,
            msgReply = CapabilitySetting.ALL,
            msgReact = CapabilitySetting.ALL,
            photoShare = CapabilitySetting.ALL,
            fileUpload = CapabilitySetting.ALL,
            mediaDownload = CapabilitySetting.ALL,
            memberInvite = CapabilitySetting.ALL,
            joinApprove = CapabilitySetting.ALL,
            memberRemove = CapabilitySetting.ALL,
            editInfo = CapabilitySetting.ALL,
            manageInvitations = CapabilitySetting.ALL,
            managePermissions = CapabilitySetting.ALL,
            promoteAdmin = CapabilitySetting.ALL,
            deleteGroup = CapabilitySetting.ALL
        )

        allFourteenCapabilities.forEach { cap ->
            val hasPermission = PermissionEvaluator.hasPermission(
                role = GroupRole.MEMBER,
                capability = cap,
                capabilities = openCapabilities
            )
            assertTrue("Member should be allowed $cap when set to ALL", hasPermission)
        }
    }

    // 4. Non-member unconditionally denied across all 14 capabilities
    @Test
    fun adversarial_non_member_unconditionally_denied_all_14_capabilities() {
        val openCapabilities = GroupCapabilities(
            msgSend = CapabilitySetting.ALL,
            photoShare = CapabilitySetting.ALL
        )

        allFourteenCapabilities.forEach { cap ->
            val hasPermission = PermissionEvaluator.hasPermission(
                role = null, // Non-member
                capability = cap,
                capabilities = openCapabilities
            )
            assertFalse("Non-member must be denied capability $cap", hasPermission)
        }
    }

    // 5. FR-PRM-01: Two groups with different settings behave differently for one user
    @Test
    fun fr_prm_01_two_groups_with_different_settings_behave_independently() {
        val groupAOpen = GroupCapabilities(photoShare = CapabilitySetting.ALL)
        val groupBLocked = GroupCapabilities(photoShare = CapabilitySetting.ADMIN)

        val canPostInA = PermissionEvaluator.hasPermission(GroupRole.MEMBER, Capability.PHOTO_SHARE, groupAOpen)
        val canPostInB = PermissionEvaluator.hasPermission(GroupRole.MEMBER, Capability.PHOTO_SHARE, groupBLocked)

        assertTrue("User can post photos in Group A where photoShare = ALL", canPostInA)
        assertFalse("User is silenced from posting photos in Group B where photoShare = ADMIN", canPostInB)
    }
}
