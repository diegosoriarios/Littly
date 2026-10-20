package com.diego.littly.core.data.mapper

import com.diego.littly.core.database.entity.HabitEntity
import com.diego.littly.core.database.entity.HabitLogEntity
import com.diego.littly.core.database.entity.IdeaEntity
import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.HabitLog
import com.diego.littly.core.domain.model.Idea
import com.diego.littly.core.domain.model.Reminder
import com.diego.littly.core.domain.model.Weekdays
import java.time.LocalDate

fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    name = name,
    createdAt = LocalDate.ofEpochDay(createdAtEpochDay),
    archivedAt = archivedAtEpochDay?.let(LocalDate::ofEpochDay),
    goalDays = Weekdays.fromBitmask(goalDaysBitmask),
    reminder = Reminder(
        enabled = reminderEnabled,
        minuteOfDay = reminderMinuteOfDay,
        days = reminderDaysBitmask?.let(Weekdays::fromBitmask)
    ),
    colorIndex = colorIndex,
    sortOrder = sortOrder
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    name = name,
    createdAtEpochDay = createdAt.toEpochDay(),
    archivedAtEpochDay = archivedAt?.toEpochDay(),
    goalDaysBitmask = Weekdays.toBitmask(goalDays),
    reminderEnabled = reminder.enabled,
    reminderMinuteOfDay = reminder.minuteOfDay,
    reminderDaysBitmask = reminder.days?.let(Weekdays::toBitmask),
    colorIndex = colorIndex,
    sortOrder = sortOrder
)

fun HabitLogEntity.toDomain(): HabitLog = HabitLog(
    habitId = habitId,
    date = LocalDate.ofEpochDay(epochDay),
    completedAtMillis = completedAtMillis,
    source = source
)

fun HabitLog.toEntity(): HabitLogEntity = HabitLogEntity(
    habitId = habitId,
    epochDay = date.toEpochDay(),
    completedAtMillis = completedAtMillis,
    source = source
)

fun IdeaEntity.toDomain(): Idea = Idea(
    id = id,
    habitId = habitId,
    title = title,
    text = text,
    category = category,
    createdAtMillis = createdAtMillis,
    scheduledFor = scheduledForEpochDay?.let(LocalDate::ofEpochDay),
    status = status,
    model = model
)
