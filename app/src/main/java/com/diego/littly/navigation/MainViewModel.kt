package com.diego.littly.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diego.littly.core.domain.model.ThemeMode
import com.diego.littly.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {

    val startDestination: StateFlow<String?> =
        settingsRepository.onboardingComplete
            .map { complete -> if (complete) LittlyRoutes.TODAY else LittlyRoutes.ONBOARDING }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )

    val themeMode: StateFlow<ThemeMode> =
        settingsRepository.themeMode
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = ThemeMode.SYSTEM
            )
}
