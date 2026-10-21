package com.diego.littly.core.domain.usecase

import com.diego.littly.core.domain.model.CompletionSource
import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.HabitLog
import com.diego.littly.core.domain.model.Reminder
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.time.TimeProvider
import com.diego.littly.testing.FixedTimeProvider
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveTodayHabitsTest {

    private val today: LocalDate = LocalDate.of(2026, 10, 19)
    private val time = FixedTimeProvider(today)

    private class FakeHabitRepository : HabitRepository {
        val habits = MutableStateFlow<List<Habit>>(emptyList())
        val logs = MutableStateFlow<List<HabitLog>>(emptyList())

        override fun observeActiveHabits(): Flow<List<Habit>> = habits
        override fun observeArchivedHabits(): Flow<List<Habit>> = MutableStateFlow(emptyList())
        override fun observeHabit(habitId: Long): Flow<Habit?> = MutableStateFlow(null)
        override suspend fun getHabit(habitId: Long): Habit? = null
        override suspend fun getActiveHabits(): List<Habit> =
            habits.value.filter { it.archivedAt == null }
        override suspend fun addHabit(
            name: String,
            goalDays: Set<DayOfWeek>,
            reminder: Reminder,
            colorIndex: Int,
            description: String?,
            iconKey: String
        ): Long = error("not used")

        override suspend fun updateHabit(habit: Habit) = error("not used")
        override suspend fun setArchived(habitId: Long, archived: Boolean) = error("not used")
        override fun observeLogs(habitId: Long): Flow<List<HabitLog>> =
            MutableStateFlow(logs.value.filter { it.habitId == habitId })

        override fun observeAllLogs(): Flow<List<HabitLog>> = logs
        override suspend fun toggleCompletion(habitId: Long, date: LocalDate, source: CompletionSource) =
            error("not used")
    }

    private fun habit(
        id: Long,
        name: String,
        goalDays: Set<DayOfWeek> = DayOfWeek.entries.toSet()
    ): Habit = Habit(
        id = id,
        name = name,
        createdAt = today.minusDays(30),
        goalDays = goalDays
    )

    @Test
    fun mapsCompletionStreakAndTotals() = runTest {
        val repo = FakeHabitRepository()
        val draw = habit(1, "draw more")
        val read = habit(2, "read more", setOf(DayOfWeek.MONDAY))
        repo.habits.value = listOf(draw, read)
        repo.logs.value = listOf(
            HabitLog(1, today, 0, CompletionSource.APP),
            HabitLog(1, today.minusDays(1), 0, CompletionSource.APP),
            HabitLog(1, today.minusDays(2), 0, CompletionSource.APP),
            HabitLog(2, today.minusDays(7), 0, CompletionSource.WIDGET)
        )
        val useCase = ObserveTodayHabits(repo, time)

        val result = useCase().first()

        val drawProgress = result.first { it.habit.id == 1L }
        assertEquals(true, drawProgress.completedToday)
        assertEquals(3, drawProgress.currentStreak)
        assertEquals(3, drawProgress.totalCompletions)

        val readProgress = result.first { it.habit.id == 2L }
        assertEquals(false, readProgress.completedToday)
        assertEquals(1, readProgress.currentStreak)
        assertEquals(1, readProgress.totalCompletions)
    }

    @Test
    fun emptyState_returnsEmptyList() = runTest {
        val useCase = ObserveTodayHabits(FakeHabitRepository(), time)

        assertEquals(emptyList<Nothing>(), useCase().first())
    }

    @Test
    fun reactsToNewLogs() = runTest {
        val repo = FakeHabitRepository()
        repo.habits.value = listOf(habit(1, "draw more"))
        val useCase = ObserveTodayHabits(repo, time)

        val before = useCase().first().single()
        assertEquals(false, before.completedToday)

        repo.logs.value = listOf(HabitLog(1, today, 0, CompletionSource.APP))

        val after = useCase().first().single()
        assertEquals(true, after.completedToday)
        assertEquals(1, after.currentStreak)
    }
}
