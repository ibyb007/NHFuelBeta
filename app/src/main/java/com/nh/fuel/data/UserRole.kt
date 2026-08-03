package com.nh.fuel.data

enum class Role { SUPER_ADMIN, ADMIN, MANAGER }
enum class KeyStatus { ACTIVE, REVOKED }

@com.google.firebase.firestore.IgnoreExtraProperties
data class StaffAccessKey(
    val id: String = "",
    val accessCode: String = "",
    val nickname: String = "",
    val role: Role = Role.MANAGER,
    val status: KeyStatus = KeyStatus.ACTIVE,
    val canEditPastDates: Boolean = false,
    @get:com.google.firebase.firestore.PropertyName("isReadOnly")
    @set:com.google.firebase.firestore.PropertyName("isReadOnly")
    var isReadOnly: Boolean = false, // Read-only privilege toggle for managers
    val createdBy: String = "",
    val createdAt: String = ""
)

data class AppUserSession(
    val emailOrKey: String = "",
    val displayName: String = "",
    val role: Role = Role.MANAGER,
    val canEditPastDates: Boolean = false,
    val isReadOnly: Boolean = false,
    val isOwnerLogin: Boolean = false
)
