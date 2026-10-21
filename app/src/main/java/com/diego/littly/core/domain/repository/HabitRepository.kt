package com.diego.littly.core.domain.repository

import com.diego.littly.core.domain.model.CompletionSource
import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.HabitLog
import com.diego.littly.core.domain.model.Reminder
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun observeActiveHabits(): Flow<List<Habit>>
    fun observeArchivedHabits(): Flow<List<Habit>>
    fun observeHabit(habitId: Long): Flow<Habit?>
    suspend fun getHabit(habitId: Long): Habit?
    suspend fun getActiveHabits(): List<Habit>
    suspend fun addHabit(
        name: String,
        goalDays: Set<DayOfWeek>,
        reminder: Reminder,
        colorIndex: Int,
        description: String? = null,
        iconKey: String = com.diego.littly.core.domain.model.HabitIcon.default.key
    ): Long

    suspend fun updateHabit(habit: Habit)
    suspend fun setArchived(habitId: Long, archived: Boolean)
    fun observeLogs(habitId: Long): Flow<List<HabitLog>>
    fun observeAllLogs(): Flow<List<HabitLog>>
    suspend fun toggleCompletion(habitId: Long, date: LocalDate, source: CompletionSource)
}
