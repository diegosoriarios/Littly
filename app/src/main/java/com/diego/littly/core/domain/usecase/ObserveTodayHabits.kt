package com.diego.littly.core.domain.usecase

import com.diego.littly.core.domain.model.HabitWithProgress
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.streak.StreakCalculator
import com.diego.littly.core.domain.time.TimeProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveTodayHabits @Inject constructor(
    private val habitRepository: HabitRepository,
    private val timeProvider: TimeProvider
) {
    operator fun invoke(): Flow<List<HabitWithProgress>> =
        combine(
            habitRepository.observeActiveHabits(),
            habitRepository.observeAllLogs()
        ) { habits, logs ->
            val today = timeProvider.today()
            val logsByHabit = logs.groupBy { it.habitId }
            habits.map { habit ->
                val completedDays = logsByHabit[habit.id]
                    .orEmpty()
                    .mapTo(mutableSetOf()) { it.date }
                HabitWithProgress(
                    habit = habit,
                    completedToday = today in completedDays,
                    currentStreak = StreakCalculator.currentStreak(
                        completedDays = completedDays,
                        goalDays = habit.goalDays,
                        today = today,
                        createdAt = habit.createdAt
                    ),
                    totalCompletions = completedDays.size
                )
            }
        }
}
