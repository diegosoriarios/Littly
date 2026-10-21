package com.diego.littly.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.diego.littly.core.domain.model.ThemeMode
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

private val overviewSizes = setOf(
    DpSize(180.dp, 110.dp),
    DpSize(250.dp, 110.dp),
    DpSize(250.dp, 180.dp),
    DpSize(250.dp, 260.dp),
    DpSize(320.dp, 180.dp),
    DpSize(320.dp, 260.dp)
)

class LittlyWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(overviewSizes)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val habits = entryPoint.habitRepository().observeActiveHabits().first()
        val logs = entryPoint.habitRepository().observeAllLogs().first()
        val today = entryPoint.timeProvider().today()
        val data = WidgetDataSelector.overview(habits, logs, today, heightDp = 0f)
        val palette = resolvePalette(context, entryPoint.settingsRepository().themeMode.first())
        provideContent {
            OverviewWidgetContent(data, palette, context)
        }
    }
}

class StreakWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(DpSize(110.dp, 110.dp), DpSize(150.dp, 150.dp), DpSize(220.dp, 110.dp))
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val habits = entryPoint.habitRepository().observeActiveHabits().first()
        val logs = entryPoint.habitRepository().observeAllLogs().first()
        val today = entryPoint.timeProvider().today()
        val data = WidgetDataSelector.streak(habits, logs, today)
        val palette = resolvePalette(context, entryPoint.settingsRepository().themeMode.first())
        provideContent {
            StreakWidgetContent(data, palette, context)
        }
    }
}

class IdeaWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(DpSize(110.dp, 110.dp), DpSize(150.dp, 150.dp), DpSize(220.dp, 150.dp))
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val habits = entryPoint.habitRepository().observeActiveHabits().first()
        val ideas = entryPoint.ideaRepository().observeLatest(12).first()
        val enabled = entryPoint.settingsRepository().ideaEngineEnabled.first()
        val data = WidgetDataSelector.idea(habits, ideas, enabled)
        val palette = resolvePalette(context, entryPoint.settingsRepository().themeMode.first())
        provideContent {
            IdeaWidgetContent(data, palette, context)
        }
    }
}

private fun resolvePalette(context: Context, themeMode: ThemeMode): WidgetPalette {
    val systemDark = (context.resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    return WidgetTheme.resolve(themeMode, systemDark)
}

class LittlyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LittlyWidget()
}

class StreakWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreakWidget()
}

class IdeaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = IdeaWidget()
}
