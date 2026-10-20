package com.diego.littly.core.notifications

import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.repository.SettingsRepository
import com.diego.littly.core.domain.time.TimeProvider
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.first

interface ReminderPlanner {
    suspend fun rescheduleAll()
    suspend fun rescheduleHabit(habitId: Long)
    suspend fun cancelHabit(habitId: Long)
}

class ReminderPlannerImpl @Inject constructor(
    private val habitRepository: HabitRepository,
    private val settingsRepository: SettingsRepository,
    private val alarmScheduler: AlarmScheduler,
    private val timeProvider: TimeProvider
) : ReminderPlanner {

    override suspend fun rescheduleAll() {
        val habits = habitRepository.getActiveHabits()
        if (!settingsRepository.remindersMasterEnabled.first()) {
            habits.forEach { alarmScheduler.cancel(it.id) }
            return
        }
        habits.forEach { scheduleOrCancel(it) }
    }

    override suspend fun rescheduleHabit(habitId: Long) {
        val habit = habitRepository.getHabit(habitId)
        if (habit == null) {
            alarmScheduler.cancel(habitId)
            return
        }
        if (!settingsRepository.remindersMasterEnabled.first()) {
            alarmScheduler.cancel(habitId)
            return
        }
        scheduleOrCancel(habit)
    }

    override suspend fun cancelHabit(habitId: Long) {
        alarmScheduler.cancel(habitId)
    }

    private suspend fun scheduleOrCancel(habit: Habit) {
        val minuteOfDay = habit.reminder.minuteOfDay
        if (!habit.reminder.enabled || minuteOfDay == null) {
            alarmScheduler.cancel(habit.id)
            return
        }
        val zone = ZoneId.systemDefault()
        val triggerAtMillis = ReminderTime.nextTriggerFromMillis(
            effectiveDays = ReminderTime.effectiveReminderDays(habit),
            minuteOfDay = minuteOfDay,
            nowMillis = timeProvider.nowMillis(),
            zone = zone
        )
        alarmScheduler.schedule(habit.id, triggerAtMillis)
    }
}
