package com.snapfilters.feature.groups.model

enum class JoiningPolicy {
    DIRECT,
    APPROVAL
}

enum class GroupRole {
    ADMIN,
    MEMBER
}

data class Group(
    val id: String,
    val name: String,
    val description: String?,
    val imagePath: String?,
    val ownerId: String,
    val joiningPolicy: JoiningPolicy = JoiningPolicy.APPROVAL,
    val memberCount: Int = 1,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

data class GroupInvitation(
    val id: String,
    val groupId: String,
    val rawToken: String, // Kept only at mint time, never readable from DB
    val tokenHashHex: String,
    val expiresAtEpochMillis: Long,
    val maxUses: Int? = 200,
    val usedCount: Int = 0,
    val isRevoked: Boolean = false
)

data class JoinRequest(
    val id: String,
    val groupId: String,
    val userId: String,
    val userDisplayName: String,
    val state: JoinRequestState = JoinRequestState.PENDING,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

enum class JoinRequestState {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}

sealed interface RedeemResult {
    data object Invalid : RedeemResult
    data object Expired : RedeemResult
    data object Exhausted : RedeemResult
    data object GroupGone : RedeemResult
    data class AlreadyMember(val groupId: String) : RedeemResult
    data class AlreadyPending(val groupId: String) : RedeemResult
    data class Joined(val group: Group) : RedeemResult
    data class Requested(val group: Group) : RedeemResult
}
