package com.diego.littly.feature.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diego.littly.R
import com.diego.littly.core.designsystem.component.EmptyState
import com.diego.littly.core.designsystem.component.HabitHistoryGrid
import com.diego.littly.core.designsystem.component.HabitIconTile
import com.diego.littly.core.designsystem.component.LittlyCard
import com.diego.littly.core.designsystem.component.LittlyIconButton
import com.diego.littly.core.designsystem.component.LittlyPrimaryButton
import com.diego.littly.core.designsystem.component.LittlyTopBar
import com.diego.littly.core.designsystem.component.SectionLabel
import com.diego.littly.core.designsystem.component.habitAccent
import com.diego.littly.core.designsystem.theme.LittlySpacing
import com.diego.littly.core.designsystem.theme.littly
import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.Weekdays
import com.diego.littly.core.llm.ModelLocator
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
private val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
private val historyDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

@Composable
fun HabitDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HabitDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ideaState by viewModel.ideaState.collectAsStateWithLifecycle()
    var showArchiveDialog by remember { mutableStateOf(false) }
    var archiveRequested by remember { mutableStateOf(false) }

    LaunchedEffect(archiveRequested, state.habit?.archivedAt) {
        if (archiveRequested && state.habit?.archivedAt != null) {
            onBack()
        }
    }

    Column(modifier.fillMaxSize()) {
        LittlyTopBar(
            title = stringResource(R.string.habit_detail_title),
            onBack = onBack,
            actions = {
                if (state.habit != null) {
                    val archived = state.habit?.archivedAt != null
                    LittlyIconButton(
                        icon = if (archived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                        contentDescription = stringResource(
                            if (archived) R.string.detail_restore else R.string.cd_archive
                        ),
                        onClick = {
                            if (archived) {
                                viewModel.setArchived(false)
                            } else {
                                showArchiveDialog = true
                            }
                        }
                    )
                    LittlyIconButton(
                        icon = Icons.Rounded.Edit,
                        contentDescription = stringResource(R.string.cd_edit_habit),
                        onClick = { state.habit?.let { onEdit(it.id) } }
                    )
                }
            }
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.habit == null -> EmptyState(
                icon = Icons.Rounded.Refresh,
                title = stringResource(R.string.detail_not_found_title),
                body = stringResource(R.string.detail_not_found_body),
                modifier = Modifier.fillMaxSize(),
                action = {
                    LittlyPrimaryButton(text = stringResource(R.string.cd_back), onClick = onBack)
                }
            )
            else -> DetailContent(
                state = state,
                ideaState = ideaState,
                onToggle = viewModel::toggleToday,
                onGenerateIdea = viewModel::generateIdea,
                onRestore = { viewModel.setArchived(false) }
            )
        }
    }

    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text(stringResource(R.string.detail_archive_title)) },
            text = { Text(stringResource(R.string.detail_archive_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showArchiveDialog = false
                        archiveRequested = true
                        viewModel.setArchived(true)
                    }
                ) {
                    Text(stringResource(R.string.detail_archive))
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun DetailContent(
    state: HabitDetailState,
    ideaState: IdeaStreamState,
    onToggle: () -> Unit,
    onGenerateIdea: () -> Unit,
    onRestore: () -> Unit
) {
    val habit = state.habit ?: return
    val accent = habitAccent(habit.colorIndex).accent
    LazyColumn(
        contentPadding = PaddingValues(
            start = LittlySpacing.gutter,
            end = LittlySpacing.gutter,
            top = LittlySpacing.xs,
            bottom = LittlySpacing.xxl
        ),
        verticalArrangement = Arrangement.spacedBy(LittlySpacing.xl)
    ) {
        item(key = "hero") {
            HeroCard(habit = habit, state = state)
        }
        item(key = "history") {
            HistoryCard(habit = habit, state = state, accent = accent)
        }
        item(key = "idea") {
            IdeaSection(
                habit = habit,
                ideaState = ideaState,
                onGenerate = onGenerateIdea
            )
        }
        item(key = "actions") {
            if (habit.archivedAt != null) {
                ArchivedCard(onRestore = onRestore)
            } else {
                CompletionButton(
                    completed = state.completedToday,
                    onToggle = onToggle
                )
            }
        }
        if (ideaState.enabled) {
            item(key = "offline_note") {
                OfflineNote()
            }
        }
    }
}

@Composable
private fun HeroCard(habit: Habit, state: HabitDetailState) {
    LittlyCard(
        color = MaterialTheme.littly.navyCard,
        borderColor = MaterialTheme.littly.navyCard,
        contentPadding = PaddingValues(LittlySpacing.lg),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HabitIconTile(habit = habit, size = 56.dp, solid = true)
                Spacer(Modifier.width(LittlySpacing.md))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.littly.onNavy,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = scheduleLine(habit),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.littly.onNavyMuted
                    )
                }
            }
            Spacer(Modifier.height(LittlySpacing.lg))
            SectionLabel(
                text = stringResource(R.string.today_current_streak),
                color = MaterialTheme.littly.onNavyMuted
            )
            Spacer(Modifier.height(LittlySpacing.xxs))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = state.currentStreak.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.littly.onNavy
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = pluralStringResource(
                        R.plurals.days_count,
                        state.currentStreak,
                        state.currentStreak
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.littly.onNavyMuted,
                    modifier = Modifier.padding(bottom = 7.dp)
                )
            }
            Spacer(Modifier.height(LittlySpacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(LittlySpacing.xs)) {
                MiniStat(
                    label = stringResource(R.string.detail_best),
                    value = pluralStringResource(
                        R.plurals.days_count,
                        state.longestStreak,
                        state.longestStreak
                    ),
                    icon = Icons.Rounded.EmojiEvents,
                    tint = MaterialTheme.littly.ideaAccent,
                    modifier = Modifier.weight(1f)
                )
                MiniStat(
                    label = stringResource(R.string.detail_30day_rate),
                    value = state.thirtyDayRate?.let { "${(it * 100).toInt()}%" } ?: "—",
                    icon = Icons.Rounded.LocalFireDepartment,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MiniStat(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.littly.navyCardSoft)
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Text(
            text = label.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.littly.onNavyMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(5.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.littly.onNavy,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HistoryCard(habit: Habit, state: HabitDetailState, accent: Color) {
    val history = remember(habit, state.today, state.completedDays) {
        historySummary(habit, state.completedDays, state.today)
    }
    LittlyCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.detail_three_months),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(
                            R.string.detail_history_summary,
                            history.done,
                            history.goals
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = history.rangeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(habitAccent(habit.colorIndex).soft)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
            Spacer(Modifier.height(LittlySpacing.lg))
            HabitHistoryGrid(
                completedDays = state.completedDays,
                goalDays = habit.goalDays,
                createdAt = habit.createdAt,
                today = state.today,
                accentColor = accent,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(LittlySpacing.md))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
            Spacer(Modifier.height(LittlySpacing.sm))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(LittlySpacing.md)) {
                    HistoryLegend(
                        color = accent,
                        label = stringResource(R.string.legend_done),
                        modifier = Modifier.weight(1f)
                    )
                    HistoryLegend(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        label = stringResource(R.string.legend_goal),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(LittlySpacing.md)) {
                    HistoryLegend(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        label = stringResource(R.string.legend_rest),
                        modifier = Modifier.weight(1f)
                    )
                    HistoryLegend(
                        color = MaterialTheme.colorScheme.primary,
                        label = stringResource(R.string.legend_today),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryLegend(color: Color, label: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(11.dp)
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
private fun IdeaSection(
    habit: Habit,
    ideaState: IdeaStreamState,
    onGenerate: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel(
                text = stringResource(R.string.detail_today_idea),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (ideaState.enabled) {
                TextButton(onClick = onGenerate, enabled = !ideaState.isGenerating) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.ideas_new_idea))
                }
            }
        }
        Spacer(Modifier.height(LittlySpacing.xs))
        LittlyCard(
            color = MaterialTheme.littly.ideaBackground,
            borderColor = MaterialTheme.littly.ideaBorder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                if (!ideaState.enabled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.littly.ideaAccent
                        )
                        Spacer(Modifier.width(LittlySpacing.sm))
                        Text(
                            text = stringResource(R.string.ideas_disabled_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.littly.onIdea
                        )
                    }
                    return@Column
                }
                when {
                    ideaState.isGenerating -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.littly.ideaAccent
                            )
                            Spacer(Modifier.width(LittlySpacing.sm))
                            Text(
                                text = stringResource(R.string.ideas_thinking),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.littly.onIdea
                            )
                        }
                    }
                    ideaState.latestIdea != null -> {
                        val idea = ideaState.latestIdea!!
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.littly.ideaAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFFFFFCF8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(LittlySpacing.sm))
                            Column(Modifier.weight(1f)) {
                                SectionLabel(
                                    text = stringResource(R.string.ideas_label),
                                    color = MaterialTheme.littly.ideaLabel
                                )
                                Text(
                                    text = stringResource(R.string.today_idea_for, habit.name),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IdeaSourcePill(model = idea.model)
                        }
                        Spacer(Modifier.height(LittlySpacing.md))
                        if (!idea.title.isNullOrBlank()) {
                            Text(
                                text = idea.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.littly.onIdea
                            )
                            Spacer(Modifier.height(LittlySpacing.xs))
                        }
                        Text(
                            text = idea.text,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.littly.onIdea
                        )
                    }
                    else -> {
                        Text(
                            text = stringResource(R.string.ideas_empty_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.littly.onIdea
                        )
                    }
                }
                if (ideaState.error && !ideaState.isGenerating) {
                    Spacer(Modifier.height(LittlySpacing.sm))
                    Text(
                        text = stringResource(R.string.ideas_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        val pastIdeas = ideaState.recentIdeas.drop(1).take(3)
        if (pastIdeas.isNotEmpty()) {
            Spacer(Modifier.height(LittlySpacing.md))
            SectionLabel(
                text = stringResource(R.string.ideas_recent_header),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(LittlySpacing.xs))
            pastIdeas.forEach { idea ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 7.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.littly.ideaAccent.copy(alpha = 0.7f))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = idea.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun IdeaSourcePill(model: String) {
    val onDevice = model == ModelLocator.MODEL_ID
    Text(
        text = stringResource(
            if (onDevice) R.string.idea_source_on_device else R.string.idea_source_builtin
        ),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .border(1.dp, MaterialTheme.littly.ideaBorder, CircleShape)
            .padding(horizontal = 9.dp, vertical = 5.dp)
    )
}

@Composable
private fun CompletionButton(completed: Boolean, onToggle: () -> Unit) {
    Button(
        onClick = onToggle,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (completed) {
                MaterialTheme.littly.success
            } else {
                MaterialTheme.colorScheme.primary
            },
            contentColor = if (completed) {
                MaterialTheme.littly.onSuccess
            } else {
                MaterialTheme.colorScheme.onPrimary
            }
        ),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    ) {
        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(LittlySpacing.xs))
        Text(
            text = stringResource(
                if (completed) R.string.detail_done_today else R.string.detail_mark_done
            ),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun ArchivedCard(onRestore: () -> Unit) {
    LittlyCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(R.string.detail_archived_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(LittlySpacing.sm))
            LittlyPrimaryButton(
                text = stringResource(R.string.detail_restore),
                onClick = onRestore,
                icon = Icons.Outlined.Unarchive,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun OfflineNote() {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                RoundedCornerShape(20.dp)
            )
            .padding(LittlySpacing.md)
    ) {
        Icon(
            imageVector = Icons.Rounded.Lightbulb,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(LittlySpacing.sm))
        Column {
            Text(
                text = stringResource(R.string.detail_offline_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.detail_offline_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class HistorySummary(
    val done: Int,
    val goals: Int,
    val rangeLabel: String
)

private fun historySummary(
    habit: Habit,
    completedDays: Set<LocalDate>,
    today: LocalDate,
    weeks: Int = 13
): HistorySummary {
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val start = monday.minusWeeks((weeks - 1).toLong())
    var done = 0
    var goals = 0
    var date = start
    while (!date.isAfter(today)) {
        if (!date.isBefore(habit.createdAt) && Weekdays.isGoalDay(date, habit.goalDays)) {
            goals++
            if (date in completedDays) done++
        }
        date = date.plusDays(1)
    }
    return HistorySummary(
        done = done,
        goals = goals,
        rangeLabel = "${start.format(monthFormatter)} – ${today.format(monthFormatter)}"
    )
}

@Composable
private fun scheduleLine(habit: Habit): String {
    val days = if (habit.goalDays.size == DayOfWeek.entries.size) {
        stringResource(R.string.detail_schedule_every_day)
    } else {
        habit.goalDays.sortedBy { it.value }
            .joinToString(", ") { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
    }
    val reminder = habit.reminder
    val time = reminder.minuteOfDay?.let { minute ->
        LocalTime.of(minute / 60, minute % 60).format(timeFormatter.withLocale(Locale.getDefault()))
    }
    val reminderText = if (reminder.enabled && time != null) {
        stringResource(R.string.detail_reminder_at, time)
    } else {
        stringResource(R.string.detail_no_reminder)
    }
    return "$days · $reminderText"
}
