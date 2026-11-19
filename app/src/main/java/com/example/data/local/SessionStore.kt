package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.twogetherSessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "twogether_persistent_session"
)

/**
 * Persisted YouTube-style login session stored in Jetpack DataStore Preferences.
 * Survives app restarts completely offline; only cleared when the user explicitly taps Logout or Switch Profile.
 */
data class PersistedSession(
    val isLoggedIn: Boolean = false,
    val workspaceUid: String = "",
    val roleMode: String = "", // "STUDENT", "EDUCATOR", "ADMIN", or "PROFILE_HUB"
    val entityId: String = "", // StudentProfileEntity.id or EducatorApplicationEntity.id or "admin"
    val userEmail: String = "",
    val userName: String = ""
)

class SessionStore(private val context: Context) {

    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("session_is_logged_in")
        val WORKSPACE_UID = stringPreferencesKey("session_workspace_uid")
        val ROLE_MODE = stringPreferencesKey("session_role_mode")
        val ENTITY_ID = stringPreferencesKey("session_entity_id")
        val USER_EMAIL = stringPreferencesKey("session_user_email")
        val USER_NAME = stringPreferencesKey("session_user_name")
    }

    val sessionFlow: Flow<PersistedSession> = context.twogetherSessionDataStore.data.map { prefs ->
        PersistedSession(
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: false,
            workspaceUid = prefs[Keys.WORKSPACE_UID].orEmpty(),
            roleMode = prefs[Keys.ROLE_MODE].orEmpty(),
            entityId = prefs[Keys.ENTITY_ID].orEmpty(),
            userEmail = prefs[Keys.USER_EMAIL].orEmpty(),
            userName = prefs[Keys.USER_NAME].orEmpty()
        )
    }

    suspend fun getSessionOnce(): PersistedSession = sessionFlow.first()

    suspend fun saveSession(
        workspaceUid: String,
        roleMode: String,
        entityId: String,
        userEmail: String = "",
        userName: String = ""
    ) {
        context.twogetherSessionDataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = true
            prefs[Keys.WORKSPACE_UID] = workspaceUid
            prefs[Keys.ROLE_MODE] = roleMode
            prefs[Keys.ENTITY_ID] = entityId
            prefs[Keys.USER_EMAIL] = userEmail
            prefs[Keys.USER_NAME] = userName
        }
    }

    suspend fun clearSession() {
        context.twogetherSessionDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
