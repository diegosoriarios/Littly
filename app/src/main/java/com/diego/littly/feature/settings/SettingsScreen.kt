package com.diego.littly.feature.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diego.littly.BuildConfig
import com.diego.littly.R
import com.diego.littly.core.designsystem.component.LittlyAvatar
import com.diego.littly.core.designsystem.component.LittlyCard
import com.diego.littly.core.designsystem.component.LittlyPill
import com.diego.littly.core.designsystem.component.LittlyPrimaryButton
import com.diego.littly.core.designsystem.component.SectionLabel
import com.diego.littly.core.designsystem.theme.LittlySpacing
import com.diego.littly.core.designsystem.theme.littly
import com.diego.littly.core.domain.model.ThemeMode
import com.diego.littly.core.download.ModelDownloadState

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var nameInput by remember(state.displayName) { mutableStateOf(state.displayName) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshPermissions()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { viewModel.refreshPermissions() }

    when {
        state.isLoading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = LittlySpacing.gutter,
                end = LittlySpacing.gutter,
                top = LittlySpacing.md,
                bottom = LittlySpacing.xxl
            ),
            verticalArrangement = Arrangement.spacedBy(LittlySpacing.md)
        ) {
            item(key = "header") {
                ProfileHeader(name = state.displayName)
            }
            item(key = "name") {
                LittlyCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it.take(40) },
                            label = { Text(stringResource(R.string.settings_name)) },
                            supportingText = {
                                Text(stringResource(R.string.settings_name_hint))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            trailingIcon = {
                                if (nameInput.trim() != state.displayName) {
                                    TextButton(onClick = {
                                        viewModel.setDisplayName(nameInput)
                                    }) {
                                        Text(stringResource(R.string.settings_name_save))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            item(key = "appearance_header") {
                SettingsHeader(stringResource(R.string.settings_section_appearance))
            }
            item(key = "appearance") {
                LittlyCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsIcon(Icons.Outlined.Palette)
                            Spacer(Modifier.width(LittlySpacing.md))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_theme),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.settings_theme_subtitle),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(LittlySpacing.md))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(LittlySpacing.xs),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            ThemePill(
                                label = stringResource(R.string.theme_system),
                                mode = ThemeMode.SYSTEM,
                                current = state.themeMode,
                                onClick = viewModel::setThemeMode
                            )
                            ThemePill(
                                label = stringResource(R.string.theme_light),
                                mode = ThemeMode.LIGHT,
                                current = state.themeMode,
                                onClick = viewModel::setThemeMode
                            )
                            ThemePill(
                                label = stringResource(R.string.theme_dark),
                                mode = ThemeMode.DARK,
                                current = state.themeMode,
                                onClick = viewModel::setThemeMode
                            )
                        }
                    }
                }
            }
            item(key = "reminders_header") {
                SettingsHeader(stringResource(R.string.settings_section_reminders))
            }
            item(key = "reminders") {
                LittlyCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ToggleSetting(
                            icon = Icons.Outlined.Alarm,
                            title = stringResource(R.string.settings_reminders_title),
                            subtitle = stringResource(R.string.settings_reminders_subtitle),
                            checked = state.remindersMasterEnabled,
                            onCheckedChange = viewModel::setRemindersMasterEnabled
                        )
                        SettingsDivider()
                        ActionSetting(
                            icon = Icons.Outlined.Notifications,
                            title = stringResource(R.string.settings_notifications_title),
                            subtitle = stringResource(
                                if (state.permissions.notificationsGranted) {
                                    R.string.settings_notifications_allowed
                                } else {
                                    R.string.settings_notifications_blocked
                                }
                            ),
                            actionLabel = if (state.permissions.notificationsGranted) {
                                null
                            } else {
                                stringResource(R.string.settings_allow)
                            },
                            onAction = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    openAppNotificationSettings(context)
                                }
                            }
                        )
                        SettingsDivider()
                        ActionSetting(
                            icon = Icons.Outlined.Alarm,
                            title = stringResource(R.string.settings_exact_title),
                            subtitle = stringResource(
                                if (state.permissions.exactAlarmsAllowed) {
                                    R.string.settings_exact_allowed
                                } else {
                                    R.string.settings_exact_blocked
                                }
                            ),
                            actionLabel = if (state.permissions.exactAlarmsAllowed) {
                                null
                            } else {
                                stringResource(R.string.settings_open_settings)
                            },
                            onAction = { openExactAlarmSettings(context) }
                        )
                        SettingsDivider()
                        ActionSetting(
                            icon = Icons.Outlined.BatterySaver,
                            title = stringResource(R.string.settings_battery_title),
                            subtitle = stringResource(
                                if (state.permissions.batteryExempt) {
                                    R.string.settings_battery_allowed
                                } else {
                                    R.string.settings_battery_blocked
                                }
                            ),
                            actionLabel = if (state.permissions.batteryExempt) {
                                null
                            } else {
                                stringResource(R.string.settings_allow)
                            },
                            onAction = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    runCatching {
                                        context.startActivity(
                                            Intent(
                                                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                                Uri.parse("package:${context.packageName}")
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
            item(key = "ideas_header") {
                SettingsHeader(stringResource(R.string.settings_section_idea_engine))
            }
            item(key = "ideas") {
                LittlyCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ToggleSetting(
                            icon = Icons.Outlined.AutoAwesome,
                            title = stringResource(R.string.settings_idea_engine_title),
                            subtitle = stringResource(R.string.settings_idea_engine_subtitle),
                            checked = state.ideaEngineEnabled,
                            onCheckedChange = viewModel::setIdeaEngineEnabled
                        )
                        SettingsDivider()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = LittlySpacing.xs)
                        ) {
                            SettingsIcon(
                                if (state.modelPresent) Icons.Rounded.Check else Icons.Outlined.Download
                            )
                            Spacer(Modifier.width(LittlySpacing.md))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_model_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(
                                        if (state.modelPresent) {
                                            R.string.settings_model_status_ready
                                        } else {
                                            R.string.settings_model_status_missing
                                        }
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        ModelManagement(
                            state = state.modelDownloadState,
                            modelPresent = state.modelPresent,
                            modelSizeBytes = state.modelSizeBytes,
                            onDownload = viewModel::downloadModel,
                            onCancel = viewModel::cancelDownload,
                            onDelete = viewModel::deleteModel
                        )
                    }
                }
            }
            item(key = "privacy_header") {
                SettingsHeader(stringResource(R.string.settings_section_privacy))
            }
            item(key = "privacy") {
                LittlyCard(
                    color = MaterialTheme.littly.successSoft,
                    borderColor = MaterialTheme.littly.successSoft,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        SettingsIcon(
                            icon = Icons.Outlined.Lock,
                            container = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                            tint = MaterialTheme.littly.success
                        )
                        Spacer(Modifier.width(LittlySpacing.md))
                        Column {
                            Text(
                                text = stringResource(R.string.settings_privacy_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.settings_privacy_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item(key = "about") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = LittlySpacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(LittlySpacing.xs))
                    Text(
                        text = stringResource(R.string.settings_about_version, BuildConfig.VERSION_NAME),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(name: String) {
    Column {
        SectionLabel(
            text = stringResource(R.string.settings_eyebrow),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(LittlySpacing.xs))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LittlyAvatar(name = name, size = 64.dp)
            Spacer(Modifier.width(LittlySpacing.md))
            Column {
                Text(
                    text = stringResource(R.string.settings_profile_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsHeader(text: String) {
    SectionLabel(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = LittlySpacing.xs)
    )
}

@Composable
private fun ThemePill(
    label: String,
    mode: ThemeMode,
    current: ThemeMode,
    onClick: (ThemeMode) -> Unit
) {
    LittlyPill(
        text = label,
        selected = mode == current,
        onClick = { onClick(mode) }
    )
}

@Composable
private fun SettingsIcon(
    icon: ImageVector,
    container: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(container, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun ToggleSetting(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = LittlySpacing.xs)
    ) {
        SettingsIcon(icon)
        Spacer(Modifier.width(LittlySpacing.md))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(LittlySpacing.xs))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ActionSetting(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = LittlySpacing.xs)
    ) {
        SettingsIcon(icon)
        Spacer(Modifier.width(LittlySpacing.md))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (actionLabel != null) {
            Spacer(Modifier.width(LittlySpacing.xs))
            TextButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun ModelManagement(
    state: ModelDownloadState,
    modelPresent: Boolean,
    modelSizeBytes: Long,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    when (state) {
        is ModelDownloadState.Downloading -> {
            val progress = if (state.totalBytes != null && state.totalBytes > 0) {
                state.bytesDownloaded.toFloat() / state.totalBytes.toFloat()
            } else {
                null
            }
            Spacer(Modifier.height(LittlySpacing.sm))
            if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(LittlySpacing.sm))
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.settings_model_cancel))
            }
        }
        is ModelDownloadState.Failed -> {
            Spacer(Modifier.height(LittlySpacing.sm))
            Text(
                text = state.error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(LittlySpacing.xs))
            Row(horizontalArrangement = Arrangement.spacedBy(LittlySpacing.xs)) {
                LittlyPrimaryButton(
                    text = stringResource(R.string.retry),
                    onClick = onDownload,
                    icon = Icons.Rounded.Refresh
                )
            }
        }
        else -> {
            if (modelPresent) {
                Spacer(Modifier.height(LittlySpacing.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(
                            R.string.settings_model_downloaded,
                            (modelSizeBytes / (1024 * 1024)).toInt()
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.settings_model_delete))
                    }
                }
            } else {
                Spacer(Modifier.height(LittlySpacing.sm))
                LittlyPrimaryButton(
                    text = stringResource(R.string.settings_model_download),
                    onClick = onDownload,
                    icon = Icons.Outlined.Download
                )
            }
        }
    }
    Spacer(Modifier.height(LittlySpacing.xs))
    Text(
        text = stringResource(R.string.settings_model_explanation),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

fun openExactAlarmSettings(context: android.content.Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    runCatching {
        context.startActivity(
            Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:${context.packageName}")
            )
        )
    }
}

fun openAppNotificationSettings(context: android.content.Context) {
    runCatching {
        context.startActivity(
            Intent(
                Settings.ACTION_APP_NOTIFICATION_SETTINGS,
                Uri.parse("package:${context.packageName}")
            )
        )
    }
}
