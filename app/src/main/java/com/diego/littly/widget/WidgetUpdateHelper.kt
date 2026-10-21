package com.diego.littly.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class WidgetUpdateHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    open suspend fun updateAllWidgets() {
        update(LittlyWidget())
        update(StreakWidget())
        update(IdeaWidget())
    }

    private suspend fun update(widget: GlanceAppWidget) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(widget.javaClass)
        glanceIds.forEach { glanceId ->
            widget.update(context, glanceId)
        }
    }
}
