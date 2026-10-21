package com.diego.littly.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diego.littly.R
import com.diego.littly.core.designsystem.component.EmptyState
import com.diego.littly.core.designsystem.component.LittlyCard
import com.diego.littly.core.designsystem.component.LittlyPrimaryButton
import com.diego.littly.core.designsystem.component.ProgressRing
import com.diego.littly.core.designsystem.component.SectionLabel
import com.diego.littly.core.designsystem.component.habitAccent
import com.diego.littly.core.designsystem.component.readableOn
import com.diego.littly.core.designsystem.theme.LittlySpacing
import com.diego.littly.core.designsystem.theme.littly
import com.diego.littly.core.domain.stats.BestStreak
import com.diego.littly.core.domain.stats.DayCompletion
import com.diego.littly.core.domain.stats.MonthRate
import com.diego.littly.core.domain.stats.MonthSummary
import com.diego.littly.core.domain.stats.MonthTrendPoint
import com.diego.littly.core.domain.stats.TotalCheckIns
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val monthLabelFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())
private val dayAccessibilityFormatter = DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault())
private val sinceFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
private val streakDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())

@Composable
fun StatsScreen(
    onOpenHabit: (Long) -> Unit,
    onAddHabit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.hasError -> EmptyState(
            icon = Icons.Rounded.BarChart,
            title = stringResource(R.string.today_error_title),
            body = stringResource(R.string.today_error_body),
            modifier = modifier.fillMaxSize(),
            action = {
                LittlyPrimaryButton(
                    text = stringResource(R.string.retry),
                    onClick = viewModel::retry
                )
            }
        )
        state.isLoading -> Unit
        else -> StatsContent(
            state = state,
            onAddHabit = onAddHabit,
            onPrevious = viewModel::previousMonth,
            onNext = viewModel::nextMonth,
            modifier = modifier
        )
    }
}

@Composable
private fun StatsContent(
    state: StatsState,
    onAddHabit: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = LittlySpacing.gutter,
            end = LittlySpacing.gutter,
            top = LittlySpacing.md,
            bottom = LittlySpacing.xxl
        ),
        verticalArrangement = Arrangement.spacedBy(LittlySpacing.lg)
    ) {
        item(key = "header") {
            StatsHeader(
                month = state.selectedMonth,
                canGoNext = state.canGoNext,
                onPrevious = onPrevious,
                onNext = onNext
            )
        }
        if (!state.hasHabits) {
            item(key = "empty") {
                EmptyState(
                    icon = Icons.Rounded.BarChart,
                    title = stringResource(R.string.stats_empty_title),
                    body = stringResource(R.string.stats_empty_body),
                    action = {
                        LittlyPrimaryButton(
                            text = stringResource(R.string.stats_create_habit),
                            onClick = onAddHabit
                        )
                    }
                )
            }
            return@LazyColumn
        }
        item(key = "heatmap") {
            HeatmapCard(summary = state.monthSummary, today = state.today)
        }
        item(key = "summary") {
            SummaryCards(
                summary = state.monthSummary,
                previousRate = state.previousMonthRate,
                total = state.totalCheckIns,
                state = state
            )
        }
        item(key = "trend") {
            TrendCard(points = state.trend)
        }
        item(key = "best") {
            BestStreakCard(best = state.bestStreak)
        }
    }
}

