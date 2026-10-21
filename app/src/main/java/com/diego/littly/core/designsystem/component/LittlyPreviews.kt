package com.diego.littly.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diego.littly.core.designsystem.theme.HabitMustard
import com.diego.littly.core.designsystem.theme.HabitLavender
import com.diego.littly.core.designsystem.theme.HabitPeach
import com.diego.littly.core.designsystem.theme.HabitPink
import com.diego.littly.core.designsystem.theme.HabitSage
import com.diego.littly.core.designsystem.theme.HabitTeal
import com.diego.littly.core.designsystem.theme.LittlyTheme
import com.diego.littly.core.domain.model.ThemeMode
import com.diego.littly.core.designsystem.theme.littly
import com.diego.littly.core.domain.model.Habit
import java.time.DayOfWeek
import java.time.LocalDate

@Preview(showBackground = true, widthDp = 390, heightDp = 520)
@Composable
private fun LittlyComponentsPreview() {
    LittlyTheme(themeMode = ThemeMode.LIGHT) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LittlyAvatar(name = "Alex")
                LittlyAvatar(name = "")
                CompletionControl(
                    completed = false,
                    accent = HabitPink.accent,
                    onToggle = {},
                    contentDescription = "Mark done"
                )
                CompletionControl(
                    completed = true,
                    accent = HabitPink.accent,
                    onToggle = {},
                    contentDescription = "Mark not done"
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HabitIconTile(habit = previewHabit(), size = 48.dp, solid = true)
                HabitIconTile(habit = previewHabit(colorIndex = 1), size = 48.dp)
                StreakBadge(count = 47)
            }
            LittlyCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(
                        completed = 2,
                        total = 4,
                        trackColor = MaterialTheme.littly.onNavy.copy(alpha = 0.2f),
                        progressColor = MaterialTheme.colorScheme.primary
                    )
                    Column(Modifier.padding(start = 16.dp)) {
                        SectionLabel(text = "Current streak")
                        Text(
                            text = "47 days",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LittlyPill(text = "All day", selected = true, onClick = {})
                LittlyPill(text = "Morning", selected = false, onClick = {})
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LittlyPrimaryButton(text = "Mark done", onClick = {}, icon = Icons.Rounded.Check)
                LittlySecondaryButton(text = "Another idea", onClick = {})
            }
            ActivityDots(
                states = listOf(
                    ActivityDotState.DONE,
                    ActivityDotState.DONE,
                    ActivityDotState.GOAL,
                    ActivityDotState.REST,
                    ActivityDotState.DONE
                ),
                accent = HabitPink.accent
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(HabitPink, HabitTeal, HabitSage, HabitPeach, HabitLavender, HabitMustard)
                    .forEach { accent ->
                        androidx.compose.foundation.layout.Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(accent.accent)
                        )
                    }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 520)
@Composable
private fun LittlyComponentsDarkPreview() {
    LittlyTheme(themeMode = ThemeMode.DARK) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LittlyAvatar(name = "Alex")
                HabitIconTile(habit = previewHabit(), size = 48.dp, solid = true)
                StreakBadge(count = 12)
            }
            LittlyCard {
                Text(
                    text = "A warm, calm dark theme",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LittlyPill(text = "All day", selected = true, onClick = {})
                LittlyPill(text = "Evening", selected = false, onClick = {})
            }
        }
    }
}

private fun previewHabit(colorIndex: Int = 0) = Habit(
    id = 1,
    name = "Sketch for 15 min",
    iconKey = "brush",
    createdAt = LocalDate.of(2026, 1, 1),
    goalDays = DayOfWeek.entries.toSet(),
    colorIndex = colorIndex
)
