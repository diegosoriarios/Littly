package com.diego.littly.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.diego.littly.core.database.LittlyDatabase
import com.diego.littly.core.domain.model.GeneratedIdea
import com.diego.littly.core.domain.model.HabitCategory
import com.diego.littly.core.domain.model.IdeaStatus
import com.diego.littly.testing.FixedTimeProvider
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class IdeaRepositoryTest {

    private lateinit var db: LittlyDatabase
    private lateinit var repository: IdeaRepositoryImpl
    private val time = FixedTimeProvider(LocalDate.of(2026, 10, 19))

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LittlyDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = IdeaRepositoryImpl(db.ideaDao(), time)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun insertHabit(name: String): Long =
        db.habitDao().insert(
            com.diego.littly.core.database.entity.HabitEntity(
                name = name,
                createdAtEpochDay = time.today.toEpochDay(),
                goalDaysBitmask = 0b1111111
            )
        )

    @Test
    fun saveGenerated_storesSpareIdeas() = runTest {
        val habitId = insertHabit("draw more")

        val ids = repository.saveGenerated(
            habitId = habitId,
            category = HabitCategory.CREATIVE,
            model = "gemma-3-1b",
            ideas = listOf(
                GeneratedIdea(title = "Ink garden", text = "Draw a garden using only ink swirls"),
                GeneratedIdea(title = "Reverse zoo", text = "Sketch animals visiting a human zoo")
            )
        )

        assertEquals(2, ids.size)
        val stored = repository.observeForHabit(habitId).first()
        assertEquals(2, stored.size)
        assertEquals(IdeaStatus.SPARE, stored.first().status)
        assertEquals(HabitCategory.CREATIVE, stored.first().category)
        assertEquals("gemma-3-1b", stored.first().model)
    }

    @Test
    fun getRecentTexts_returnsNewestFirst() = runTest {
        val habitId = insertHabit("read more")

        repository.saveGenerated(
            habitId, HabitCategory.READING, "test",
            listOf(GeneratedIdea(null, "first"), GeneratedIdea(null, "second"), GeneratedIdea(null, "third"))
        )

        assertEquals(listOf("third", "second", "first"), repository.getRecentTexts(habitId))
    }

    @Test
    fun scheduleAndMarkShown_controlsQueuedLookup() = runTest {
        val habitId = insertHabit("draw more")
        val ids = repository.saveGenerated(
            habitId, HabitCategory.CREATIVE, "test",
            listOf(GeneratedIdea("t", "queued idea"))
        )
        val date = time.today.plusDays(1)

        repository.scheduleIdea(ids.first(), date)

        val queued = repository.getQueuedIdeaForDate(habitId, date)
        assertEquals("queued idea", queued?.text)

        repository.markShown(ids.first())

        assertNull(repository.getQueuedIdeaForDate(habitId, date))
    }

    @Test
    fun countScheduledDays_countsDistinctDays() = runTest {
        val habitId = insertHabit("draw more")
        val ids = repository.saveGenerated(
            habitId, HabitCategory.CREATIVE, "test",
            (1..3).map { GeneratedIdea(null, "idea $it") }
        )

        repository.scheduleIdea(ids[0], time.today.plusDays(1))
        repository.scheduleIdea(ids[1], time.today.plusDays(1))
        repository.scheduleIdea(ids[2], time.today.plusDays(2))

        val count = repository.countScheduledDays(
            habitId,
            from = time.today,
            to = time.today.plusDays(7)
        )
        assertEquals(2, count)
    }

    @Test
    fun deleteForHabit_removesOnlyThatHabitsIdeas() = runTest {
        val a = insertHabit("habit a")
        val b = insertHabit("habit b")

        repository.saveGenerated(a, HabitCategory.GENERIC, "test", listOf(GeneratedIdea(null, "a1")))
        repository.saveGenerated(b, HabitCategory.GENERIC, "test", listOf(GeneratedIdea(null, "b1")))

        repository.deleteForHabit(a)

        assertEquals(0, repository.observeForHabit(a).first().size)
        assertEquals(1, repository.observeForHabit(b).first().size)
    }
}
