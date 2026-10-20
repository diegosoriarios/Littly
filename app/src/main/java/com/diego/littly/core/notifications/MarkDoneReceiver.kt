package com.diego.littly.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.diego.littly.core.common.di.ApplicationScope
import com.diego.littly.core.domain.model.CompletionSource
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.time.TimeProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MarkDoneReceiver : BroadcastReceiver() {

    @Inject lateinit var habitRepository: HabitRepository
    @Inject lateinit var timeProvider: TimeProvider
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        if (habitId <= 0L) return
        val result = goAsync()
        applicationScope.launch {
            try {
                habitRepository.toggleCompletion(
                    habitId,
                    timeProvider.today(),
                    CompletionSource.NOTIFICATION
                )
                notificationHelper.cancelReminder(habitId)
            } finally {
                result.finish()
            }
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
    }
}
