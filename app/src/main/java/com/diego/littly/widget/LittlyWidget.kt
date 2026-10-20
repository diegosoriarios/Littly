package com.diego.littly.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.diego.littly.R
import com.diego.littly.core.domain.model.HabitWithProgress
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.time.TimeProvider
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

private val WidgetHabitColors = listOf(
    Color(0xFFD96F4E),
    Color(0xFF8FA968),
    Color(0xFFD9A441),
    Color(0xFF6FA3A0),
    Color(0xFFA98BA3),
    Color(0xFFC97B8E)
)

class LittlyWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            WidgetEntryPoint::class.java
        )
        val habitRepository = entryPoint.habitRepository()
        val timeProvider = entryPoint.timeProvider()

        val habits = habitRepository.observeActiveHabits().first()
        val logs = habitRepository.observeAllLogs().first()
        val today = timeProvider.today()

        val habitsWithProgress = habits.map { habit ->
            val completedDays = logs.filter { it.habitId == habit.id }.map { it.date }.toSet()
            val completedToday = today in completedDays
            val currentStreak = calculateStreak(completedDays, habit.goalDays, today, habit.createdAt)
            HabitWithProgress(habit, completedToday, currentStreak, completedDays.size)
        }

        provideContent {
            GlanceTheme {
                LittlyWidgetContent(habitsWithProgress, today)
            }
        }
    }

    private fun calculateStreak(
        completedDays: Set<LocalDate>,
        goalDays: Set<java.time.DayOfWeek>,
        today: LocalDate,
        createdAt: LocalDate
    ): Int {
        var streak = 0
        var date = today
        while (!date.isBefore(createdAt)) {
            if (date.dayOfWeek in goalDays) {
                if (date in completedDays) {
                    streak++
                } else if (date != today) {
                    break
                }
            }
            date = date.minusDays(1)
        }
        return streak
    }
}

@Composable
private fun LittlyWidgetContent(
    habits: List<HabitWithProgress>,
    today: LocalDate
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(R.color.widget_background))
            .padding(12.dp)
    ) {
        Text(
            text = "Littly",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                color = ColorProvider(R.color.widget_title)
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))

        if (habits.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Add habits to see them here",
                    style = TextStyle(color = ColorProvider(R.color.widget_subtitle))
                )
            }
        } else {
            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                habits.take(5).forEach { habitWithProgress ->
                    HabitRow(habitWithProgress)
                    Spacer(modifier = GlanceModifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun HabitRow(habitWithProgress: HabitWithProgress) {
    val habit = habitWithProgress.habit
    val accentColor = WidgetHabitColors[habit.colorIndex % WidgetHabitColors.size]

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .clickable(
                actionRunCallback<ToggleHabitAction>(
                    actionParametersOf(
                        ToggleHabitAction.KEY_HABIT_ID to habit.id
                    )
                )
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .size(32.dp)
                .background(accentColor)
        ) {
            Text(
                text = habit.name.first().uppercase(),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(R.color.widget_on_accent)
                ),
                modifier = GlanceModifier.padding(8.dp)
            )
        }

        Spacer(modifier = GlanceModifier.width(12.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = habit.name,
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(R.color.widget_text)
                )
            )
            if (habitWithProgress.currentStreak > 0) {
                Text(
                    text = "${habitWithProgress.currentStreak} day streak",
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_subtitle)
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.width(8.dp))

        Box(
            modifier = GlanceModifier
                .size(24.dp)
                .background(
                    if (habitWithProgress.completedToday) {
                        ColorProvider(R.color.widget_accent)
                    } else {
                        ColorProvider(R.color.widget_checkbox_empty)
                    }
                )
        ) {
            if (habitWithProgress.completedToday) {
                Text(
                    text = "✓",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(R.color.widget_on_accent)
                    ),
                    modifier = GlanceModifier.padding(4.dp)
                )
            }
        }
    }
}

class LittlyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LittlyWidget()
}
