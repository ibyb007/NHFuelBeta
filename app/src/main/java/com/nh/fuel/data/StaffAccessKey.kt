package com.nh.fuel.data

import androidx.annotation.Keep

enum class KeyStatus {
    ACTIVE,
    REVOKED
}

enum class Role {
    SUPER_ADMIN,
    ADMIN,
    MANAGER
}

@Keep
data class StaffAccessKey(
    val id: String = "",
    val accessCode: String = "",
    val nickname: String = "",
    val role: Role = Role.MANAGER,
    val status: KeyStatus = KeyStatus.ACTIVE,
    val canEditPastDates: Boolean = false,
    val canEditFinancePastDates: Boolean = false, // Dedicated Finance Past-Date Toggle
    val isReadOnly: Boolean = false,
    val createdAt: String = ""
)

@Keep
data class AppUserSession(
    val emailOrKey: String = "",
    val displayName: String = "",
    val role: Role = Role.MANAGER,
    val canEditPastDates: Boolean = false,
    val canEditFinancePastDates: Boolean = false,
    val isReadOnly: Boolean = false,
    val isOwnerLogin: Boolean = false
)
