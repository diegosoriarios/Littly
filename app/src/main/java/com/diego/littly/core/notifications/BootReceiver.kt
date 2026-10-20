package com.diego.littly.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.diego.littly.core.common.di.ApplicationScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var reminderPlanner: ReminderPlanner
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }
        val result = goAsync()
        applicationScope.launch {
            try {
                reminderPlanner.rescheduleAll()
            } finally {
                result.finish()
            }
        }
    }
}
