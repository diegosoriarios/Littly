package com.diego.littly.core.domain.stats

import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.HabitLog
import com.diego.littly.core.domain.model.HabitWithProgress
import com.diego.littly.core.domain.model.Weekdays
import com.diego.littly.core.domain.streak.StreakCalculator
import com.diego.littly.core.domain.streak.StreakRun
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

data class DayCompletion(
    val date: LocalDate,
    val completed: Int,
    val due: Int,
    val isFuture: Boolean = false
) {
    val ratio: Float
        get() = if (due <= 0) 0f else completed.toFloat() / due.toFloat()

    val hasOpportunity: Boolean
        get() = due > 0

    val allDone: Boolean
        get() = due > 0 && completed >= due
}

data class TodayOverview(
    val dueToday: Int,
    val completedToday: Int,
    val week: List<DayCompletion>,
    val bestCurrent: HabitWithProgress?,
    val weeklyCompleted: Int,
    val weeklyDue: Int
)

data class MonthSummary(
    val month: YearMonth,
    val days: List<DayCompletion>,
    val completed: Int,
    val due: Int,
    val completionRate: Float?
)

data class MonthTrendPoint(
    val month: YearMonth,
    val completed: Int,
    val due: Int,
    val completionRate: Float?
)

data class MonthRate(
    val month: YearMonth,
    val completionRate: Float?,
    val completed: Int,
    val due: Int
)

data class BestStreak(
    val habit: Habit,
    val run: StreakRun
)

data class TotalCheckIns(
    val count: Int,
    val since: LocalDate?
)

object StatsCalculator {

    fun todayOverview(
        habits: List<Habit>,
        logs: List<HabitLog>,
        today: LocalDate
    ): TodayOverview {
        val logsByHabit = logs.groupBy { it.habitId }
        val week = weekDays(habits, logsByHabit, today, days = 7)
        val todayData = dayCompletion(habits, logsByHabit, today)
        val progress = habits.map { habit ->
            val completedDays = logsByHabit[habit.id].orEmpty().mapTo(mutableSetOf()) { it.date }
            HabitWithProgress(
                habit = habit,
                completedToday = today in completedDays,
                currentStreak = StreakCalculator.currentStreak(
                    completedDays = completedDays,
                    goalDays = habit.goalDays,
                    today = today,
                    createdAt = habit.createdAt
                ),
                totalCompletions = completedDays.size
            )
        }
        val best = progress
            .filter { it.currentStreak > 0 }
            .maxWithOrNull(compareBy<HabitWithProgress> { it.currentStreak }.thenByDescending { it.habit.sortOrder })
        return TodayOverview(
            dueToday = todayData.due,
            completedToday = todayData.completed,
            week = week,
            bestCurrent = best,
            weeklyCompleted = week.sumOf { it.completed },
            weeklyDue = week.sumOf { it.due }
        )
    }

    fun weekDays(
        habits: List<Habit>,
        logsByHabit: Map<Long, List<HabitLog>>,
        today: LocalDate,
        days: Int = 7
    ): List<DayCompletion> =
        (days - 1 downTo 0).map { offset ->
            dayCompletion(habits, logsByHabit, today.minusDays(offset.toLong()))
        }

    fun dayCompletion(
        habits: List<Habit>,
        logsByHabit: Map<Long, List<HabitLog>>,
        date: LocalDate,
        today: LocalDate = date
    ): DayCompletion {
        val dueHabits = habits.filter { habit ->
            isActiveOn(habit, date) && Weekdays.isGoalDay(date, habit.goalDays)
        }
        val completed = dueHabits.count { habit ->
            logsByHabit[habit.id].orEmpty().any { it.date == date }
        }
        return DayCompletion(
            date = date,
            completed = completed,
            due = dueHabits.size,
            isFuture = date.isAfter(today)
        )
    }

    fun monthSummary(
        habits: List<Habit>,
        logsByHabit: Map<Long, List<HabitLog>>,
        month: YearMonth,
        today: LocalDate
    ): MonthSummary {
        val days = monthDays(habits, logsByHabit, month, today)
        val elapsed = days.filterNot { it.isFuture }
        return MonthSummary(
            month = month,
            days = days,
            completed = elapsed.sumOf { it.completed },
            due = elapsed.sumOf { it.due },
            completionRate = rateOf(
                completed = elapsed.sumOf { it.completed },
                due = elapsed.sumOf { it.due }
            )
        )
    }

