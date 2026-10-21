package com.diego.littly.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diego.littly.core.domain.repository.SettingsRepository
import com.diego.littly.core.download.ModelDownloadRepository
import com.diego.littly.core.download.ModelDownloadState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val modelDownloadRepository: ModelDownloadRepository
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    val downloadState: StateFlow<ModelDownloadState> = modelDownloadRepository.downloadState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ModelDownloadState.Idle
        )

    fun onNameChange(name: String) {
        _name.value = name.take(40)
    }

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

    fun cancelDownload() {
        viewModelScope.launch {
            modelDownloadRepository.cancelDownload()
        }
    }

    fun retryDownload() {
        viewModelScope.launch {
            modelDownloadRepository.cancelDownload()
            kotlinx.coroutines.delay(500)
            modelDownloadRepository.startDownload()
        }
    }

    fun complete(onComplete: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.setDisplayName(_name.value.trim())
            settingsRepository.setOnboardingComplete()
            onComplete()
        }
    }
}
