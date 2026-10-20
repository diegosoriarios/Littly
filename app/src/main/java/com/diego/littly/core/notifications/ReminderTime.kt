package com.diego.littly.core.notifications

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

object ReminderTime {

    const val MAX_LOOKAHEAD_DAYS = 8L

    fun effectiveReminderDays(habit: com.diego.littly.core.domain.model.Habit): Set<DayOfWeek> {
        val days = habit.reminder.days ?: habit.goalDays
        return days.ifEmpty { DayOfWeek.entries.toSet() }
    }

    fun nextTrigger(
        effectiveDays: Set<DayOfWeek>,
        minuteOfDay: Int,
        after: ZonedDateTime,
        zone: ZoneId
    ): ZonedDateTime {
        val days = effectiveDays.ifEmpty { DayOfWeek.entries.toSet() }
        val hour = minuteOfDay / 60
        val minute = minuteOfDay % 60
        var date = after.toLocalDate()
        repeat(MAX_LOOKAHEAD_DAYS.toInt()) {
            if (date.dayOfWeek in days) {
                val candidate = date.atTime(hour, minute).atZone(zone)
                if (candidate.isAfter(after)) return candidate
            }
            date = date.plusDays(1)
        }
        return after.toLocalDate().plusDays(MAX_LOOKAHEAD_DAYS).atTime(hour, minute).atZone(zone)
    }

    fun nextTriggerFromMillis(
        effectiveDays: Set<DayOfWeek>,
        minuteOfDay: Int,
        nowMillis: Long,
        zone: ZoneId
    ): Long = nextTrigger(
        effectiveDays = effectiveDays,
        minuteOfDay = minuteOfDay,
        after = Instant.ofEpochMilli(nowMillis).atZone(zone),
        zone = zone
    ).toInstant().toEpochMilli()
}