    fun monthDays(
        habits: List<Habit>,
        logsByHabit: Map<Long, List<HabitLog>>,
        month: YearMonth,
        today: LocalDate
    ): List<DayCompletion> =
        (1..month.lengthOfMonth()).map { day ->
            dayCompletion(habits, logsByHabit, month.atDay(day), today)
        }

    fun monthRate(
        habits: List<Habit>,
        logsByHabit: Map<Long, List<HabitLog>>,
        month: YearMonth,
        today: LocalDate
    ): MonthRate {
        val summary = monthSummary(habits, logsByHabit, month, today)
        return MonthRate(summary.month, summary.completionRate, summary.completed, summary.due)
    }

    fun trend(
        habits: List<Habit>,
        logsByHabit: Map<Long, List<HabitLog>>,
        endMonth: YearMonth,
        today: LocalDate,
        monthCount: Int = 6
    ): List<MonthTrendPoint> =
        (monthCount - 1 downTo 0).map { offset ->
            val month = endMonth.minusMonths(offset.toLong())
            val summary = monthSummary(habits, logsByHabit, month, today)
            MonthTrendPoint(
                month = month,
                completed = summary.completed,
                due = summary.due,
                completionRate = summary.completionRate
            )
        }

    fun thirtyDayRate(
        habit: Habit,
        logs: List<HabitLog>,
        today: LocalDate,
        days: Int = 30
    ): MonthRate? {
        if (today.isBefore(habit.createdAt)) return null
        val start = today.minusDays((days - 1).toLong())
        val completedDays = logs.mapTo(mutableSetOf()) { it.date }
        var due = 0
        var done = 0
        var date = maxOf(start, habit.createdAt)
        while (!date.isAfter(today)) {
            if (isActiveOn(habit, date) && Weekdays.isGoalDay(date, habit.goalDays)) {
                due++
                if (date in completedDays) done++
            }
            date = date.plusDays(1)
        }
        return MonthRate(YearMonth.from(today), rateOf(done, due), done, due)
    }

    fun rangeRate(
        habit: Habit,
        completedDays: Set<LocalDate>,
        start: LocalDate,
        end: LocalDate
    ): MonthRate {
        var due = 0
        var done = 0
        var date = maxOf(start, habit.createdAt)
        val last = end
        while (!date.isAfter(last)) {
            if (isActiveOn(habit, date) && Weekdays.isGoalDay(date, habit.goalDays)) {
                due++
                if (date in completedDays) done++
            }
            date = date.plusDays(1)
        }
        return MonthRate(YearMonth.from(start), rateOf(done, due), done, due)
    }

    fun bestStreak(
        habits: List<Habit>,
        logsByHabit: Map<Long, List<HabitLog>>,
        today: LocalDate
    ): BestStreak? =
        habits.mapNotNull { habit ->
            val completedDays = logsByHabit[habit.id].orEmpty().mapTo(mutableSetOf()) { it.date }
            val run = StreakCalculator.longestStreakRun(
                completedDays = completedDays,
                goalDays = habit.goalDays,
                today = today,
                createdAt = habit.createdAt
            )
            if (run.length > 0) BestStreak(habit, run) else null
        }.maxByOrNull { it.run.length }

    fun totalCheckIns(logs: List<HabitLog>): TotalCheckIns =
        TotalCheckIns(
            count = logs.size,
            since = logs.minOfOrNull { it.date }
        )

    fun completionsInMonth(logs: List<HabitLog>, month: YearMonth): Int =
        logs.count { YearMonth.from(it.date) == month }

    fun daysBetween(start: LocalDate, end: LocalDate): Long =
        ChronoUnit.DAYS.between(start, end) + 1

    fun isActiveOn(habit: Habit, date: LocalDate): Boolean {
        if (date.isBefore(habit.createdAt)) return false
        val archived = habit.archivedAt
        return archived == null || !date.isAfter(archived)
    }

    fun rateOf(completed: Int, due: Int): Float? =
        if (due <= 0) null else completed.toFloat() / due.toFloat()
}
