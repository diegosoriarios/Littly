package com.diego.littly.core.domain.model

import java.time.DayOfWeek

object Weekdays {
    val ALL_DAYS: Set<DayOfWeek> = DayOfWeek.entries.toSet()

    fun toBitmask(days: Set<DayOfWeek>): Int =
        days.fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }

    fun fromBitmask(bitmask: Int): Set<DayOfWeek> =
        DayOfWeek.entries.filter { bitmask and (1 shl (it.value - 1)) != 0 }.toSet()

    fun isGoalDay(date: java.time.LocalDate, goalDays: Set<DayOfWeek>): Boolean =
        goalDays.isEmpty() || date.dayOfWeek in goalDays
}
