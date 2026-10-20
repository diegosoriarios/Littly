package com.diego.littly.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.diego.littly.core.database.LittlyDatabase
import com.diego.littly.core.domain.model.CompletionSource
import com.diego.littly.core.domain.model.Reminder
import com.diego.littly.testing.FixedTimeProvider
import com.diego.littly.widget.WidgetUpdateHelper
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HabitRepositoryTest {

    private lateinit var db: LittlyDatabase
    private lateinit var repository: HabitRepositoryImpl
    private val time = FixedTimeProvider(LocalDate.of(2026, 10, 19))
    private val fakeWidgetUpdateHelper = object : WidgetUpdateHelper(
        ApplicationProvider.getApplicationContext()
    ) {
        override suspend fun updateAllWidgets() {
            // No-op for tests
        }
    }

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LittlyDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = HabitRepositoryImpl(db.habitDao(), db.habitLogDao(), time, fakeWidgetUpdateHelper)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun addHabit_persistsFieldsAndSortOrder() = runTest {
        val drawId = repository.addHabit(
            name = "  draw more  ",
            goalDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            reminder = Reminder(enabled = true, minuteOfDay = 8 * 60 + 30),
            colorIndex = 2
        )
        val readId = repository.addHabit(
            name = "read more",
            goalDays = DayOfWeek.entries.toSet(),
            reminder = Reminder(),
            colorIndex = 0
        )

        val draw = repository.getHabit(drawId)!!
        val read = repository.getHabit(readId)!!

        assertEquals("draw more", draw.name)
        assertEquals(time.today, draw.createdAt)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), draw.goalDays)
        assertEquals(true, draw.reminder.enabled)
        assertEquals(8 * 60 + 30, draw.reminder.minuteOfDay)
        assertEquals(2, draw.colorIndex)
        assertEquals(0, draw.sortOrder)
        assertEquals(1, read.sortOrder)
    }

    @Test
    fun toggleCompletion_insertsThenRemovesLog() = runTest {
        val id = repository.addHabit("draw more", DayOfWeek.entries.toSet(), Reminder(), 0)

        repository.toggleCompletion(id, time.today, CompletionSource.APP)
        val afterInsert = repository.observeLogs(id).first()

        assertEquals(1, afterInsert.size)
        assertEquals(time.today, afterInsert.first().date)
        assertEquals(CompletionSource.APP, afterInsert.first().source)

        repository.toggleCompletion(id, time.today, CompletionSource.APP)
        val afterRemove = repository.observeLogs(id).first()

        assertTrue(afterRemove.isEmpty())
    }

    @Test
    fun toggleCompletion_storesWidgetSource() = runTest {
        val id = repository.addHabit("draw more", DayOfWeek.entries.toSet(), Reminder(), 0)

        repository.toggleCompletion(id, time.today, CompletionSource.WIDGET)

        val log = repository.observeLogs(id).first().single()
        assertEquals(CompletionSource.WIDGET, log.source)
    }

    @Test
    fun setArchived_movesHabitBetweenLists() = runTest {
        val id = repository.addHabit("draw more", DayOfWeek.entries.toSet(), Reminder(), 0)

        repository.setArchived(id, archived = true)

        assertTrue(repository.observeActiveHabits().first().isEmpty())
        assertEquals(1, repository.observeArchivedHabits().first().size)
        assertEquals(time.today, repository.getHabit(id)!!.archivedAt)

        repository.setArchived(id, archived = false)

        assertEquals(1, repository.observeActiveHabits().first().size)
        assertNull(repository.getHabit(id)!!.archivedAt)
    }

    @Test
    fun updateHabit_persistsChanges() = runTest {
        val id = repository.addHabit(
            "draw more",
            setOf(DayOfWeek.MONDAY),
            Reminder(),
            0
        )
        val original = repository.getHabit(id)!!

        repository.updateHabit(
            original.copy(
                name = "sketch every morning",
                goalDays = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                reminder = Reminder(enabled = true, minuteOfDay = 480, days = setOf(DayOfWeek.TUESDAY))
            )
        )

        val updated = repository.getHabit(id)!!
        assertEquals("sketch every morning", updated.name)
        assertEquals(setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), updated.goalDays)
        assertEquals(true, updated.reminder.enabled)
        assertEquals(setOf(DayOfWeek.TUESDAY), updated.reminder.days)
        assertEquals(480, updated.reminder.minuteOfDay)
    }

    @Test
    fun observeAllLogs_aggregatesAcrossHabits() = runTest {
        val a = repository.addHabit("habit a", DayOfWeek.entries.toSet(), Reminder(), 0)
        val b = repository.addHabit("habit b", DayOfWeek.entries.toSet(), Reminder(), 0)

        repository.toggleCompletion(a, time.today, CompletionSource.APP)
        repository.toggleCompletion(b, time.today.minusDays(1), CompletionSource.APP)

        assertEquals(2, repository.observeAllLogs().first().size)
    }
}
