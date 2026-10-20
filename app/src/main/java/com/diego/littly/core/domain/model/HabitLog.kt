package com.diego.littly.core.domain.model

import java.time.LocalDate

enum class CompletionSource { APP, WIDGET, NOTIFICATION }

data class HabitLog(
    val habitId: Long,
    val date: LocalDate,
    val completedAtMillis: Long,
    val source: CompletionSource
)
