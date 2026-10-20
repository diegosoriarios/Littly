package com.diego.littly.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.diego.littly.core.domain.model.CompletionSource
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.time.TimeProvider
import dagger.hilt.EntryPoints
import dagger.hilt.android.EntryPointAccessors

class ToggleHabitAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habitId = parameters[KEY_HABIT_ID] ?: return

        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            WidgetEntryPoint::class.java
        )
        val habitRepository = entryPoint.habitRepository()
        val timeProvider = entryPoint.timeProvider()

        habitRepository.toggleCompletion(
            habitId = habitId,
            date = timeProvider.today(),
            source = CompletionSource.WIDGET
        )

        // Update the widget to reflect the change
        LittlyWidget().update(context, glanceId)
    }

    companion object {
        val KEY_HABIT_ID = ActionParameters.Key<Long>("habit_id")
    }
}
