package com.diego.littly.feature.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diego.littly.R
import com.diego.littly.core.designsystem.component.HabitHistoryGrid
import com.diego.littly.core.designsystem.component.habitColor
import com.diego.littly.core.designsystem.component.readableOn
import com.diego.littly.feature.habits.IdeaStreamState

@Composable
fun HabitDetailScreen(
    onEdit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HabitDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ideaState by viewModel.ideaState.collectAsStateWithLifecycle()

    when {
        state.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.habit == null -> {
            Box(Modifier.fillMaxSize())
        }
        else -> {
            val habit = state.habit!!
            val accent = habitColor(habit.colorIndex)
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = accent) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                            Text(
                                text = habit.name.take(1).uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                color = readableOn(accent)
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onEdit(habit.id) }) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.cd_edit_habit)
                        )
                    }
                    IconButton(onClick = { viewModel.setArchived(archived = true) }) {
                        Icon(
                            imageVector = Icons.Outlined.Archive,
                            contentDescription = stringResource(R.string.cd_archive)
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                TodayButton(
                    completedToday = state.completedToday,
                    accent = accent,
                    onToggle = viewModel::toggleToday
                )
                Spacer(Modifier.height(24.dp))
                StatsRow(
                    currentStreak = state.currentStreak,
                    longestStreak = state.longestStreak,
                    total = state.totalCompletions
                )
                Spacer(Modifier.height(28.dp))
                Text(
                    text = stringResource(R.string.history_header),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(Modifier.padding(16.dp)) {
                        if (state.completedDays.isEmpty()) {
                            Text(
                                text = stringResource(R.string.history_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        HabitHistoryGrid(
                            completedDays = state.completedDays,
                            goalDays = habit.goalDays,
                            createdAt = habit.createdAt,
                            today = state.today,
                            accentColor = accent,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Legend(accent = accent)
                    }
                }
                Spacer(Modifier.height(28.dp))
                IdeasSection(
                    ideaState = ideaState,
                    accent = accent,
                    onGenerate = viewModel::generateIdea
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TodayButton(
    completedToday: Boolean,
    accent: Color,
    onToggle: () -> Unit
) {
    val container by animateColorAsState(
        targetValue = if (completedToday) accent else MaterialTheme.colorScheme.primary,
        label = "todayButton"
    )
    FilledTonalButton(
        onClick = onToggle,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = container,
            contentColor = if (completedToday) readableOn(accent) else MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        if (completedToday) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.detail_done_today))
        } else {
            Text(stringResource(R.string.detail_mark_done))
        }
    }
}

@Composable
private fun StatsRow(
    currentStreak: Int,
    longestStreak: Int,
    total: Int
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(
            value = currentStreak.toString(),
            label = stringResource(R.string.stat_current),
            icon = { Icon(Icons.Rounded.LocalFireDepartment, null, tint = MaterialTheme.colorScheme.tertiary) },
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = longestStreak.toString(),
            label = stringResource(R.string.stat_longest),
            icon = { Icon(Icons.Rounded.EmojiEvents, null, tint = MaterialTheme.colorScheme.secondary) },
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = total.toString(),
            label = stringResource(R.string.stat_total),
            icon = { Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 14.dp)
        ) {
            icon()
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Legend(accent: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(accent, RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.legend_done),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(4.dp)
                    )
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.legend_goal),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun IdeasSection(
    ideaState: IdeaStreamState,
    accent: Color,
    onGenerate: () -> Unit
) {
    Text(
        text = stringResource(R.string.ideas_header),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
    )
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            when {
                ideaState.isGenerating -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = accent
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (ideaState.streamText.isBlank()) {
                                stringResource(R.string.ideas_thinking)
                            } else {
                                ideaState.streamText
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (ideaState.streamText.isBlank()) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                ideaState.latestIdea != null -> {
                    val idea = ideaState.latestIdea!!
                    if (!idea.title.isNullOrBlank()) {
                        Text(
                            text = idea.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = accent
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        text = idea.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                else -> {
                    Text(
                        text = stringResource(R.string.ideas_empty_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (ideaState.error && !ideaState.isGenerating) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.ideas_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(12.dp))
            FilledTonalButton(
                onClick = onGenerate,
                enabled = !ideaState.isGenerating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.ideas_new_idea))
            }
        }
    }
    val pastIdeas = ideaState.recentIdeas.drop(1).take(4)
    if (pastIdeas.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.ideas_recent_header),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        pastIdeas.forEach { idea ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 7.dp)
                        .size(6.dp)
                        .background(accent.copy(alpha = 0.6f), CircleShape)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = idea.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
