package com.diego.littly.feature.habits

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diego.littly.R
import com.diego.littly.core.designsystem.component.EmptyState
import com.diego.littly.core.designsystem.component.HabitIconTile
import com.diego.littly.core.designsystem.component.LittlyCard
import com.diego.littly.core.designsystem.component.LittlyPrimaryButton
import com.diego.littly.core.designsystem.component.LittlyTimePickerDialog
import com.diego.littly.core.designsystem.component.LittlyTopBar
import com.diego.littly.core.designsystem.component.SectionLabel
import com.diego.littly.core.designsystem.component.WeekdayPickerRow
import com.diego.littly.core.designsystem.component.imageVector
import com.diego.littly.core.designsystem.component.readableOn
import com.diego.littly.core.designsystem.theme.LittlyHabitPalette
import com.diego.littly.core.designsystem.theme.LittlySpacing
import com.diego.littly.core.domain.model.HabitIcon
import java.time.DayOfWeek
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
    val context = LocalContext.current
    var notificationsGranted by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsGranted = granted
    }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.consumeSaved()
            onDone()
        }
    }

    Column(modifier.fillMaxSize()) {
        LittlyTopBar(
            title = stringResource(
                if (state.isEdit) R.string.edit_habit_title else R.string.new_habit_title
            ),
            onBack = onDone,
            actions = {
                TextButton(onClick = onDone) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )

        when {
            state.notFound -> EmptyState(
                icon = Icons.Rounded.Refresh,
                title = stringResource(R.string.detail_not_found_title),
                body = stringResource(R.string.detail_not_found_body),
                modifier = Modifier.fillMaxSize(),
                action = {
                    LittlyPrimaryButton(
                        text = stringResource(R.string.cd_back),
                        onClick = onDone
                    )
                }
            )
            state.isLoading -> Unit
            else -> EditForm(
                state = state,
                notificationsGranted = notificationsGranted,
                onNameChange = viewModel::onNameChange,
                onDescriptionChange = viewModel::onDescriptionChange,
                onGoalDaysChange = viewModel::onGoalDaysChange,
                onReminderEnabledChange = viewModel::onReminderEnabledChange,
                onReminderDaysChange = viewModel::onReminderDaysChange,
                onShowTimePicker = { showTimePicker = true },
                onColorChange = viewModel::onColorChange,
                onIconChange = viewModel::onIconChange,
                onRequestPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        runCatching {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APP_NOTIFICATION_SETTINGS,
                                    Uri.parse("package:${context.packageName}")
                                )
                            )
                        }
                    }
                },
                onSave = viewModel::save
            )
        }
    }

    if (showTimePicker) {
        LittlyTimePickerDialog(
            initialMinuteOfDay = state.reminderMinuteOfDay,
            onConfirm = { time ->
                viewModel.onReminderTimeChange(time)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
private fun EditForm(
    state: HabitEditState,
    notificationsGranted: Boolean,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onGoalDaysChange: (Set<DayOfWeek>) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderDaysChange: (Set<DayOfWeek>) -> Unit,
    onShowTimePicker: () -> Unit,
    onColorChange: (Int) -> Unit,
    onIconChange: (String) -> Unit,
    onRequestPermission: () -> Unit,
    onSave: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = LittlySpacing.gutter,
            end = LittlySpacing.gutter,
            top = LittlySpacing.xs,
            bottom = LittlySpacing.xxl
        ),
        verticalArrangement = Arrangement.spacedBy(LittlySpacing.lg)
    ) {
        item(key = "name") {
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.label_habit_name)) },
                placeholder = { Text(stringResource(R.string.hint_habit_name)) },
                singleLine = false,
                maxLines = 3,
                isError = state.nameError,
                supportingText = if (state.nameError) {
                    { Text(stringResource(R.string.validation_name)) }
                } else {
                    null
                },
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item(key = "description") {
            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.label_habit_description)) },
                placeholder = { Text(stringResource(R.string.hint_habit_description)) },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item(key = "goal_days") {
            Column {
                SectionLabel(text = stringResource(R.string.section_goal_days))
                Spacer(Modifier.height(LittlySpacing.xs))
                WeekdayPickerRow(
                    selected = state.goalDays,
                    onSelectionChange = onGoalDaysChange,
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.goalDaysError) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.validation_goal_days),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        item(key = "reminder") {
            ReminderCard(
                state = state,
                notificationsGranted = notificationsGranted,
                onEnabledChange = onReminderEnabledChange,
                onTimeClick = onShowTimePicker,
                onDaysChange = onReminderDaysChange,
                onRequestPermission = onRequestPermission
            )
        }
        item(key = "icon") {
            Column {
                SectionLabel(text = stringResource(R.string.section_icon))
                Spacer(Modifier.height(LittlySpacing.xs))
                IconPicker(
                    selected = state.iconKey,
                    colorIndex = state.colorIndex,
                    onSelect = onIconChange
                )
            }
        }
        item(key = "color") {
            Column {
                SectionLabel(text = stringResource(R.string.section_color))
                Spacer(Modifier.height(LittlySpacing.xs))
                ColorPicker(selected = state.colorIndex, onSelect = onColorChange)
            }
        }
        item(key = "save") {
            LittlyPrimaryButton(
                text = stringResource(R.string.save_habit),
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReminderCard(
    state: HabitEditState,
    notificationsGranted: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onTimeClick: () -> Unit,
    onDaysChange: (Set<DayOfWeek>) -> Unit,
    onRequestPermission: () -> Unit
) {
    SectionLabel(text = stringResource(R.string.section_reminder))
    Spacer(Modifier.height(LittlySpacing.xs))
    LittlyCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.reminder_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (state.reminderEnabled) {
                            stringResource(R.string.reminder_on_goal_days)
                        } else {
                            stringResource(R.string.reminder_off)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = state.reminderEnabled, onCheckedChange = onEnabledChange)
            }
            if (state.reminderEnabled) {
                Spacer(Modifier.height(LittlySpacing.sm))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onTimeClick)
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
                        Spacer(Modifier.width(LittlySpacing.sm))
                        Text(
                            text = formatMinuteOfDay(state.reminderMinuteOfDay),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(LittlySpacing.md))
                SectionLabel(text = stringResource(R.string.reminder_days))
                Spacer(Modifier.height(LittlySpacing.xs))
                WeekdayPickerRow(
                    selected = state.reminderDays,
                    onSelectionChange = onDaysChange,
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.reminderDaysError) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.validation_reminder_days),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (!notificationsGranted) {
                    Spacer(Modifier.height(LittlySpacing.sm))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.reminder_permission_needed),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onRequestPermission) {
                            Text(stringResource(R.string.reminder_permission_action))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconPicker(
    selected: String,
    colorIndex: Int,
    onSelect: (String) -> Unit
) {
    val palette = LittlyHabitPalette[Math.floorMod(colorIndex, LittlyHabitPalette.size)]
    Row(
        horizontalArrangement = Arrangement.spacedBy(LittlySpacing.xs),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        HabitIcon.entries.forEach { icon ->
            val isSelected = icon.key == selected
            val label = stringResource(iconLabelRes(icon))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) palette.accent else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    if (isSelected) 0.dp else 1.dp,
                    if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .size(52.dp)
                    .semantics { contentDescription = label }
                    .clickable { onSelect(icon.key) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon.imageVector,
                        contentDescription = null,
                        tint = if (isSelected) readableOn(palette.accent) else palette.accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorPicker(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(LittlySpacing.xs),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        LittlyHabitPalette.forEachIndexed { index, accent ->
            val isSelected = index == selected
            val label = stringResource(colorLabelRes(index))
            Surface(
                shape = CircleShape,
                color = accent.accent,
                border = if (isSelected) {
                    BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface)
                } else {
                    null
                },
                modifier = Modifier
                    .size(48.dp)
                    .semantics { contentDescription = label }
                    .clickable { onSelect(index) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = readableOn(accent.accent)
                        )
                    }
                }
            }
        }
    }
}

private fun iconLabelRes(icon: HabitIcon): Int = when (icon) {
    HabitIcon.SPARKLES -> R.string.icon_sparkles
    HabitIcon.BRUSH -> R.string.icon_brush
    HabitIcon.BOOK -> R.string.icon_book
    HabitIcon.WALK -> R.string.icon_walk
    HabitIcon.MEDITATE -> R.string.icon_meditate
    HabitIcon.FITNESS -> R.string.icon_fitness
    HabitIcon.MUSIC -> R.string.icon_music
    HabitIcon.LEAF -> R.string.icon_leaf
    HabitIcon.WATER -> R.string.icon_water
    HabitIcon.SLEEP -> R.string.icon_sleep
    HabitIcon.CODE -> R.string.icon_code
    HabitIcon.HEART -> R.string.icon_heart
    HabitIcon.TASK -> R.string.icon_task
    HabitIcon.CAMERA -> R.string.icon_camera
    HabitIcon.PET -> R.string.icon_pet
    HabitIcon.COFFEE -> R.string.icon_coffee
}

private fun colorLabelRes(index: Int): Int = when (index) {
    0 -> R.string.color_pink
    1 -> R.string.color_teal
    2 -> R.string.color_sage
    3 -> R.string.color_peach
    4 -> R.string.color_lavender
    else -> R.string.color_mustard
}

private fun formatMinuteOfDay(minuteOfDay: Int?): String {
    val minutes = minuteOfDay ?: 9 * 60
    return LocalTime.of(minutes / 60, minutes % 60).format(
        timeFormatter.withLocale(Locale.getDefault())
    )
}
