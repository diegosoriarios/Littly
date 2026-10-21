package com.diego.littly.feature.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diego.littly.R
import com.diego.littly.core.designsystem.component.ActivityDots
import com.diego.littly.core.designsystem.component.EmptyState
import com.diego.littly.core.designsystem.component.HabitIconTile
import com.diego.littly.core.designsystem.component.LittlyCard
import com.diego.littly.core.designsystem.component.LittlyPill
import com.diego.littly.core.designsystem.component.LittlyPrimaryButton
import com.diego.littly.core.designsystem.component.SectionLabel
import com.diego.littly.core.designsystem.component.activityDots
import com.diego.littly.core.designsystem.component.habitAccent
import com.diego.littly.core.designsystem.theme.LittlySpacing
import com.diego.littly.core.designsystem.theme.littly
import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.HabitWithProgress
import java.time.LocalDate
import java.util.Locale

@Composable
fun HabitListScreen(
    onAddHabit: () -> Unit,
    onOpenHabit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HabitListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier.fillMaxSize()) {
        when {
            state.hasError -> EmptyState(
                icon = Icons.AutoMirrored.Rounded.FormatListBulleted,
                title = stringResource(R.string.today_error_title),
                body = stringResource(R.string.today_error_body),
                modifier = Modifier.fillMaxSize(),
                action = {
                    LittlyPrimaryButton(
                        text = stringResource(R.string.retry),
                        onClick = viewModel::retry
                    )
                }
            )
            state.isLoading -> Unit
            state.isEmpty -> EmptyState(
                icon = Icons.Rounded.LocalFlorist,
                title = stringResource(R.string.habits_empty_title),
                body = stringResource(R.string.habits_empty_body),
                modifier = Modifier.fillMaxSize(),
                action = {
                    LittlyPrimaryButton(
                        text = stringResource(R.string.add_habit),
                        onClick = onAddHabit,
                        icon = Icons.Rounded.Add
                    )
                }
            )
            else -> HabitGrid(
                state = state,
                onAddHabit = onAddHabit,
                onOpenHabit = onOpenHabit,
                onRestore = { viewModel.setArchived(it, false) },
                onFilter = viewModel::setFilter
            )
        }

        FloatingActionButton(
            onClick = onAddHabit,
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = LittlySpacing.gutter, bottom = LittlySpacing.xl)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_habit))
        }
    }
}

