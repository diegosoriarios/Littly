package com.diego.littly.core.domain.model

data class HabitWithProgress(
    val habit: Habit,
    val completedToday: Boolean,
    val currentStreak: Int,
    val totalCompletions: Int
)
