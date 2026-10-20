package com.diego.littly.core.domain.streak

import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class StreakCalculatorTest {

    private val monday = LocalDate.of(2026, 10, 19)
    private val epoch = LocalDate.of(2026, 9, 1)

    private fun day(offset: Int): LocalDate = monday.plusDays(offset.toLong())

    private fun all(days: List<LocalDate>): Set<LocalDate> = days.toSet()

    @Test
    fun `no completions returns zero`() {
        val streak = StreakCalculator.currentStreak(
            completedDays = emptySet(),
            goalDays = WeekdaySets.daily,
            today = monday,
            createdAt = epoch
        )
        assertEquals(0, streak)
    }

    @Test
    fun `consecutive daily completions count today`() {
        val streak = StreakCalculator.currentStreak(
            completedDays = all(listOf(day(-1), day(0))),
            goalDays = WeekdaySets.daily,
            today = monday,
            createdAt = epoch
        )
        assertEquals(2, streak)
    }

    @Test
    fun `today pending does not break streak`() {
        val streak = StreakCalculator.currentStreak(
            completedDays = all(listOf(day(-3), day(-2), day(-1))),
            goalDays = WeekdaySets.daily,
            today = monday,
            createdAt = epoch
        )
        assertEquals(3, streak)
    }

    @Test
    fun `missed goal day breaks streak`() {
        val streak = StreakCalculator.currentStreak(
            completedDays = all(listOf(day(-2), day(0))),
            goalDays = WeekdaySets.daily,
            today = monday,
            createdAt = epoch
        )
        assertEquals(1, streak)
    }

    @Test
    fun `non goal days are skipped both directions`() {
        val sundays = setOf(DayOfWeek.SUNDAY)
        val streak = StreakCalculator.currentStreak(
            completedDays = all(listOf(day(-1), day(-8), day(0))),
            goalDays = sundays,
            today = monday,
            createdAt = LocalDate.of(2026, 9, 1)
        )
        assertEquals(2, streak)
    }

    @Test
    fun `missing non goal day does not break streak`() {
        val monWed = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        val streak = StreakCalculator.currentStreak(
            completedDays = all(listOf(day(-7), day(-5), day(0))),
            goalDays = monWed,
            today = monday,
            createdAt = epoch
        )
        assertEquals(3, streak)
    }

    @Test
    fun `streak stops at creation date`() {
        val createdAt = day(-2)
        val streak = StreakCalculator.currentStreak(
            completedDays = all(listOf(day(-2), day(-1), day(0))),
            goalDays = WeekdaySets.daily,
            today = monday,
            createdAt = createdAt
        )
        assertEquals(3, streak)
    }

    @Test
    fun `today before creation returns zero`() {
        val streak = StreakCalculator.currentStreak(
            completedDays = emptySet(),
            goalDays = WeekdaySets.daily,
            today = monday,
            createdAt = monday.plusDays(1)
        )
        assertEquals(0, streak)
    }

    @Test
    fun `empty goal days means every day counts`() {
        val streak = StreakCalculator.currentStreak(
            completedDays = all(listOf(day(-1), day(0))),
            goalDays = emptySet(),
            today = monday,
            createdAt = epoch
        )
        assertEquals(2, streak)
    }

    @Test
    fun `longest streak ignores later gaps`() {
        val completed = all(
            listOf(day(-18), day(-17), day(-16), day(-15), day(-13), day(-12), day(-11), day(-10), day(-9))
        )
        val longest = StreakCalculator.longestStreak(
            completedDays = completed,
            goalDays = WeekdaySets.daily,
            today = monday,
            createdAt = epoch
        )
        assertEquals(5, longest)
    }

    @Test
    fun `longest streak does not reset on pending today`() {
        val longest = StreakCalculator.longestStreak(
            completedDays = all(listOf(day(-2), day(-1))),
            goalDays = WeekdaySets.daily,
            today = monday,
            createdAt = epoch
        )
        assertEquals(2, longest)
    }

    @Test
    fun `today not a goal day counts last completed goal days`() {
        val tuesdays = setOf(DayOfWeek.TUESDAY)
        val streak = StreakCalculator.currentStreak(
            completedDays = all(listOf(day(-6))),
            goalDays = tuesdays,
            today = monday,
            createdAt = epoch
        )
        assertEquals(1, streak)
    }

    private object WeekdaySets {
        val daily: Set<DayOfWeek> = DayOfWeek.entries.toSet()
    }
}
