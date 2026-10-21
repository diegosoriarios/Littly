package com.diego.littly.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity as actionStartActivityClass
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity as actionStartActivityIntent
import androidx.glance.appwidget.cornerRadius
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
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.diego.littly.MainActivity
import com.diego.littly.core.llm.ModelLocator

@Composable
fun OverviewWidgetContent(data: OverviewWidgetData, palette: WidgetPalette, context: Context) {
    val size = LocalSize.current
    val limit = WidgetDataSelector.itemLimit(size.height.value)
    val items = data.items.take(limit)
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(palette.background))
            .cornerRadius(24.dp)
            .padding(12.dp)
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 7.dp)
                .clickable(actionStartActivityClass<MainActivity>())
                .semantics {
                    contentDescription = context.getString(com.diego.littly.R.string.widget_cd_open_today)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(34.dp)
                    .background(ColorProvider(palette.success))
                    .cornerRadius(11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "L",
                    style = TextStyle(
                        color = ColorProvider(palette.onSuccess),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(GlanceModifier.width(8.dp))
            Column(GlanceModifier.defaultWeight()) {
                Text(
                    text = "Littly",
                    style = TextStyle(
                        color = ColorProvider(palette.text),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = if (data.due == 0) {
                        "${data.dateLabel} · ${context.getString(com.diego.littly.R.string.widget_rest_day)}"
                    } else {
                        "${data.dateLabel} · ${context.getString(com.diego.littly.R.string.widget_done_today, data.completed, data.due)}"
                    },
                    style = TextStyle(
                        color = ColorProvider(palette.mutedText),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }
            if (data.bestStreak > 0) {
                Row(
                    modifier = GlanceModifier
                        .background(ColorProvider(palette.accent))
                        .cornerRadius(12.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔥",
                        style = TextStyle(fontSize = 11.sp),
                        modifier = GlanceModifier.padding(end = 2.dp)
                    )
                    Text(
                        text = data.bestStreak.toString(),
                        style = TextStyle(
                            color = ColorProvider(palette.onAccent),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
        Spacer(GlanceModifier.height(8.dp))
        if (items.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = context.getString(
                        if (data.hasActiveHabits) {
                            com.diego.littly.R.string.widget_rest_day
                        } else {
                            com.diego.littly.R.string.widget_empty
                        }
                    ),
                    style = TextStyle(
                        color = ColorProvider(palette.mutedText),
                        fontSize = 12.sp
                    )
                )
            }
        } else {
            Column(GlanceModifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    WidgetHabitRow(item, palette, context)
                    if (index < items.lastIndex) Spacer(GlanceModifier.height(5.dp))
                }
            }
        }
    }
}

@Composable
private fun WidgetHabitRow(item: WidgetHabitItem, palette: WidgetPalette, context: Context) {
    val accent = WidgetTheme.accentFor(item.colorIndex)
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ColorProvider(palette.card))
            .cornerRadius(14.dp)
            .padding(horizontal = 8.dp, vertical = 11.dp)
            .clickable(
                actionRunCallback<ToggleHabitAction>(
                    actionParametersOf(ToggleHabitAction.KEY_HABIT_ID to item.habitId)
                )
            )
            .semantics {
                contentDescription = context.getString(
                    com.diego.littly.R.string.widget_cd_toggle,
                    item.name
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .size(26.dp)
                .background(
                    ColorProvider(if (item.completed) palette.success else palette.cardMuted)
                )
                .cornerRadius(13.dp),
            contentAlignment = Alignment.Center
        ) {
            if (item.completed) {
                Text(
                    text = "✓",
                    style = TextStyle(
                        color = ColorProvider(palette.onSuccess),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        Spacer(GlanceModifier.width(7.dp))
        Box(
            modifier = GlanceModifier
                .size(7.dp)
                .background(ColorProvider(accent))
                .cornerRadius(4.dp)
        ) {}
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = item.name,
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = ColorProvider(if (item.completed) palette.mutedText else palette.text),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (item.completed) TextDecoration.LineThrough else TextDecoration.None
            ),
            maxLines = 1
        )
        if (item.currentStreak > 0) {
            Spacer(GlanceModifier.width(5.dp))
            Text(
                text = item.currentStreak.toString(),
                style = TextStyle(
                    color = ColorProvider(palette.accent),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun StreakWidgetContent(data: StreakWidgetData?, palette: WidgetPalette, context: Context) {
    val compact = LocalSize.current.height.value < 140f
    val action = if (data != null) {
        openHabitAction(context, data.habitId)
    } else {
        actionStartActivityClass<MainActivity>()
    }
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(palette.navy))
            .cornerRadius(24.dp)
            .padding(14.dp)
            .clickable(action)
            .semantics {
                contentDescription = context.getString(com.diego.littly.R.string.widget_streak_description)
            }
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(if (compact) 28.dp else 32.dp)
                    .background(ColorProvider(palette.success))
                    .cornerRadius(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "L",
                    style = TextStyle(
                        color = ColorProvider(palette.onSuccess),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(GlanceModifier.defaultWeight())
            Text(
                text = "🔥",
                style = TextStyle(fontSize = 16.sp)
            )
        }
        Spacer(GlanceModifier.defaultWeight())
        if (data != null) {
            Text(
                text = data.streak.toString(),
                style = TextStyle(
                    color = ColorProvider(palette.onNavy),
                    fontSize = if (compact) 36.sp else 44.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Text(
                text = context.getString(com.diego.littly.R.string.widget_day_streak),
                style = TextStyle(
                    color = ColorProvider(palette.onNavyMuted),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            if (!compact) {
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = data.habitName,
                    style = TextStyle(
                        color = ColorProvider(palette.onNavy),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 2
                )
            }
        } else {
            Text(
                text = context.getString(com.diego.littly.R.string.widget_streak_empty),
                style = TextStyle(
                    color = ColorProvider(palette.onNavy),
                    fontSize = if (compact) 17.sp else 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = context.getString(com.diego.littly.R.string.widget_open_littly),
                style = TextStyle(
                    color = ColorProvider(palette.onNavyMuted),
                    fontSize = 11.sp
                ),
                maxLines = 2
            )
        }
    }
}

@Composable
fun IdeaWidgetContent(data: IdeaWidgetData, palette: WidgetPalette, context: Context) {
    val compact = LocalSize.current.height.value < 140f
    val action = if (data.available && data.habitId != null) {
        openHabitAction(context, data.habitId)
    } else {
        actionStartActivityClass<MainActivity>()
    }
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(palette.ideaBackground))
            .cornerRadius(24.dp)
            .padding(14.dp)
            .clickable(action)
            .semantics {
                contentDescription = context.getString(com.diego.littly.R.string.widget_idea_description)
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = GlanceModifier
                    .size(if (compact) 28.dp else 32.dp)
                    .background(ColorProvider(palette.ideaAccent))
                    .cornerRadius(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💡",
                    style = TextStyle(fontSize = 15.sp)
                )
            }
            Spacer(GlanceModifier.width(8.dp))
            Text(
                text = if (data.available && data.habitName != null) {
                    context.getString(com.diego.littly.R.string.widget_idea_label, data.habitName)
                        .uppercase()
                } else {
                    context.getString(com.diego.littly.R.string.widget_idea_name).uppercase()
                },
                style = TextStyle(
                    color = ColorProvider(palette.ideaAccent),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight()
            )
            if (data.available && data.model != null) {
                Text(
                    text = if (data.model == ModelLocator.MODEL_ID) "AI" else "Built-in",
                    style = TextStyle(
                        color = ColorProvider(palette.ideaText),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        Spacer(GlanceModifier.defaultWeight())
        when {
            !data.enabled -> Text(
                text = context.getString(com.diego.littly.R.string.widget_ideas_off),
                style = TextStyle(
                    color = ColorProvider(palette.ideaText),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2
            )
            !data.available -> Text(
                text = context.getString(com.diego.littly.R.string.widget_idea_empty),
                style = TextStyle(
                    color = ColorProvider(palette.ideaText),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2
            )
            else -> {
                Text(
                    text = data.headline.orEmpty(),
                    style = TextStyle(
                        color = ColorProvider(palette.ideaText),
                        fontSize = if (compact) 15.sp else 17.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = if (compact) 2 else 3
                )
                if (data.body != null && !compact) {
                    Spacer(GlanceModifier.height(3.dp))
                    Text(
                        text = data.body,
                        style = TextStyle(
                            color = ColorProvider(palette.ideaText),
                            fontSize = 11.sp
                        ),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

private fun openHabitAction(context: Context, habitId: Long) =
    actionStartActivityIntent(
        Intent(context, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_HABIT_ID, habitId)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    )
