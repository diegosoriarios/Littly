package com.diego.littly.core.domain.model

import java.time.LocalDate

enum class HabitCategory {
    CREATIVE,
    READING,
    LEARNING,
    FITNESS,
    MINDFULNESS,
    SOCIAL,
    PRODUCTIVITY,
    GENERIC
}

enum class IdeaStatus { SPARE, QUEUED, SHOWN, USED }

data class Idea(
    val id: Long = 0,
    val habitId: Long,
    val title: String?,
    val text: String,
    val category: HabitCategory,
    val createdAtMillis: Long,
    val scheduledFor: LocalDate?,
    val status: IdeaStatus,
    val model: String
)

data class GeneratedIdea(
    val title: String?,
    val text: String
)
