package com.diego.littly.core.domain.stats

import com.diego.littly.core.domain.model.CompletionSource
import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.HabitLog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsCalculatorTest {

    private val today = LocalDate.of(2026, 10, 21)

    private fun habit(
        id: Long = 1,
        name: String = "draw",
        goalDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
        createdAt: LocalDate = LocalDate.of(2026, 10, 1),
        archivedAt: LocalDate? = null
    ) = Habit(
        id = id,
        name = name,
        createdAt = createdAt,
        archivedAt = archivedAt,
        goalDays = goalDays
    )

    private fun logsFor(habitId: Long, vararg dates: LocalDate): List<HabitLog> =
        dates.map { HabitLog(habitId, it, 0L, CompletionSource.APP) }

    @Test
    fun todayOverview_usesRealDueAndCompletionData() {
        val daily = habit()
        val mondayWednesday = habit(
            id = 2,
            name = "read",
            goalDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        )
        val logs = logsFor(1, today.minusDays(2), today.minusDays(1), today) +
            logsFor(2, LocalDate.of(2026, 10, 19), today)

        val overview = StatsCalculator.todayOverview(listOf(daily, mondayWednesday), logs, today)

        assertEquals(2, overview.dueToday)
        assertEquals(2, overview.completedToday)
        assertEquals(7, overview.week.size)
        assertEquals(9, overview.weeklyDue)
        assertEquals(5, overview.weeklyCompleted)
        assertEquals(3, overview.bestCurrent?.currentStreak)
        assertEquals("draw", overview.bestCurrent?.habit?.name)
    }

    @Test
    fun weekDays_areOrderedOldestToToday() {
        val days = StatsCalculator.weekDays(
            habits = listOf(habit()),
            logsByHabit = emptyMap(),
            today = today
        )

        assertEquals(today.minusDays(6), days.first().date)
        assertEquals(today, days.last().date)
    }

    @Test
    fun monthSummary_excludesFutureDaysFromRate() {
        val daily = habit()
        val logs = logsFor(1, today.minusDays(2), today.minusDays(1), today)

        val summary = StatsCalculator.monthSummary(
            habits = listOf(daily),
            logsByHabit = mapOf(1L to logs),
            month = YearMonth.of(2026, 10),
            today = today
        )

        assertEquals(31, summary.days.size)
        assertEquals(21, summary.due)
        assertEquals(3, summary.completed)
        assertEquals(3f / 21f, summary.completionRate!!, 0.0001f)
        assertTrue(summary.days.drop(21).all { it.isFuture })
    }

    @Test
    fun monthSummary_emptyHabitListHasNoRate() {
        val summary = StatsCalculator.monthSummary(
            habits = emptyList(),
            logsByHabit = emptyMap(),
            month = YearMonth.of(2026, 10),
            today = today
        )

        assertEquals(0, summary.due)
        assertNull(summary.completionRate)
    }

    @Test
    fun monthSummary_respectsCreationAndArchiveDates() {
        val paused = habit(
            id = 2,
            createdAt = LocalDate.of(2026, 10, 5),
            archivedAt = LocalDate.of(2026, 10, 10)
        )
        val summary = StatsCalculator.monthSummary(
            habits = listOf(paused),
            logsByHabit = emptyMap(),
            month = YearMonth.of(2026, 10),
            today = today
        )

        assertEquals(6, summary.due)
    }

    @Test
    fun trend_returnsRequestedNumberOfMonthsEndingAtSelection() {
        val trend = StatsCalculator.trend(
            habits = listOf(habit()),
            logsByHabit = emptyMap(),
            endMonth = YearMonth.of(2026, 10),
            today = today,
            monthCount = 6
        )

        assertEquals(6, trend.size)
        assertEquals(YearMonth.of(2026, 5), trend.first().month)
        assertEquals(YearMonth.of(2026, 10), trend.last().month)
        assertNull(trend.first().completionRate)
        assertEquals(0f, trend.last().completionRate!!, 0.0001f)
    }

    @Test
    fun thirtyDayRate_usesGoalDaysOnly() {
        val weekdaysOnly = habit(goalDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        val logs = logsFor(1, LocalDate.of(2026, 10, 19), today)

        val rate = StatsCalculator.thirtyDayRate(weekdaysOnly, logs, today)!!

        assertEquals(6, rate.due)
        assertEquals(2, rate.completed)
        assertEquals(2f / 6f, rate.completionRate!!, 0.0001f)
    }

    @Test
    fun bestStreak_returnsHabitAndDateRange() {
        val logs = logsFor(
            1,
            LocalDate.of(2026, 10, 1),
            LocalDate.of(2026, 10, 2),
            LocalDate.of(2026, 10, 3),
            LocalDate.of(2026, 10, 8),
            LocalDate.of(2026, 10, 9),
            LocalDate.of(2026, 10, 10),
            LocalDate.of(2026, 10, 11)
        )

        val best = StatsCalculator.bestStreak(
            habits = listOf(habit()),
            logsByHabit = mapOf(1L to logs),
            today = today
        )!!

        assertEquals(4, best.run.length)
        assertEquals(LocalDate.of(2026, 10, 8), best.run.start)
        assertEquals(LocalDate.of(2026, 10, 11), best.run.end)
        assertEquals("draw", best.habit.name)
    }

    @Test
    fun totalCheckIns_usesEarliestStoredCompletion() {
        val logs = logsFor(1, LocalDate.of(2026, 9, 2), today)

        val total = StatsCalculator.totalCheckIns(logs)

        assertEquals(2, total.count)
        assertEquals(LocalDate.of(2026, 9, 2), total.since)
    }

    @Test
    fun dayCompletion_isBinaryPerHabit() {
        val day = DayCompletion(date = today, completed = 2, due = 4)

        assertEquals(0.5f, day.ratio, 0.0001f)
        assertTrue(!day.allDone)
    }
}
