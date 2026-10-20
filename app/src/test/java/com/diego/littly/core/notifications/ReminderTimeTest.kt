package com.diego.littly.core.notifications

import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderTimeTest {

    private val zone: ZoneId = ZoneId.of("Europe/Lisbon")

    private fun zdt(date: String, hour: Int, minute: Int): ZonedDateTime =
        ZonedDateTime.of(java.time.LocalDate.parse(date), java.time.LocalTime.of(hour, minute), zone)

    @Test
    fun nextTriggerIsLaterTodayWhenTimeNotPassed() {
        val now = zdt("2026-10-21", 10, 0)
        val trigger = ReminderTime.nextTrigger(
            effectiveDays = DayOfWeek.entries.toSet(),
            minuteOfDay = 14 * 60,
            after = now,
            zone = zone
        )
        assertEquals(zdt("2026-10-21", 14, 0), trigger)
    }

    @Test
    fun skipsToNextGoalDayWhenTodayAlreadyPassed() {
        val now = zdt("2026-10-21", 10, 0)
        val trigger = ReminderTime.nextTrigger(
            effectiveDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            minuteOfDay = 9 * 60,
            after = now,
            zone = zone
        )
        assertEquals(zdt("2026-10-23", 9, 0), trigger)
    }

    @Test
    fun wrapsAroundTheWeek() {
        val now = zdt("2026-10-24", 12, 0)
        val trigger = ReminderTime.nextTrigger(
            effectiveDays = setOf(DayOfWeek.MONDAY),
            minuteOfDay = 9 * 60,
            after = now,
            zone = zone
        )
        assertEquals(zdt("2026-10-26", 9, 0), trigger)
    }

    @Test
    fun firesOnExactTimeMovesToNextWeek() {
        val now = zdt("2026-10-21", 9, 0)
        val trigger = ReminderTime.nextTrigger(
            effectiveDays = setOf(DayOfWeek.WEDNESDAY),
            minuteOfDay = 9 * 60,
            after = now,
            zone = zone
        )
        assertEquals(zdt("2026-10-28", 9, 0), trigger)
    }

    @Test
    fun emptyDaysMeansDaily() {
        val now = zdt("2026-10-23", 23, 0)
        val trigger = ReminderTime.nextTrigger(
            effectiveDays = emptySet(),
            minuteOfDay = 9 * 60,
            after = now,
            zone = zone
        )
        assertEquals(zdt("2026-10-24", 9, 0), trigger)
    }

    @Test
    fun handlesNonRoundMinutes() {
        val now = zdt("2026-10-21", 0, 0)
        val trigger = ReminderTime.nextTrigger(
            effectiveDays = DayOfWeek.entries.toSet(),
            minuteOfDay = 13 * 60 + 45,
            after = now,
            zone = zone
        )
        assertEquals(zdt("2026-10-21", 13, 45), trigger)
    }

    @Test
    fun nextTriggerFromMillisReturnsEpochMillis() {
        val now = zdt("2026-10-21", 10, 0)
        val nowMillis = now.toInstant().toEpochMilli()
        val triggerMillis = ReminderTime.nextTriggerFromMillis(
            effectiveDays = setOf(DayOfWeek.FRIDAY),
            minuteOfDay = 9 * 60,
            nowMillis = nowMillis,
            zone = zone
        )
        assertEquals(zdt("2026-10-23", 9, 0).toInstant().toEpochMilli(), triggerMillis)
    }
}
