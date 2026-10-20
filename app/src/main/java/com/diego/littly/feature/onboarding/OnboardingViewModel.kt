package com.diego.littly.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diego.littly.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    fun onNotificationPermissionResult(granted: Boolean) {
        if (granted) {
            viewModelScope.launch {
                settingsRepository.setRemindersMasterEnabled(true)
            }
        }
    }

    fun complete() {
        viewModelScope.launch {
            settingsRepository.setOnboardingComplete()
        }
    }
}
