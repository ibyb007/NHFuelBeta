package com.nh.fuel.data

enum class Role { ADMIN, MANAGER }
enum class AccountStatus { ACTIVE, SUSPENDED }

data class AppUser(
    val email: String = "",
    val role: Role = Role.MANAGER,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val canEditPastDates: Boolean = false,
    val createdAt: String = ""
)
