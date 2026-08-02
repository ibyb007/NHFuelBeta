package com.nh.fuel.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "user_session")

object UserSessionManager {
    private val KEY_EMAIL_OR_KEY = stringPreferencesKey("email_or_key")
    private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
    private val KEY_ROLE = stringPreferencesKey("role")
    private val KEY_CAN_EDIT_PAST = booleanPreferencesKey("can_edit_past")
    private val KEY_IS_OWNER = booleanPreferencesKey("is_owner")
    private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

    suspend fun saveSession(context: Context, session: AppUserSession) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EMAIL_OR_KEY] = session.emailOrKey
            prefs[KEY_DISPLAY_NAME] = session.displayName
            prefs[KEY_ROLE] = session.role.name
            prefs[KEY_CAN_EDIT_PAST] = session.canEditPastDates
            prefs[KEY_IS_OWNER] = session.isOwnerLogin
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun getSavedSession(context: Context): AppUserSession? {
        val prefs = context.dataStore.data.first()
        val isLoggedIn = prefs[KEY_IS_LOGGED_IN] ?: false
        if (!isLoggedIn) return null

        val emailOrKey = prefs[KEY_EMAIL_OR_KEY] ?: return null
        val displayName = prefs[KEY_DISPLAY_NAME] ?: "User"
        val roleStr = prefs[KEY_ROLE] ?: Role.MANAGER.name
        val canEditPast = prefs[KEY_CAN_EDIT_PAST] ?: false
        val isOwner = prefs[KEY_IS_OWNER] ?: false

        val role = try { Role.valueOf(roleStr) } catch (e: Exception) { Role.MANAGER } // FIXED: fallback to MANAGER

        return AppUserSession(
            emailOrKey = emailOrKey,
            displayName = displayName,
            role = role,
            canEditPastDates = canEditPast,
            isOwnerLogin = isOwner
        )
    }

    suspend fun clearSession(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
