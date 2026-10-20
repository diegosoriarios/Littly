package com.diego.littly.core.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

data class Habit(
    val id: Long = 0,
    val name: String,
    val createdAt: LocalDate,
    val archivedAt: LocalDate? = null,
    val goalDays: Set<DayOfWeek>,
    val reminder: Reminder = Reminder(),
    val colorIndex: Int = 0,
    val sortOrder: Int = 0
)

data class Reminder(
    val enabled: Boolean = false,
    val minuteOfDay: Int? = null,
    val days: Set<DayOfWeek>? = null
)
