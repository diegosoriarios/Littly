package com.diego.littly.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface AlarmScheduler {
    fun schedule(habitId: Long, triggerAtMillis: Long)
    fun cancel(habitId: Long)
}

class AndroidAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmScheduler {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(habitId: Long, triggerAtMillis: Long) {
        val pendingIntent = reminderPendingIntent(habitId)
        val canBeExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        if (canBeExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                INEXACT_WINDOW_MILLIS,
                pendingIntent
            )
        }
    }

    override fun cancel(habitId: Long) {
        alarmManager.cancel(reminderPendingIntent(habitId))
    }

    private fun reminderPendingIntent(habitId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(ReminderReceiver.EXTRA_HABIT_ID, habitId)
        return PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private companion object {
        const val INEXACT_WINDOW_MILLIS = 10L * 60 * 1000
    }
}
