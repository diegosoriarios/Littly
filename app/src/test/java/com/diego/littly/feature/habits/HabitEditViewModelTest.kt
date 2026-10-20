package com.diego.littly.feature.habits

import androidx.lifecycle.SavedStateHandle
import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.HabitLog
import com.diego.littly.core.domain.model.Reminder
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.time.TimeProvider
import com.diego.littly.core.domain.usecase.IdeaQueueFiller
import com.diego.littly.core.notifications.ReminderPlanner
import com.diego.littly.testing.FixedTimeProvider
import com.diego.littly.testing.MainDispatcherRule
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HabitEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today: LocalDate = LocalDate.of(2026, 10, 19)
    private val time: TimeProvider = FixedTimeProvider(today)

    private class FakeHabitRepository(
        var stored: Habit? = null
    ) : HabitRepository {

        data class Added(
            val name: String,
            val goalDays: Set<DayOfWeek>,
            val reminder: Reminder,
            val colorIndex: Int
        )

        var added: Added? = null
        var updated: Habit? = null
        var archived: Pair<Long, Boolean>? = null
        val logs = MutableStateFlow<List<HabitLog>>(emptyList())

        override fun observeActiveHabits(): Flow<List<Habit>> =
            MutableStateFlow(stored?.let { listOf(it) } ?: emptyList())

        override fun observeArchivedHabits(): Flow<List<Habit>> =
            MutableStateFlow(emptyList())

        override fun observeHabit(habitId: Long): Flow<Habit?> = MutableStateFlow(stored)

        override suspend fun getHabit(habitId: Long): Habit? = stored

        override suspend fun getActiveHabits(): List<Habit> =
            stored?.let { listOf(it) } ?: emptyList()

        override suspend fun addHabit(
            name: String,
            goalDays: Set<DayOfWeek>,
            reminder: Reminder,
            colorIndex: Int
        ): Long {
            added = Added(name, goalDays, reminder, colorIndex)
            return 1L
        }

        override suspend fun updateHabit(habit: Habit) {
            updated = habit
        }

        override suspend fun setArchived(habitId: Long, archived: Boolean) {
            this.archived = habitId to archived
        }

        override fun observeLogs(habitId: Long): Flow<List<HabitLog>> = logs

        override fun observeAllLogs(): Flow<List<HabitLog>> = logs

        override suspend fun toggleCompletion(habitId: Long, date: LocalDate, source: com.diego.littly.core.domain.model.CompletionSource) =
            error("not used")
    }

    private class FakeReminderPlanner : ReminderPlanner {
        val rescheduled = mutableListOf<Long>()
        val cancelled = mutableListOf<Long>()

        override suspend fun rescheduleAll() = Unit
        override suspend fun rescheduleHabit(habitId: Long) {
            rescheduled += habitId
        }

        override suspend fun cancelHabit(habitId: Long) {
            cancelled += habitId
        }
    }

    private object NoOpIdeaQueueFiller : IdeaQueueFiller {
        override suspend fun fillQueue(habitId: Long, days: Int) = Unit
        override suspend fun fillAllQueues(days: Int) = Unit
    }

    private fun existingHabit() = Habit(
        id = 5L,
        name = "draw more",
        createdAt = today.minusDays(10),
        goalDays = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
        reminder = Reminder(enabled = true, minuteOfDay = 540),
        colorIndex = 2,
        sortOrder = 3
    )

    private fun addModeViewModel(repo: FakeHabitRepository): HabitEditViewModel =
        HabitEditViewModel(SavedStateHandle(), repo, time, FakeReminderPlanner(), NoOpIdeaQueueFiller)

    private fun editModeViewModel(repo: FakeHabitRepository): HabitEditViewModel =
        HabitEditViewModel(SavedStateHandle(mapOf("habitId" to 5L)), repo, time, FakeReminderPlanner(), NoOpIdeaQueueFiller)

    @Test
    fun addMode_defaults() = runTest {
        val viewModel = addModeViewModel(FakeHabitRepository())

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isEdit)
        assertEquals("", state.name)
        assertEquals(DayOfWeek.entries.toSet(), state.goalDays)
        assertFalse(state.canSave)
    }

    @Test
    fun editMode_prefillsFromRepository() = runTest {
        val repo = FakeHabitRepository(stored = existingHabit())
        val         viewModel = editModeViewModel(repo)

        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isEdit)
        assertFalse(state.isLoading)
        assertEquals("draw more", state.name)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), state.goalDays)
        assertEquals(true, state.reminderEnabled)
        assertEquals(540, state.reminderMinuteOfDay)
        assertEquals(2, state.colorIndex)
    }

    @Test
    fun save_addMode_trimsNameAndSavesReminder() = runTest {
        val repo = FakeHabitRepository()
        val viewModel = addModeViewModel(repo)

        viewModel.onNameChange("  draw more  ")
        viewModel.onGoalDaysChange(setOf(DayOfWeek.MONDAY))
        viewModel.onReminderEnabledChange(true)
        viewModel.onReminderTimeChange(480)
        viewModel.onColorChange(3)
        viewModel.save()
        advanceUntilIdle()

        val added = repo.added!!
        assertEquals("draw more", added.name)
        assertEquals(setOf(DayOfWeek.MONDAY), added.goalDays)
        assertEquals(true, added.reminder.enabled)
        assertEquals(480, added.reminder.minuteOfDay)
        assertEquals(3, added.colorIndex)
        assertTrue(viewModel.state.value.saved)
    }

    @Test
    fun save_addMode_reminderDisabledClearsTime() = runTest {
        val repo = FakeHabitRepository()
        val viewModel = addModeViewModel(repo)

        viewModel.onNameChange("draw more")
        viewModel.onReminderEnabledChange(true)
        viewModel.onReminderTimeChange(480)
        viewModel.onReminderEnabledChange(false)
        viewModel.save()
        advanceUntilIdle()

        val added = repo.added!!
        assertEquals(false, added.reminder.enabled)
        assertNull(added.reminder.minuteOfDay)
    }

    @Test
    fun save_editMode_updatesExistingHabit() = runTest {
        val repo = FakeHabitRepository(stored = existingHabit())
        val viewModel = editModeViewModel(repo)
        advanceUntilIdle()

        viewModel.onNameChange("sketch daily")
        viewModel.onGoalDaysChange(setOf(DayOfWeek.TUESDAY))
        viewModel.save()
        advanceUntilIdle()

        val updated = repo.updated!!
        assertEquals(5L, updated.id)
        assertEquals("sketch daily", updated.name)
        assertEquals(setOf(DayOfWeek.TUESDAY), updated.goalDays)
        assertEquals(today.minusDays(10), updated.createdAt)
        assertEquals(3, updated.sortOrder)
        assertEquals(true, updated.reminder.enabled)
        assertEquals(540, updated.reminder.minuteOfDay)
    }

    @Test
    fun save_addMode_schedulesReminderForNewHabit() = runTest {
        val repo = FakeHabitRepository()
        val planner = FakeReminderPlanner()
        val viewModel = HabitEditViewModel(SavedStateHandle(), repo, time, planner, NoOpIdeaQueueFiller)

        viewModel.onNameChange("draw more")
        viewModel.save()
        advanceUntilIdle()

        assertEquals(listOf(1L), planner.rescheduled)
    }

    @Test
    fun save_blockedWhenNameBlank() = runTest {
        val repo = FakeHabitRepository()
        val viewModel = addModeViewModel(repo)

        viewModel.onNameChange("   ")
        viewModel.save()
        advanceUntilIdle()

        assertNull(repo.added)
        assertFalse(viewModel.state.value.saved)
    }

    @Test
    fun save_blockedWhenNoGoalDays() = runTest {
        val repo = FakeHabitRepository()
        val viewModel = addModeViewModel(repo)

        viewModel.onNameChange("draw more")
        viewModel.onGoalDaysChange(emptySet())
        viewModel.save()
        advanceUntilIdle()

        assertNull(repo.added)
        assertFalse(viewModel.state.value.canSave)
    }
}
