package com.diego.littly.feature.settings

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diego.littly.core.domain.model.ThemeMode
import com.diego.littly.core.domain.repository.SettingsRepository
import com.diego.littly.core.download.ModelDownloadRepository
import com.diego.littly.core.download.ModelDownloadState
import com.diego.littly.core.notifications.ReminderPlanner
import com.diego.littly.widget.WidgetUpdateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PermissionSnapshot(
    val notificationsGranted: Boolean = false,
    val exactAlarmsAllowed: Boolean = false,
    val batteryExempt: Boolean = false
)

data class SettingsState(
    val isLoading: Boolean = true,
    val remindersMasterEnabled: Boolean = true,
    val ideaEngineEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val displayName: String = "",
    val permissions: PermissionSnapshot = PermissionSnapshot(),
    val modelDownloadState: ModelDownloadState = ModelDownloadState.Idle,
    val modelPresent: Boolean = false,
    val modelSizeBytes: Long = 0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val settingsRepository: SettingsRepository,
    private val reminderPlanner: ReminderPlanner,
    private val modelDownloadRepository: ModelDownloadRepository,
    private val widgetUpdateHelper: WidgetUpdateHelper
) : ViewModel() {

    private val permissions = MutableStateFlow(PermissionSnapshot())

    val state: StateFlow<SettingsState> =
        combine(
            settingsRepository.remindersMasterEnabled,
            settingsRepository.ideaEngineEnabled,
            settingsRepository.themeMode,
            settingsRepository.displayName,
            permissions
        ) { master, idea, theme, name, perms ->
            SettingsState(
                isLoading = false,
                remindersMasterEnabled = master,
                ideaEngineEnabled = idea,
                themeMode = theme,
                displayName = name,
                permissions = perms
            )
        }.combine(modelDownloadRepository.downloadState) { state, downloadState ->
            state.copy(
                modelDownloadState = downloadState,
                modelPresent = modelDownloadRepository.isModelPresent(),
                modelSizeBytes = modelDownloadRepository.getModelSize()
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsState()
        )

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        permissions.value = snapshotPermissions(appContext)
    }

    fun setRemindersMasterEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setRemindersMasterEnabled(enabled)
            reminderPlanner.rescheduleAll()
        }
    }

    fun setIdeaEngineEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setIdeaEngineEnabled(enabled)
            widgetUpdateHelper.updateAllWidgets()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
            widgetUpdateHelper.updateAllWidgets()
        }
    }

    fun setDisplayName(name: String) {
        viewModelScope.launch {
            settingsRepository.setDisplayName(name)
        }
    }

    fun downloadModel() {
        viewModelScope.launch {
            modelDownloadRepository.startDownload()
            widgetUpdateHelper.updateAllWidgets()
        }
    }

    fun cancelDownload() {
        viewModelScope.launch {
            modelDownloadRepository.cancelDownload()
            widgetUpdateHelper.updateAllWidgets()
        }
    }

    fun deleteModel() {
        viewModelScope.launch {
            modelDownloadRepository.deleteModel()
            widgetUpdateHelper.updateAllWidgets()
        }
    }
}

fun snapshotPermissions(context: Context): PermissionSnapshot = PermissionSnapshot(
    notificationsGranted = NotificationManagerCompat.from(context).areNotificationsEnabled(),
    exactAlarmsAllowed = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        true
    } else {
        context.getSystemService<AlarmManager>()?.canScheduleExactAlarms() ?: false
    },
    batteryExempt = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        true
    } else {
        val pm = context.getSystemService<android.os.PowerManager>()
        pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }
)
