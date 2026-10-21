package com.diego.littly.core.domain.repository

import com.diego.littly.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val onboardingComplete: Flow<Boolean>
    val ideaEngineEnabled: Flow<Boolean>
    val wifiOnlyDownload: Flow<Boolean>
    val themeMode: Flow<ThemeMode>
    val displayName: Flow<String>
    val remindersMasterEnabled: Flow<Boolean>

    suspend fun setOnboardingComplete()
    suspend fun setIdeaEngineEnabled(enabled: Boolean)
    suspend fun setWifiOnlyDownload(enabled: Boolean)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDisplayName(name: String)
    suspend fun setRemindersMasterEnabled(enabled: Boolean)
}