@Composable
private fun HabitGrid(
    state: HabitListState,
    onAddHabit: () -> Unit,
    onOpenHabit: (Long) -> Unit,
    onRestore: (Long) -> Unit,
    onFilter: (HabitFilter) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(
            start = LittlySpacing.gutter,
            end = LittlySpacing.gutter,
            top = LittlySpacing.md,
            bottom = 132.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(LittlySpacing.sm),
        verticalArrangement = Arrangement.spacedBy(LittlySpacing.sm)
    ) {
        item(key = "header", span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Text(
                    text = buildString {
                        append(stringResource(R.string.habits_active_count, state.activeCount))
                        if (state.archived.isNotEmpty()) {
                            append(" · ")
                            append(
                                stringResource(
                                    R.string.habits_paused_count,
                                    state.archived.size
                                )
                            )
                        }
                    }.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(LittlySpacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.habits_title),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        onClick = { onFilter(HabitFilter.ALL) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text(
                            text = stringResource(
                                R.string.habits_all_count,
                                state.habits.size + state.archived.size
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
        item(key = "filters", span = { GridItemSpan(maxLineSpan) }) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(LittlySpacing.xs),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = LittlySpacing.xs)
            ) {
                FilterPill(
                    label = stringResource(R.string.filter_all),
                    selected = state.filter == HabitFilter.ALL,
                    onClick = { onFilter(HabitFilter.ALL) }
                )
                FilterPill(
                    label = stringResource(R.string.filter_morning),
                    selected = state.filter == HabitFilter.MORNING,
                    onClick = { onFilter(HabitFilter.MORNING) }
                )
                FilterPill(
                    label = stringResource(R.string.filter_afternoon),
                    selected = state.filter == HabitFilter.AFTERNOON,
                    onClick = { onFilter(HabitFilter.AFTERNOON) }
                )
                FilterPill(
                    label = stringResource(R.string.filter_evening),
                    selected = state.filter == HabitFilter.EVENING,
                    onClick = { onFilter(HabitFilter.EVENING) }
                )
                if (state.archived.isNotEmpty()) {
                    FilterPill(
                        label = stringResource(R.string.filter_paused),
                        selected = state.filter == HabitFilter.PAUSED,
                        onClick = { onFilter(HabitFilter.PAUSED) }
                    )
                }
            }
        }

        if (state.isFilterEmpty) {
            item(key = "filter_empty", span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(
                    icon = Icons.AutoMirrored.Rounded.FormatListBulleted,
                    title = stringResource(R.string.habits_no_filter_title),
                    body = stringResource(R.string.habits_no_filter_body),
                    action = {
                        LittlyPrimaryButton(
                            text = stringResource(R.string.habits_filter_empty_cta),
                            onClick = { onFilter(HabitFilter.ALL) }
                        )
                    }
                )
            }
        } else {
            items(
                items = state.visibleActive,
                key = { it.habit.id }
            ) { progress ->
                HabitGridCard(
                    progress = progress,
                    isBest = progress.habit.id == state.bestHabitId,
                    completedDays = state.completedDaysByHabit[progress.habit.id].orEmpty(),
                    today = state.today,
                    onClick = { onOpenHabit(progress.habit.id) }
                )
            }
            items(
                items = state.visibleArchived,
                key = { "archived_${it.id}" }
            ) { habit ->
                PausedHabitCard(
                    habit = habit,
                    onClick = { onOpenHabit(habit.id) },
                    onRestore = { onRestore(habit.id) }
                )
            }
        }

        item(key = "new_habit", span = { GridItemSpan(maxLineSpan) }) {
            NewHabitCard(onClick = onAddHabit)
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    LittlyPill(text = label, selected = selected, onClick = onClick)
}

@Composable
private fun HabitGridCard(
    progress: HabitWithProgress,
    isBest: Boolean,
    completedDays: Set<LocalDate>,
    today: LocalDate,
    onClick: () -> Unit
) {
    val habit = progress.habit
    val palette = habitAccent(habit.colorIndex)
    val tinted = habit.colorIndex == 2 || habit.colorIndex == 3 || habit.colorIndex == 5
    val container = when {
        isBest -> MaterialTheme.littly.navyCard
        tinted -> palette.soft
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        isBest -> MaterialTheme.littly.onNavy
        tinted -> palette.onSoft
        else -> MaterialTheme.colorScheme.onSurface
    }
    val secondaryColor = when {
        isBest -> MaterialTheme.littly.onNavyMuted
        tinted -> palette.onSoft.copy(alpha = 0.72f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val dots = activityDots(habit, completedDays, today, count = 8)

    LittlyCard(
        color = container,
        borderColor = if (isBest) container else MaterialTheme.colorScheme.outline,
        contentPadding = PaddingValues(LittlySpacing.md),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 178.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(LittlySpacing.xs))
                HabitIconTile(habit = habit, size = 44.dp, solid = true)
            }
            Spacer(Modifier.height(LittlySpacing.lg))
            Text(
                text = progress.currentStreak.toString().padStart(2, '0'),
                style = MaterialTheme.typography.displaySmall,
                color = contentColor
            )
            Text(
                text = stringResource(
                    if (isBest) R.string.habit_day_streak_best else R.string.habit_day_streak
                ).uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall,
                color = secondaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(LittlySpacing.sm))
            ActivityDots(
                states = dots,
                accent = palette.accent,
                description = null
            )
        }
    }
}

@Composable
private fun PausedHabitCard(
    habit: Habit,
    onClick: () -> Unit,
    onRestore: () -> Unit
) {
    LittlyCard(
        onClick = onClick,
        contentPadding = PaddingValues(LittlySpacing.md),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 178.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(LittlySpacing.xs))
                HabitIconTile(habit = habit, size = 44.dp)
            }
            Spacer(Modifier.height(LittlySpacing.lg))
            SectionLabel(
                text = stringResource(R.string.filter_paused),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(LittlySpacing.xs))
            TextButton(onClick = onRestore, modifier = Modifier.heightIn(min = 48.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Unarchive,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(stringResource(R.string.restore_habit))
            }
        }
    }
}

@Composable
private fun NewHabitCard(onClick: () -> Unit) {
    val outline = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = outline,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
                    )
                )
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(LittlySpacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(LittlySpacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.habits_new_habit),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.habits_new_habit_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 390, heightDp = 420)
@Composable
private fun HabitCardPreview() {
    com.diego.littly.core.designsystem.theme.LittlyTheme(
        themeMode = com.diego.littly.core.domain.model.ThemeMode.LIGHT
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {
            HabitGridCard(
                progress = com.diego.littly.core.domain.model.HabitWithProgress(
                    habit = Habit(
                        id = 1,
                        name = "Read 20 pages",
                        iconKey = "book",
                        createdAt = LocalDate.of(2026, 1, 1),
                        goalDays = java.time.DayOfWeek.entries.toSet(),
                        colorIndex = 1
                    ),
                    completedToday = true,
                    currentStreak = 47,
                    totalCompletions = 74
                ),
                isBest = true,
                completedDays = setOf(LocalDate.now()),
                today = LocalDate.now(),
                onClick = {}
            )
            HabitGridCard(
                progress = com.diego.littly.core.domain.model.HabitWithProgress(
                    habit = Habit(
                        id = 2,
                        name = "Sketch 15 min",
                        iconKey = "brush",
                        createdAt = LocalDate.of(2026, 1, 1),
                        goalDays = java.time.DayOfWeek.entries.toSet(),
                        colorIndex = 0
                    ),
                    completedToday = false,
                    currentStreak = 12,
                    totalCompletions = 20
                ),
                isBest = false,
                completedDays = emptySet(),
                today = LocalDate.now(),
                onClick = {}
            )
        }
    }
}
