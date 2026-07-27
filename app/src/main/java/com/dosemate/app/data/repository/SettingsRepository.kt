package com.dosemate.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "dosemate_settings")

data class AppSettings(
    val generalAlarmsEnabled: Boolean = true,
    val refillAlertsEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    val themeMode: String = "Light",
    val textSize: String = "Medium"
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val GENERAL_ALARMS = booleanPreferencesKey("general_alarms")
        val REFILL_ALERTS = booleanPreferencesKey("refill_alerts")
        val QUIET_START = stringPreferencesKey("quiet_start")
        val QUIET_END = stringPreferencesKey("quiet_end")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val TEXT_SIZE = stringPreferencesKey("text_size")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            generalAlarmsEnabled = prefs[Keys.GENERAL_ALARMS] ?: true,
            refillAlertsEnabled = prefs[Keys.REFILL_ALERTS] ?: false,
            quietHoursStart = prefs[Keys.QUIET_START] ?: "22:00",
            quietHoursEnd = prefs[Keys.QUIET_END] ?: "07:00",
            themeMode = prefs[Keys.THEME_MODE] ?: "Light",
            textSize = prefs[Keys.TEXT_SIZE] ?: "Medium"
        )
    }

    suspend fun setGeneralAlarms(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.GENERAL_ALARMS] = enabled }
    }

    suspend fun setRefillAlerts(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.REFILL_ALERTS] = enabled }
    }

    suspend fun setQuietHours(start: String, end: String) {
        context.settingsDataStore.edit {
            it[Keys.QUIET_START] = start
            it[Keys.QUIET_END] = end
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.settingsDataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setTextSize(size: String) {
        context.settingsDataStore.edit { it[Keys.TEXT_SIZE] = size }
    }

    suspend fun resetToDefaults() {
        context.settingsDataStore.edit { it.clear() }
    }
}
