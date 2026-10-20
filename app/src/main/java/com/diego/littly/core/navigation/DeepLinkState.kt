package com.diego.littly.core.navigation

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class DeepLinkState @Inject constructor() {
    private val _pending = MutableStateFlow<DeepLink?>(null)
    val pending: StateFlow<DeepLink?> = _pending.asStateFlow()

    fun set(link: DeepLink?) {
        _pending.value = link
    }

    fun consume() {
        _pending.value = null
    }
}

sealed class DeepLink {
    data class HabitDetail(val habitId: Long, val generateIdea: Boolean) : DeepLink()
}
