package com.diego.littly.core.domain.streak

import com.diego.littly.core.domain.model.Weekdays
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Streak rule:
 * - Only goal days participate in a streak.
 * - A completed goal day extends the streak.
 * - A non-goal day neither extends nor breaks it.
 * - A still-pending today does not break the streak.
 * - Any missed past goal day breaks it.
 * - An empty goal-day set means every day is a goal day.
 */
data class StreakRun(
    val length: Int = 0,
    val start: LocalDate? = null,
    val end: LocalDate? = null
)

object StreakCalculator {

    fun currentStreak(
        completedDays: Set<LocalDate>,
        goalDays: Set<DayOfWeek>,
        today: LocalDate,
        createdAt: LocalDate
    ): Int {
        if (today.isBefore(createdAt)) return 0
        var streak = 0
        var date = today
        while (!date.isBefore(createdAt)) {
            if (Weekdays.isGoalDay(date, goalDays)) {
                when {
                    date in completedDays -> streak++
                    date == today -> Unit
                    else -> break
                }
            }
            date = date.minusDays(1)
        }
        return streak
    }

    fun longestStreak(
        completedDays: Set<LocalDate>,
        goalDays: Set<DayOfWeek>,
        today: LocalDate,
        createdAt: LocalDate
    ): Int = longestStreakRun(completedDays, goalDays, today, createdAt).length

    fun longestStreakRun(
        completedDays: Set<LocalDate>,
        goalDays: Set<DayOfWeek>,
        today: LocalDate,
        createdAt: LocalDate
    ): StreakRun {
        if (today.isBefore(createdAt)) return StreakRun()
        var best = StreakRun()
        var runLength = 0
        var runStart: LocalDate? = null
        var date = createdAt
        while (!date.isAfter(today)) {
            if (Weekdays.isGoalDay(date, goalDays)) {
                if (date in completedDays) {
                    if (runLength == 0) runStart = date
                    runLength++
                    if (runLength > best.length) {
                        best = StreakRun(runLength, runStart, date)
                    }
                } else if (date != today) {
                    runLength = 0
                    runStart = null
                }
            }
            date = date.plusDays(1)
        }
        return best
    }
}
