package com.diego.littly.feature.habits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diego.littly.R
import com.diego.littly.core.designsystem.component.LittlyHabitPalette
import com.diego.littly.core.designsystem.component.LittlyTimePickerDialog
import com.diego.littly.core.designsystem.component.WeekdayPickerRow
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

@Composable
fun HabitEditScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HabitEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.consumeSaved()
            onDone()
        }
    }

    if (state.isLoading) return

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = { Text(stringResource(R.string.label_habit_name)) },
            placeholder = { Text(stringResource(R.string.hint_habit_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        SectionHeader(stringResource(R.string.section_goal_days))
        WeekdayPickerRow(
            selected = state.goalDays,
            onSelectionChange = viewModel::onGoalDaysChange,
            modifier = Modifier.fillMaxWidth()
        )
        ReminderSection(
            enabled = state.reminderEnabled,
            minuteOfDay = state.reminderMinuteOfDay,
            onEnabledChange = viewModel::onReminderEnabledChange,
            onTimeChange = viewModel::onReminderTimeChange
        )
        SectionHeader(stringResource(R.string.section_color))
        ColorPickerRow(
            selected = state.colorIndex,
            onSelect = viewModel::onColorChange
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = viewModel::save,
            enabled = state.canSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(stringResource(R.string.save_habit))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionHeader(text: String) {
    Spacer(Modifier.height(24.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
    )
}

@Composable
private fun ReminderSection(
    enabled: Boolean,
    minuteOfDay: Int?,
    onEnabledChange: (Boolean) -> Unit,
    onTimeChange: (Int) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    SectionHeader(stringResource(R.string.section_reminder))
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.reminder_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (enabled) {
                            stringResource(R.string.reminder_on_goal_days)
                        } else {
                            stringResource(R.string.reminder_off)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = enabled, onCheckedChange = onEnabledChange)
            }
            if (enabled) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = formatMinuteOfDay(minuteOfDay),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        LittlyTimePickerDialog(
            initialMinuteOfDay = minuteOfDay,
            onConfirm = { time ->
                onTimeChange(time)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
private fun ColorPickerRow(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LittlyHabitPalette.forEachIndexed { index, color ->
            val isSelected = index == selected
            val label = stringResource(R.string.cd_color_option, index + 1)
            Surface(
                shape = CircleShape,
                color = color,
                border = if (isSelected) {
                    BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface)
                } else {
                    null
                },
                modifier = Modifier
                    .size(40.dp)
                    .semantics { contentDescription = label }
                    .clickable { onSelect(index) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun formatMinuteOfDay(minuteOfDay: Int?): String {
    val minutes = minuteOfDay ?: 9 * 60
    return LocalTime.of(minutes / 60, minutes % 60).format(
        timeFormatter.withLocale(Locale.getDefault())
    )
}
