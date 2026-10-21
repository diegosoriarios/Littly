package com.diego.littly.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diego.littly.R
import com.diego.littly.core.designsystem.component.ActivityDots
import com.diego.littly.core.designsystem.component.CompletionControl
import com.diego.littly.core.designsystem.component.EmptyState
import com.diego.littly.core.designsystem.component.HabitIconTile
import com.diego.littly.core.designsystem.component.LittlyAvatar
import com.diego.littly.core.designsystem.component.LittlyCard
import com.diego.littly.core.designsystem.component.LittlyIconButton
import com.diego.littly.core.designsystem.component.LittlyPrimaryButton
import com.diego.littly.core.designsystem.component.LittlySecondaryButton
import com.diego.littly.core.designsystem.component.ProgressRing
import com.diego.littly.core.designsystem.component.SectionLabel
import com.diego.littly.core.designsystem.component.StreakBadge
import com.diego.littly.core.designsystem.component.activityDots
import com.diego.littly.core.designsystem.component.habitAccent
import com.diego.littly.core.designsystem.theme.LittlySpacing
import com.diego.littly.core.designsystem.theme.littly
import com.diego.littly.core.domain.model.HabitWithProgress
import com.diego.littly.core.domain.stats.DayCompletion
import com.diego.littly.core.llm.ModelLocator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val todayDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())

@Composable
fun TodayScreen(
    onAddHabit: () -> Unit,
    onOpenHabit: (Long) -> Unit,
    onOpenHabitWithIdea: (Long) -> Unit,
    onSeeAllHabits: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenReminders: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier.fillMaxSize()) {
        when {
            state.hasError -> TodayError(onRetry = viewModel::retry)
            state.isLoading -> Unit
            state.allActiveHabits.isEmpty() -> {
                EmptyState(
                    icon = Icons.Rounded.LocalFlorist,
                    title = stringResource(R.string.today_empty_title),
                    body = stringResource(R.string.today_empty_body),
                    modifier = Modifier.align(Alignment.Center),
                    action = {
                        LittlyPrimaryButton(
                            text = stringResource(R.string.add_habit),
                            onClick = onAddHabit,
                            icon = Icons.Rounded.Add
                        )
                    }
                )
            }
            else -> TodayContent(
                state = state,
                onToggle = viewModel::toggleToday,
                onOpenHabit = onOpenHabit,
                onOpenHabitWithIdea = onOpenHabitWithIdea,
                onSeeAllHabits = onSeeAllHabits,
                onOpenProfile = onOpenProfile,
                onOpenReminders = onOpenReminders
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
private fun TodayContent(
    state: TodayState,
    onToggle: (Long) -> Unit,
    onOpenHabit: (Long) -> Unit,
    onOpenHabitWithIdea: (Long) -> Unit,
    onSeeAllHabits: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenReminders: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = LittlySpacing.gutter,
            end = LittlySpacing.gutter,
            top = LittlySpacing.md,
            bottom = 132.dp
        ),
        verticalArrangement = Arrangement.spacedBy(LittlySpacing.xl)
    ) {
        item(key = "header") {
            TodayHeader(
                state = state,
                onOpenProfile = onOpenProfile,
                onOpenReminders = onOpenReminders
            )
        }
        item(key = "hero") {
            HeroCard(state)
        }
        item(key = "habits_header") {
            HabitsHeader(
                completed = state.completedCount,
                total = state.dueHabits.size,
                onSeeAll = onSeeAllHabits
            )
        }
        item(key = "habits") {
            if (state.dueHabits.isEmpty()) {
                LittlyCard(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RestDayContent()
                }
            } else {
                if (state.allDueDone) {
                    AllDoneBanner()
                    Spacer(Modifier.height(LittlySpacing.sm))
                }
                HabitRows(
                    habits = state.dueHabits,
                    completedDaysByHabit = state.completedDaysByHabit,
                    today = state.today,
                    onToggle = onToggle,
                    onOpenHabit = onOpenHabit
                )
            }
        }
        state.idea?.let { idea ->
            item(key = "idea") {
                IdeaCard(
                    idea = idea,
                    onOpenHabit = { onOpenHabit(idea.habitId) },
                    onAnotherIdea = { onOpenHabitWithIdea(idea.habitId) }
                )
            }
        }
    }
}

@Composable
private fun TodayHeader(
    state: TodayState,
    onOpenProfile: () -> Unit,
    onOpenReminders: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = state.today.format(todayDateFormatter).uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            LittlyIconButton(
                icon = Icons.Outlined.NotificationsNone,
                contentDescription = stringResource(R.string.cd_notifications),
                onClick = onOpenReminders
            )
            Spacer(Modifier.width(LittlySpacing.xs))
            val profileDescription = stringResource(R.string.cd_profile)
            Box(
                Modifier
                    .clickable(role = Role.Button, onClick = onOpenProfile)
                    .semantics { contentDescription = profileDescription }
            ) {
                LittlyAvatar(name = state.displayName)
            }
        }
        Spacer(Modifier.height(LittlySpacing.md))
        val baseGreeting = when (state.greeting) {
            TodayGreeting.MORNING -> stringResource(R.string.greeting_morning)
            TodayGreeting.AFTERNOON -> stringResource(R.string.greeting_afternoon)
            TodayGreeting.EVENING -> stringResource(R.string.greeting_evening)
        }
        val greeting = if (state.displayName.isBlank()) {
            baseGreeting
        } else {
            stringResource(R.string.greeting_named, baseGreeting, state.displayName)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(LittlySpacing.xs))
            Icon(
                imageVector = Icons.Rounded.LocalFlorist,
                contentDescription = null,
                tint = MaterialTheme.littly.success,
                modifier = Modifier
                    .padding(bottom = 7.dp)
                    .size(28.dp)
            )
        }
        Spacer(Modifier.height(LittlySpacing.xs))
        Text(
            text = if (state.dueHabits.isEmpty()) {
                stringResource(R.string.today_no_habits_due)
            } else {
                stringResource(
                    R.string.today_progress,
                    state.completedCount,
                    state.dueHabits.size
                )
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HeroCard(state: TodayState) {
    val best = state.overview?.bestCurrent
    LittlyCard(
        color = MaterialTheme.littly.navyCard,
        borderColor = MaterialTheme.littly.navyCard,
        contentPadding = PaddingValues(LittlySpacing.lg),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    SectionLabel(
                        text = stringResource(R.string.today_current_streak),
                        color = MaterialTheme.littly.onNavyMuted
                    )
                    Spacer(Modifier.height(LittlySpacing.xs))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = (best?.currentStreak ?: 0).toString(),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.littly.onNavy
                        )
                        Spacer(Modifier.width(LittlySpacing.xs))
                        Text(
                            text = pluralStringResource(
                                R.plurals.days_count,
                                best?.currentStreak ?: 0,
                                best?.currentStreak ?: 0
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.littly.onNavyMuted,
                            modifier = Modifier.padding(bottom = 7.dp)
                        )
                    }
                    Spacer(Modifier.height(LittlySpacing.xs))
                    Text(
                        text = if (best != null) {
                            stringResource(R.string.today_streak_subtitle, best.habit.name)
                        } else {
                            stringResource(R.string.today_streak_empty)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.littly.onNavyMuted
                    )
                }
                ProgressRing(
                    completed = state.completedCount,
                    total = state.dueHabits.size,
                    trackColor = MaterialTheme.littly.onNavy.copy(alpha = 0.18f),
                    progressColor = MaterialTheme.colorScheme.primary,
                    labelColor = MaterialTheme.littly.onNavy
                )
            }
            Spacer(Modifier.height(LittlySpacing.lg))
            WeekStrip(
                week = state.overview?.week.orEmpty(),
                today = state.today
            )
        }
    }
}

