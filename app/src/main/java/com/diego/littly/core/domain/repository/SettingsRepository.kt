package com.diego.littly.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val onboardingComplete: Flow<Boolean>
    val ideaEngineEnabled: Flow<Boolean>
    val wifiOnlyDownload: Flow<Boolean>
    val dynamicColorEnabled: Flow<Boolean>
    val remindersMasterEnabled: Flow<Boolean>

    suspend fun setOnboardingComplete()
    suspend fun setIdeaEngineEnabled(enabled: Boolean)
    suspend fun setWifiOnlyDownload(enabled: Boolean)
    suspend fun setDynamicColorEnabled(enabled: Boolean)
    suspend fun setRemindersMasterEnabled(enabled: Boolean)
}
