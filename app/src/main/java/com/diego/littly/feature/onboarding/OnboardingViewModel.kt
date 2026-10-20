package com.diego.littly.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diego.littly.core.domain.repository.SettingsRepository
import com.diego.littly.core.download.ModelDownloadRepository
import com.diego.littly.core.download.ModelDownloadState
import com.diego.littly.core.llm.ModelLocator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val modelDownloadRepository: ModelDownloadRepository,
    private val modelLocator: ModelLocator
) : ViewModel() {

    val downloadState: StateFlow<ModelDownloadState> = modelDownloadRepository.downloadState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ModelDownloadState.Idle
        )

    fun onNotificationPermissionResult(granted: Boolean) {
        if (granted) {
            viewModelScope.launch {
                settingsRepository.setRemindersMasterEnabled(true)
            }
        }
    }

    fun startModelDownload() {
        viewModelScope.launch {
            modelDownloadRepository.startDownload()
        }
    }

    fun complete() {
        viewModelScope.launch {
            settingsRepository.setOnboardingComplete()
        }
    }
}
