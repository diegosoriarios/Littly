package com.diego.littly.core.domain.streak

import com.diego.littly.core.domain.model.Weekdays
import java.time.DayOfWeek
import java.time.LocalDate

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
    ): Int {
        if (today.isBefore(createdAt)) return 0
        var best = 0
        var run = 0
        var date = createdAt
        while (!date.isAfter(today)) {
            if (Weekdays.isGoalDay(date, goalDays)) {
                if (date in completedDays) {
                    run++
                    if (run > best) best = run
                } else if (date != today) {
                    run = 0
                }
            }
            date = date.plusDays(1)
        }
        return best
    }
}
