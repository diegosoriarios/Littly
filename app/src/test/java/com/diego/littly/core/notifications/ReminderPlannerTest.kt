package com.diego.littly.core.notifications

import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.Reminder
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.repository.SettingsRepository
import com.diego.littly.core.domain.time.TimeProvider
import com.diego.littly.testing.FixedTimeProvider
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderPlannerTest {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val today: LocalDate = LocalDate.of(2026, 10, 21)
    private val nowMillis: Long =
        ZonedDateTime.of(today, java.time.LocalTime.of(10, 0), zone).toInstant().toEpochMilli()
    private val time: TimeProvider = FixedTimeProvider(today, baseMillis = nowMillis)

    private class FakeAlarmScheduler : AlarmScheduler {
        val scheduled = mutableListOf<Pair<Long, Long>>()
        val cancelled = mutableSetOf<Long>()

        override fun schedule(habitId: Long, triggerAtMillis: Long) {
            scheduled.add(habitId to triggerAtMillis)
            cancelled.remove(habitId)
        }

        override fun cancel(habitId: Long) {
            cancelled.add(habitId)
        }
    }

    private class FakeSettingsRepository(
        masterEnabled: Boolean = true
    ) : SettingsRepository {
        val master = MutableStateFlow(masterEnabled)

        override val onboardingComplete: Flow<Boolean> = MutableStateFlow(true)
        override val ideaEngineEnabled: Flow<Boolean> = MutableStateFlow(true)
        override val wifiOnlyDownload: Flow<Boolean> = MutableStateFlow(true)
        override val dynamicColorEnabled: Flow<Boolean> = MutableStateFlow(false)
        override val remindersMasterEnabled: Flow<Boolean> = master

        override suspend fun setOnboardingComplete() = Unit
        override suspend fun setIdeaEngineEnabled(enabled: Boolean) = Unit
        override suspend fun setWifiOnlyDownload(enabled: Boolean) = Unit
        override suspend fun setDynamicColorEnabled(enabled: Boolean) = Unit
        override suspend fun setRemindersMasterEnabled(enabled: Boolean) {
            master.value = enabled
        }
    }

    private class FakeHabitRepository(
        var habits: List<Habit> = emptyList()
    ) : HabitRepository {
        override fun observeActiveHabits(): Flow<List<Habit>> = MutableStateFlow(habits)
        override fun observeArchivedHabits(): Flow<List<Habit>> = MutableStateFlow(emptyList())
        override fun observeHabit(habitId: Long): Flow<Habit?> =
            MutableStateFlow(habits.firstOrNull { it.id == habitId })

        override suspend fun getHabit(habitId: Long): Habit? =
            habits.firstOrNull { it.id == habitId }

        override suspend fun getActiveHabits(): List<Habit> = habits
        override suspend fun addHabit(
            name: String,
            goalDays: Set<DayOfWeek>,
            reminder: Reminder,
            colorIndex: Int
        ): Long = error("not used")

        override suspend fun updateHabit(habit: Habit) = error("not used")
        override suspend fun setArchived(habitId: Long, archived: Boolean) = error("not used")
        override fun observeLogs(habitId: Long): Flow<List<com.diego.littly.core.domain.model.HabitLog>> =
            MutableStateFlow(emptyList())

        override fun observeAllLogs(): Flow<List<com.diego.littly.core.domain.model.HabitLog>> =
            MutableStateFlow(emptyList())

        override suspend fun toggleCompletion(
            habitId: Long,
            date: LocalDate,
            source: com.diego.littly.core.domain.model.CompletionSource
        ) = error("not used")
    }

    private fun habit(
        id: Long,
        reminder: Reminder
    ): Habit = Habit(
        id = id,
        name = "habit $id",
        createdAt = today.minusDays(5),
        goalDays = DayOfWeek.entries.toSet(),
        reminder = reminder
    )

    @Test
    fun rescheduleAll_schedulesEnabledRemindersWithExpectedTrigger() = runTest {
        val enabledHabit = habit(1, Reminder(enabled = true, minuteOfDay = 9 * 60))
        val repo = FakeHabitRepository(listOf(enabledHabit))
        val scheduler = FakeAlarmScheduler()
        val planner = ReminderPlannerImpl(repo, FakeSettingsRepository(), scheduler, time)

        planner.rescheduleAll()

        val expected = ReminderTime.nextTriggerFromMillis(
            effectiveDays = DayOfWeek.entries.toSet(),
            minuteOfDay = 9 * 60,
            nowMillis = nowMillis,
            zone = zone
        )
        assertEquals(listOf(1L to expected), scheduler.scheduled)
        assertTrue(expected > nowMillis)
    }

    @Test
    fun rescheduleAll_cancelsDisabledReminders() = runTest {
        val enabled = habit(1, Reminder(enabled = true, minuteOfDay = 9 * 60))
        val disabled = habit(2, Reminder(enabled = false))
        val noTime = habit(3, Reminder(enabled = true, minuteOfDay = null))
        val scheduler = FakeAlarmScheduler()
        val planner = ReminderPlannerImpl(
            FakeHabitRepository(listOf(enabled, disabled, noTime)),
            FakeSettingsRepository(),
            scheduler,
            time
        )

        planner.rescheduleAll()

        assertEquals(listOf(1L), scheduler.scheduled.map { it.first })
        assertTrue(2L in scheduler.cancelled)
        assertTrue(3L in scheduler.cancelled)
    }

    @Test
    fun rescheduleAll_masterOff_cancelsEverything() = runTest {
        val enabled = habit(1, Reminder(enabled = true, minuteOfDay = 9 * 60))
        val scheduler = FakeAlarmScheduler()
        val planner = ReminderPlannerImpl(
            FakeHabitRepository(listOf(enabled)),
            FakeSettingsRepository(masterEnabled = false),
            scheduler,
            time
        )

        planner.rescheduleAll()

        assertTrue(scheduler.scheduled.isEmpty())
        assertTrue(1L in scheduler.cancelled)
    }

    @Test
    fun rescheduleHabit_missingHabit_cancelsAlarm() = runTest {
        val scheduler = FakeAlarmScheduler()
        val planner = ReminderPlannerImpl(FakeHabitRepository(), FakeSettingsRepository(), scheduler, time)

        planner.rescheduleHabit(42)

        assertTrue(42L in scheduler.cancelled)
    }

    @Test
    fun rescheduleHabit_usesReminderDaysOverGoalDays() = runTest {
        val reminderDays = setOf(DayOfWeek.SUNDAY)
        val enabled = Habit(
            id = 1,
            name = "draw",
            createdAt = today.minusDays(5),
            goalDays = DayOfWeek.entries.toSet(),
            reminder = Reminder(enabled = true, minuteOfDay = 9 * 60, days = reminderDays)
        )
        val scheduler = FakeAlarmScheduler()
        val planner = ReminderPlannerImpl(
            FakeHabitRepository(listOf(enabled)),
            FakeSettingsRepository(),
            scheduler,
            time
        )

        planner.rescheduleHabit(1)

        val expected = ReminderTime.nextTriggerFromMillis(
            effectiveDays = reminderDays,
            minuteOfDay = 9 * 60,
            nowMillis = nowMillis,
            zone = zone
        )
        assertEquals(listOf(1L to expected), scheduler.scheduled)
    }
}
