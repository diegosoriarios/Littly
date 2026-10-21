package com.diego.littly.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.diego.littly.core.domain.model.ThemeMode
import com.diego.littly.core.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val IDEA_ENGINE_ENABLED = booleanPreferencesKey("idea_engine_enabled")
        val WIFI_ONLY_DOWNLOAD = booleanPreferencesKey("wifi_only_download")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val REMINDERS_MASTER_ENABLED = booleanPreferencesKey("reminders_master_enabled")
    }

    override val onboardingComplete: Flow<Boolean> =
        dataStore.data.map { it[Keys.ONBOARDING_COMPLETE] ?: false }

    override val ideaEngineEnabled: Flow<Boolean> =
        dataStore.data.map { it[Keys.IDEA_ENGINE_ENABLED] ?: true }

    override val wifiOnlyDownload: Flow<Boolean> =
        dataStore.data.map { it[Keys.WIFI_ONLY_DOWNLOAD] ?: true }

    override val themeMode: Flow<ThemeMode> =
        dataStore.data.map { prefs ->
            prefs[Keys.THEME_MODE]
                ?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
                ?: ThemeMode.SYSTEM
        }

    override val displayName: Flow<String> =
        dataStore.data.map { it[Keys.DISPLAY_NAME].orEmpty() }

    override val remindersMasterEnabled: Flow<Boolean> =
        dataStore.data.map { it[Keys.REMINDERS_MASTER_ENABLED] ?: true }

    override suspend fun setOnboardingComplete() {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = true }
    }

    override suspend fun setIdeaEngineEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.IDEA_ENGINE_ENABLED] = enabled }
    }

    override suspend fun setWifiOnlyDownload(enabled: Boolean) {
        dataStore.edit { it[Keys.WIFI_ONLY_DOWNLOAD] = enabled }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun setDisplayName(name: String) {
        dataStore.edit { it[Keys.DISPLAY_NAME] = name.trim() }
    }

    override suspend fun setRemindersMasterEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.REMINDERS_MASTER_ENABLED] = enabled }
    }
}