@Composable
private fun StatsHeader(
    month: YearMonth,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column {
        SectionLabel(
            text = stringResource(R.string.stats_eyebrow),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(LittlySpacing.xxs))
        Text(
            text = stringResource(R.string.stats_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(LittlySpacing.sm))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevious, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.cd_previous_month),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = month.format(monthLabelFormatter),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                    IconButton(
                        onClick = onNext,
                        enabled = canGoNext,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.cd_next_month),
                            tint = if (canGoNext) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapCard(summary: MonthSummary?, today: java.time.LocalDate) {
    LittlyCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.stats_heatmap),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LegendDot(MaterialTheme.littly.successSoft)
                    LegendDot(MaterialTheme.littly.success.copy(alpha = 0.45f))
                    LegendDot(MaterialTheme.littly.success)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.stats_all_habits),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(LittlySpacing.md))
            WeekdayHeader()
            Spacer(Modifier.height(4.dp))
            MonthCalendar(summary = summary, today = today)
            Spacer(Modifier.height(LittlySpacing.md))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(LittlySpacing.md)) {
                    TextLegend(
                        color = MaterialTheme.littly.successSoft,
                        label = stringResource(R.string.stats_legend_none),
                        modifier = Modifier.weight(1f)
                    )
                    TextLegend(
                        color = MaterialTheme.littly.success.copy(alpha = 0.45f),
                        label = stringResource(R.string.stats_legend_some),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(LittlySpacing.md)) {
                    TextLegend(
                        color = MaterialTheme.littly.success,
                        label = stringResource(R.string.stats_legend_all),
                        modifier = Modifier.weight(1f)
                    )
                    TextLegend(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        label = stringResource(R.string.stats_legend_rest),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .padding(end = 3.dp)
            .size(9.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun TextLegend(color: Color, label: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WeekdayHeader() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        DayOfWeek.entries.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()).uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MonthCalendar(summary: MonthSummary?, today: java.time.LocalDate) {
    val days = summary?.days.orEmpty()
    if (days.isEmpty()) return
    val leading = days.first().date.dayOfWeek.value - 1
    val cells: List<DayCompletion?> = List(leading) { null } + days
    val rows = cells.chunked(7)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(7) { index ->
                    val day = row.getOrNull(index)
                    if (day == null) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        CalendarCell(day = day, today = today, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarCell(
    day: DayCompletion,
    today: java.time.LocalDate,
    modifier: Modifier = Modifier
) {
    val isToday = day.date == today
    val background = when {
        day.isFuture -> Color.Transparent
        day.due == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        day.allDone -> MaterialTheme.littly.success
        day.completed > 0 -> MaterialTheme.littly.success.copy(alpha = 0.45f)
        else -> MaterialTheme.littly.successSoft
    }
    val textColor = when {
        day.isFuture || day.due == 0 -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        day.allDone || day.completed > 0 -> readableOn(background)
        else -> MaterialTheme.colorScheme.onSurface
    }
    val description = when {
        day.isFuture -> "${day.date.format(dayAccessibilityFormatter)}: ${stringResource(R.string.stats_future_day)}"
        day.due == 0 -> "${day.date.format(dayAccessibilityFormatter)}: ${stringResource(R.string.stats_rest_day)}"
        else -> stringResource(
            R.string.stats_calendar_accessibility,
            day.date.format(dayAccessibilityFormatter),
            day.completed,
            day.due
        )
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .then(
                when {
                    isToday -> Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(10.dp)
                    )
                    day.isFuture -> Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    else -> Modifier
                }
            )
            .semantics(mergeDescendants = true) { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
        if (day.allDone) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(3.dp)
                    .size(9.dp)
            )
        } else if (day.completed > 0 && !day.isFuture) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
        }
    }
}

@Composable
private fun SummaryCards(
    summary: MonthSummary?,
    previousRate: MonthRate?,
    total: TotalCheckIns,
    state: StatsState
) {
    Row(horizontalArrangement = Arrangement.spacedBy(LittlySpacing.sm)) {
        CompletionCard(
            summary = summary,
            previousRate = previousRate,
            modifier = Modifier.weight(1f)
        )
        TotalCheckInsCard(
            total = total,
            state = state,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CompletionCard(
    summary: MonthSummary?,
    previousRate: MonthRate?,
    modifier: Modifier = Modifier
) {
    val rate = summary?.completionRate
    val due = summary?.due ?: 0
    val completed = summary?.completed ?: 0
    val deltaText = if (rate != null && previousRate?.completionRate != null && (previousRate.due > 0)) {
        val delta = (rate - previousRate.completionRate) * 100f
        val monthName = previousRate.month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        stringResource(
            R.string.stats_vs_previous,
            String.format(Locale.getDefault(), "%+.0f%%", delta),
            monthName
        )
    } else {
        null
    }
    LittlyCard(
        color = MaterialTheme.littly.navyCard,
        borderColor = MaterialTheme.littly.navyCard,
        contentPadding = PaddingValues(LittlySpacing.md),
        modifier = modifier.heightIn(min = 196.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SectionLabel(
                text = stringResource(R.string.stats_completion),
                color = MaterialTheme.littly.onNavyMuted
            )
            Spacer(Modifier.height(LittlySpacing.md))
            ProgressRing(
                completed = completed,
                total = due,
                size = 100.dp,
                trackColor = MaterialTheme.littly.onNavy.copy(alpha = 0.16f),
                progressColor = MaterialTheme.colorScheme.primary,
                labelColor = MaterialTheme.littly.onNavy,
                centerText = rate?.let { "${(it * 100).toInt()}%" } ?: "—",
                caption = stringResource(R.string.stats_this_month)
            )
            Spacer(Modifier.height(LittlySpacing.sm))
            Text(
                text = deltaText ?: stringResource(R.string.stats_this_month),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.littly.onNavyMuted,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TotalCheckInsCard(
    total: TotalCheckIns,
    state: StatsState,
    modifier: Modifier = Modifier
) {
    LittlyCard(
        color = MaterialTheme.colorScheme.primaryContainer,
        borderColor = MaterialTheme.colorScheme.primaryContainer,
        contentPadding = PaddingValues(LittlySpacing.md),
        modifier = modifier.heightIn(min = 196.dp)
    ) {
        Column {
            SectionLabel(
                text = stringResource(R.string.stats_total_checkins),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(LittlySpacing.xs))
            Text(
                text = NumberFormat.getIntegerInstance().format(total.count),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = total.since?.let {
                    stringResource(R.string.stats_since, it.format(sinceFormatter))
                } ?: stringResource(R.string.stats_no_checkins),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(LittlySpacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                state.habits.take(4).forEach { habit ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(habitAccent(habit.colorIndex).accent)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendCard(points: List<MonthTrendPoint>) {
    val nonNull = points.mapIndexedNotNull { index, point ->
        point.completionRate?.let { rate -> index to rate }
    }
    val description = if (nonNull.isEmpty()) {
        stringResource(R.string.stats_trend_empty)
    } else {
        val parts = nonNull.joinToString(", ") { (index, rate) ->
            val month = points[index].month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            "$month ${(rate * 100).toInt()}%"
        }
        stringResource(R.string.stats_trend_accessibility, parts)
    }
    val delta = if (nonNull.size >= 2) nonNull.last().second - nonNull.first().second else null

    LittlyCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.stats_trend),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (delta != null) {
                    Text(
                        text = String.format(Locale.getDefault(), "%+.0f%%", delta * 100f),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (delta >= 0) MaterialTheme.littly.success else MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(Modifier.height(LittlySpacing.md))
            if (nonNull.size < 2) {
                Text(
                    text = stringResource(R.string.stats_trend_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                TrendChart(
                    points = points,
                    nonNull = nonNull,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .semantics { contentDescription = description }
                )
                Spacer(Modifier.height(LittlySpacing.xs))
                Row(modifier = Modifier.fillMaxWidth()) {
                    points.forEach { point ->
                        Text(
                            text = point.month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                .uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendChart(
    points: List<MonthTrendPoint>,
    nonNull: List<Pair<Int, Float>>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val surfaceColor = MaterialTheme.colorScheme.surface
    Canvas(modifier = modifier) {
        val top = 8.dp.toPx()
        val bottom = size.height - 8.dp.toPx()
        val left = 4.dp.toPx()
        val right = size.width - 8.dp.toPx()
        val height = bottom - top
        val width = right - left
        repeat(3) { i ->
            val y = top + height * (i / 2f)
            drawLine(
                color = gridColor,
                start = Offset(left, y),
                end = Offset(right, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        val xFor = { index: Int ->
            if (points.size <= 1) left else left + width * (index.toFloat() / (points.size - 1).toFloat())
        }
        val yFor = { rate: Float -> bottom - height * rate.coerceIn(0f, 1f) }
        val path = Path()
        nonNull.forEachIndexed { i, (index, rate) ->
            val x = xFor(index)
            val y = yFor(rate)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        if (nonNull.size >= 2) {
            val fill = Path().apply {
                val firstX = xFor(nonNull.first().first)
                val lastX = xFor(nonNull.last().first)
                moveTo(firstX, bottom)
                lineTo(firstX, yFor(nonNull.first().second))
                nonNull.drop(1).forEach { (index, rate) ->
                    lineTo(xFor(index), yFor(rate))
                }
                lineTo(lastX, bottom)
                close()
            }
            drawPath(fill, color = lineColor.copy(alpha = 0.12f))
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        nonNull.forEach { (index, rate) ->
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(xFor(index), yFor(rate))
            )
            drawCircle(
                color = surfaceColor,
                radius = 1.8.dp.toPx(),
                center = Offset(xFor(index), yFor(rate))
            )
        }
    }
}

@Composable
private fun BestStreakCard(best: BestStreak?) {
    if (best == null) {
        LittlyCard(
            color = MaterialTheme.colorScheme.primaryContainer,
            borderColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SectionLabel(
                    text = stringResource(R.string.stats_best_streak),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(LittlySpacing.xs))
                Text(
                    text = stringResource(R.string.stats_best_empty),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        return
    }
    val range = best.run.start?.let { start ->
        val end = best.run.end
        if (end != null) {
            "${start.format(streakDateFormatter)} – ${end.format(streakDateFormatter)}"
        } else {
            start.format(streakDateFormatter)
        }
    }.orEmpty()
    LittlyCard(
        color = MaterialTheme.colorScheme.primary,
        borderColor = MaterialTheme.colorScheme.primary,
        contentPadding = PaddingValues(LittlySpacing.lg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                SectionLabel(
                    text = stringResource(R.string.stats_best_streak),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(LittlySpacing.xxs))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = best.run.length.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(LittlySpacing.xs))
                    Text(
                        text = stringResource(R.string.stats_days),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                        modifier = Modifier.padding(bottom = 7.dp)
                    )
                }
                Text(
                    text = "${best.habit.name} · $range",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 390, heightDp = 260)
@Composable
private fun BestStreakPreview() {
    com.diego.littly.core.designsystem.theme.LittlyTheme(
        themeMode = com.diego.littly.core.domain.model.ThemeMode.LIGHT
    ) {
        Box(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {
            BestStreakCard(
                best = BestStreak(
                    habit = com.diego.littly.core.domain.model.Habit(
                        id = 1,
                        name = "Read 20 pages",
                        createdAt = java.time.LocalDate.of(2026, 1, 1),
                        goalDays = java.time.DayOfWeek.entries.toSet()
                    ),
                    run = com.diego.littly.core.domain.streak.StreakRun(
                        length = 92,
                        start = java.time.LocalDate.of(2026, 2, 1),
                        end = java.time.LocalDate.of(2026, 5, 3)
                    )
                )
            )
        }
    }
}
