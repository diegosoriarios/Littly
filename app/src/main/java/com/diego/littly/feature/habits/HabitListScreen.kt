package com.diego.littly.feature.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diego.littly.R
import com.diego.littly.core.designsystem.component.StreakBadge
import com.diego.littly.core.designsystem.component.habitColor
import com.diego.littly.core.designsystem.component.readableOn
import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.HabitWithProgress

@Composable
fun HabitListScreen(
    onAddHabit: () -> Unit,
    onOpenHabit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HabitListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> Unit
            state.habits.isEmpty() && state.archived.isEmpty() -> EmptyState()
            else -> HabitListContent(
                state = state,
                onOpenHabit = onOpenHabit,
                onToggleToday = viewModel::toggleToday,
                onUnarchive = { viewModel.setArchived(it, false) }
            )
        }
        ExtendedFloatingActionButton(
            onClick = onAddHabit,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.add_habit))
        }
    }
}

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = stringResource(R.string.tagline),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.habits_empty_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.habits_empty_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HabitListContent(
    state: HabitListState,
    onOpenHabit: (Long) -> Unit,
    onToggleToday: (Long) -> Unit,
    onUnarchive: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(state.habits, key = { it.habit.id }) { progress ->
            HabitCard(
                progress = progress,
                onClick = { onOpenHabit(progress.habit.id) },
                onToggleToday = { onToggleToday(progress.habit.id) }
            )
        }
        if (state.archived.isNotEmpty()) {
            item(key = "archived_header") {
                Text(
                    text = stringResource(R.string.archived_header),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
            }
            items(state.archived, key = { "archived_${it.id}" }) { habit ->
                ArchivedHabitRow(habit = habit, onUnarchive = { onUnarchive(habit.id) })
            }
        }
    }
}

@Composable
private fun HabitCard(
    progress: HabitWithProgress,
    onClick: () -> Unit,
    onToggleToday: () -> Unit
) {
    val habit = progress.habit
    val accent = habitColor(habit.colorIndex)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = accent
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
                    Text(
                        text = habit.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = readableOn(accent)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StreakBadge(count = progress.currentStreak)
                    if (progress.currentStreak <= 0) {
                        Text(
                            text = stringResource(R.string.habit_start_today),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = pluralStringResource(
                                R.plurals.habit_total_times,
                                progress.totalCompletions,
                                progress.totalCompletions
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            DoneToggle(
                done = progress.completedToday,
                accent = accent,
                onToggle = onToggleToday
            )
        }
    }
}

@Composable
private fun DoneToggle(
    done: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    onToggle: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (done) accent else MaterialTheme.colorScheme.surfaceVariant,
        label = "doneToggleColor"
    )
    Surface(
        shape = CircleShape,
        color = color,
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onToggle)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (done) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = stringResource(R.string.cd_mark_done),
                    tint = readableOn(accent)
                )
            }
        }
    }
}

@Composable
private fun ArchivedHabitRow(
    habit: Habit,
    onUnarchive: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onUnarchive) {
                Icon(
                    imageVector = Icons.Outlined.Unarchive,
                    contentDescription = stringResource(R.string.cd_unarchive),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
