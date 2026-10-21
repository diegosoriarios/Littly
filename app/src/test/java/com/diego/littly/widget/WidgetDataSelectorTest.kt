package com.diego.littly.widget

import com.diego.littly.core.domain.model.CompletionSource
import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.HabitCategory
import com.diego.littly.core.domain.model.HabitLog
import com.diego.littly.core.domain.model.Idea
import com.diego.littly.core.domain.model.IdeaStatus
import com.diego.littly.core.domain.model.ThemeMode
import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetDataSelectorTest {

    private val today = LocalDate.of(2026, 10, 21)

    private fun habit(
        id: Long,
        name: String,
        goalDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
        createdAt: LocalDate = today.minusDays(10)
    ) = Habit(
        id = id,
        name = name,
        createdAt = createdAt,
        goalDays = goalDays
    )

    private fun log(habitId: Long, date: LocalDate) =
        HabitLog(habitId, date, 0L, CompletionSource.APP)

    @Test
    fun itemLimit_adaptsToAvailableHeight() {
        assertEquals(0, WidgetDataSelector.itemLimit(100f))
        assertEquals(1, WidgetDataSelector.itemLimit(120f))
        assertEquals(2, WidgetDataSelector.itemLimit(180f))
        assertEquals(3, WidgetDataSelector.itemLimit(230f))
        assertEquals(5, WidgetDataSelector.itemLimit(320f))
    }

    @Test
    fun overview_includesOnlyHabitsScheduledToday() {
        val daily = habit(1, "draw")
        val wednesdayOnly = habit(2, "read", setOf(DayOfWeek.WEDNESDAY))
        val mondayOnly = habit(3, "run", setOf(DayOfWeek.MONDAY))
        val logs = listOf(log(1, today))

        val data = WidgetDataSelector.overview(
            habits = listOf(daily, wednesdayOnly, mondayOnly),
            logs = logs,
            today = today,
            heightDp = 200f
        )

        assertEquals(2, data.due)
        assertEquals(1, data.completed)
        assertEquals(2, data.items.size)
        assertTrue(data.items.first { it.habitId == 1L }.completed)
        assertFalse(data.items.first { it.habitId == 2L }.completed)
    }

    @Test
    fun overview_selectsBestCurrentStreak() {
        val draw = habit(1, "draw")
        val read = habit(2, "read")
        val logs = listOf(
            log(1, today),
            log(2, today.minusDays(2)),
            log(2, today.minusDays(1)),
            log(2, today)
        )

        val data = WidgetDataSelector.overview(listOf(draw, read), logs, today, 200f)

        assertEquals(3, data.bestStreak)
        assertEquals("read", data.bestStreakName)
    }

    @Test
    fun streak_returnsNullWhenThereIsNoActiveStreak() {
        val draw = habit(1, "draw")

        assertNull(WidgetDataSelector.streak(listOf(draw), emptyList(), today))
    }

    @Test
    fun idea_isUnavailableWhenEngineIsOff() {
        val data = WidgetDataSelector.idea(listOf(habit(1, "draw")), emptyList(), false)

        assertFalse(data.enabled)
        assertFalse(data.available)
        assertNull(data.headline)
    }

    @Test
    fun idea_usesLatestSavedIdeaForActiveHabit() {
        val idea = Idea(
            id = 1,
            habitId = 1,
            title = "Draw a teapot",
            text = "Draw it from memory.",
            category = HabitCategory.CREATIVE,
            createdAtMillis = 100,
            scheduledFor = null,
            status = IdeaStatus.SPARE,
            model = "curated"
        )

        val data = WidgetDataSelector.idea(listOf(habit(1, "draw")), listOf(idea), true)

        assertTrue(data.enabled)
        assertTrue(data.available)
        assertEquals("draw", data.habitName)
        assertEquals("Draw a teapot", data.headline)
        assertEquals("Draw it from memory.", data.body)
    }

    @Test
    fun widgetTheme_respectsAppOverrideAndSystemDefault() {
        assertTrue(WidgetTheme.resolve(ThemeMode.SYSTEM, systemDark = true).isDark)
        assertFalse(WidgetTheme.resolve(ThemeMode.SYSTEM, systemDark = false).isDark)
        assertTrue(WidgetTheme.resolve(ThemeMode.DARK, systemDark = false).isDark)
        assertFalse(WidgetTheme.resolve(ThemeMode.LIGHT, systemDark = true).isDark)
    }
}