@Composable
private fun WeekStrip(week: List<DayCompletion>, today: LocalDate) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        week.forEach { day ->
            val isToday = day.date == today
            val label = day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
            val contentDescription = if (day.hasOpportunity) {
                stringResource(
                    R.string.stats_calendar_accessibility,
                    day.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())),
                    day.completed,
                    day.due
                )
            } else {
                stringResource(
                    R.string.stats_rest_day
                ) + " · " + day.date.format(
                    DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .semantics { this.contentDescription = contentDescription }
            ) {
                Text(
                    text = label.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) MaterialTheme.littly.onNavy else MaterialTheme.littly.onNavyMuted
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                day.allDone -> MaterialTheme.littly.success
                                else -> MaterialTheme.littly.navyCardSoft
                            }
                        )
                        .then(
                            if (isToday) {
                                Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        day.allDone -> Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.littly.onSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                        !day.hasOpportunity -> Box(
                            Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.littly.onNavyMuted.copy(alpha = 0.5f))
                        )
                        isToday -> Text(
                            text = day.date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.littly.onNavy
                        )
                        day.completed > 0 -> Text(
                            text = day.completed.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.littly.onNavy
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitsHeader(completed: Int, total: Int, onSeeAll: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SectionLabel(
            text = stringResource(R.string.today_habits),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(LittlySpacing.sm))
        if (total > 0) {
            Text(
                text = "$completed/$total",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        if (total > 0) {
            TextButton(onClick = onSeeAll) {
                Text(
                    text = stringResource(R.string.today_see_all),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun HabitRows(
    habits: List<HabitWithProgress>,
    completedDaysByHabit: Map<Long, Set<LocalDate>>,
    today: LocalDate,
    onToggle: (Long) -> Unit,
    onOpenHabit: (Long) -> Unit
) {
    LittlyCard(
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            habits.forEachIndexed { index, progress ->
                HabitRow(
                    progress = progress,
                    completedDays = completedDaysByHabit[progress.habit.id].orEmpty(),
                    today = today,
                    onToggle = { onToggle(progress.habit.id) },
                    onOpen = { onOpenHabit(progress.habit.id) }
                )
                if (index < habits.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = LittlySpacing.lg),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitRow(
    progress: HabitWithProgress,
    completedDays: Set<LocalDate>,
    today: LocalDate,
    onToggle: () -> Unit,
    onOpen: () -> Unit
) {
    val habit = progress.habit
    val accent = habitAccent(habit.colorIndex).accent
    val dots = activityDots(habit, completedDays, today)
    val dotDescription = stringResource(
        R.string.habit_activity_description,
        habit.name,
        dots.count { it == com.diego.littly.core.designsystem.component.ActivityDotState.DONE },
        dots.count { it != com.diego.littly.core.designsystem.component.ActivityDotState.REST }
    )
    val doneDescription = stringResource(R.string.cd_mark_done_named, habit.name)
    val undoDescription = stringResource(R.string.cd_mark_not_done_named, habit.name)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(horizontal = LittlySpacing.md, vertical = LittlySpacing.md)
    ) {
        HabitIconTile(habit = habit)
        Spacer(Modifier.width(LittlySpacing.md))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (progress.completedToday) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (progress.completedToday) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (progress.currentStreak > 0) {
                    Spacer(Modifier.width(LittlySpacing.xs))
                    StreakBadge(count = progress.currentStreak, compact = true)
                }
            }
            Spacer(Modifier.height(6.dp))
            ActivityDots(
                states = dots,
                accent = accent,
                description = dotDescription
            )
        }
        Spacer(Modifier.width(LittlySpacing.sm))
        CompletionControl(
            completed = progress.completedToday,
            accent = accent,
            onToggle = onToggle,
            contentDescription = if (progress.completedToday) undoDescription else doneDescription
        )
    }
}

@Composable
private fun AllDoneBanner() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.littly.successSoft)
            .padding(horizontal = LittlySpacing.md, vertical = LittlySpacing.sm)
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = MaterialTheme.littly.success,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(LittlySpacing.xs))
        Column {
            Text(
                text = stringResource(R.string.today_done_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.today_done_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RestDayContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = LittlySpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.LocalFlorist,
            contentDescription = null,
            tint = MaterialTheme.littly.success,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(LittlySpacing.sm))
        Text(
            text = stringResource(R.string.today_rest_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(LittlySpacing.xxs))
        Text(
            text = stringResource(R.string.today_rest_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IdeaCard(
    idea: TodayIdea,
    onOpenHabit: () -> Unit,
    onAnotherIdea: (Long) -> Unit
) {
    LittlyCard(
        color = MaterialTheme.littly.ideaBackground,
        borderColor = MaterialTheme.littly.ideaBorder,
        contentPadding = PaddingValues(LittlySpacing.lg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.littly.ideaAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFFFCF8),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(LittlySpacing.md))
                Column(Modifier.weight(1f)) {
                    SectionLabel(
                        text = stringResource(R.string.ideas_label),
                        color = MaterialTheme.littly.ideaLabel
                    )
                    Text(
                        text = stringResource(R.string.today_idea_for, idea.habitName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                SourcePill(model = idea.model)
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
            Spacer(Modifier.height(LittlySpacing.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(LittlySpacing.sm)) {
                LittlyPrimaryButton(
                    text = stringResource(R.string.today_idea_open),
                    onClick = onOpenHabit,
                    modifier = Modifier.weight(1f)
                )
                LittlySecondaryButton(
                    text = stringResource(R.string.today_idea_more),
                    onClick = { onAnotherIdea(idea.habitId) },
                    icon = Icons.Rounded.Refresh,
                    modifier = Modifier.weight(1f)
                )
            }
            if (idea.earlierText != null) {
                Spacer(Modifier.height(LittlySpacing.md))
                HorizontalDivider(color = MaterialTheme.littly.ideaBorder)
                Spacer(Modifier.height(LittlySpacing.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.today_earlier_idea, idea.earlierText),
                        style = MaterialTheme.typography.bodySmall,
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
private fun SourcePill(model: String) {
    val onDevice = model == ModelLocator.MODEL_ID
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .border(1.dp, MaterialTheme.littly.ideaBorder, CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = MaterialTheme.littly.success,
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(
                if (onDevice) R.string.idea_source_on_device else R.string.idea_source_builtin
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TodayError(onRetry: () -> Unit) {
    EmptyState(
        icon = Icons.Rounded.Refresh,
        title = stringResource(R.string.today_error_title),
        body = stringResource(R.string.today_error_body),
        modifier = Modifier.fillMaxSize(),
        action = {
            LittlyPrimaryButton(
                text = stringResource(R.string.retry),
                onClick = onRetry
            )
        }
    )
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 390, heightDp = 600)
@Composable
private fun TodayIdeaCardPreview() {
    com.diego.littly.core.designsystem.theme.LittlyTheme(
        themeMode = com.diego.littly.core.domain.model.ThemeMode.LIGHT
    ) {
        Box(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {
            IdeaCard(
                idea = TodayIdea(
                    habitId = 1,
                    habitName = "Sketch for 15 min",
                    title = "Draw a teapot from memory",
                    text = "Then compare it with the real thing. Keep it loose — ten minutes is plenty.",
                    model = "curated",
                    earlierText = "A cat napping on a windowsill"
                ),
                onOpenHabit = {},
                onAnotherIdea = {}
            )
        }
    }
}
