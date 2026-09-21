package com.elachi.app.ui.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// This screen provides the user interface for viewing and managing the application's settings.
// It allows users to control appearance settings and access profile, privacy, account deletion, and logout options.
// It also displays unavailable upcoming features such as language selection, biometric security, and push notifications.

private val Context.dataStore by preferencesDataStore(name = "elachi_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        fun darkThemeKey(userId: String?) = booleanPreferencesKey("dark_theme_${userId ?: "guest"}")
        val UNITS = stringPreferencesKey("units")
        val SEEN_ONBOARDING = booleanPreferencesKey("seen_onboarding")
        val ASKED_NOTIF_PERMISSION = booleanPreferencesKey("asked_notif_permission")
    }

    fun isDarkTheme(userId: String?): Flow<Boolean> =
        context.dataStore.data.map { it[Keys.darkThemeKey(userId)] ?: false }

    val units: Flow<String> = context.dataStore.data.map { it[Keys.UNITS] ?: "metric" }
    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map { it[Keys.SEEN_ONBOARDING] ?: false }
    val hasAskedNotificationPermission: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ASKED_NOTIF_PERMISSION] ?: false }

    suspend fun setDarkTheme(userId: String?, enabled: Boolean) {
        context.dataStore.edit { it[Keys.darkThemeKey(userId)] = enabled }
    }

    suspend fun setUnits(units: String) {
        context.dataStore.edit { it[Keys.UNITS] = units }
    }

    suspend fun setSeenOnboarding() {
        context.dataStore.edit { it[Keys.SEEN_ONBOARDING] = true }
    }

    suspend fun setAskedNotificationPermission() {
        context.dataStore.edit { it[Keys.ASKED_NOTIF_PERMISSION] = true }
    }
}