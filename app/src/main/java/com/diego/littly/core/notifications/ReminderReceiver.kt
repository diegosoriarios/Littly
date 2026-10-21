package com.diego.littly.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.diego.littly.R
import com.diego.littly.core.common.di.ApplicationScope
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.repository.IdeaRepository
import com.diego.littly.core.domain.repository.SettingsRepository
import com.diego.littly.core.domain.time.TimeProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var habitRepository: HabitRepository
    @Inject lateinit var ideaRepository: IdeaRepository
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var reminderPlanner: ReminderPlanner
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var timeProvider: TimeProvider
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        if (habitId <= 0L) return
        val result = goAsync()
        applicationScope.launch {
            try {
                val habit = habitRepository.getHabit(habitId) ?: return@launch
                if (habit.archivedAt != null || !habit.reminder.enabled) return@launch
                if (!settingsRepository.remindersMasterEnabled.first()) return@launch

                val today = timeProvider.today()
                val ideaEnabled = settingsRepository.ideaEngineEnabled.first()
                val queuedIdea = if (ideaEnabled) {
                    ideaRepository.getQueuedIdeaForDate(habitId, today)
                } else {
                    null
                }
                val body = queuedIdea?.text ?: context.getString(R.string.reminder_default_body, habit.name)
                queuedIdea?.let { ideaRepository.markShown(it.id) }

                notificationHelper.showReminder(
                    habitId = habitId,
                    title = habit.name,
                    body = body
                )
                reminderPlanner.rescheduleHabit(habitId)
            } finally {
                result.finish()
            }
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
    }
}
