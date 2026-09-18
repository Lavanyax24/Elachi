package com.elachi.app.ui.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "elachi_settings")

/**
 * Persists lightweight user preferences on-device. Theme and unit
 * preference are used by the app today; the notification-permission flag
 * stops us asking for POST_NOTIFICATIONS on every launch.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val UNITS = stringPreferencesKey("units") // "metric" | "imperial"
        val SEEN_ONBOARDING = booleanPreferencesKey("seen_onboarding")
        val ASKED_NOTIF_PERMISSION = booleanPreferencesKey("asked_notif_permission")
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { it[Keys.DARK_THEME] ?: false }
    val units: Flow<String> = context.dataStore.data.map { it[Keys.UNITS] ?: "metric" }
    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map { it[Keys.SEEN_ONBOARDING] ?: false }
    val hasAskedNotificationPermission: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ASKED_NOTIF_PERMISSION] ?: false }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }
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