package com.nh.fuel.data

enum class Role { SUPER_ADMIN, ADMIN, MANAGER }
enum class KeyStatus { ACTIVE, REVOKED }

data class StaffAccessKey(
    val id: String = "",
    val accessCode: String = "",          // 8-character code, e.g. "NH78-K92B"
    val nickname: String = "",            // e.g. "Rahul - Shift 1 Lead"
    val role: Role = Role.MANAGER,
    val status: KeyStatus = KeyStatus.ACTIVE,
    val canEditPastDates: Boolean = false,
    val createdBy: String = "",
    val createdAt: String = ""
)

data class AppUserSession(
    val emailOrKey: String = "",
    val displayName: String = "",
    val role: Role = Role.MANAGER,
    val canEditPastDates: Boolean = false,
    val isOwnerLogin: Boolean = false
)
