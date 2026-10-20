package com.diego.littly.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class WidgetUpdateHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    open suspend fun updateAllWidgets() {
        val manager = GlanceAppWidgetManager(context)
        val widget = LittlyWidget()
        val glanceIds = manager.getGlanceIds(LittlyWidget::class.java)
        glanceIds.forEach { glanceId ->
            widget.update(context, glanceId)
        }
    }
}
