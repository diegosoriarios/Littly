package com.diego.littly.feature.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diego.littly.BuildConfig
import com.diego.littly.R

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { viewModel.refreshPermissions() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "header_reminders") {
            SectionHeader(stringResource(R.string.settings_section_reminders))
        }
        item(key = "row_master") {
            ToggleRow(
                icon = Icons.Outlined.Alarm,
                title = stringResource(R.string.settings_reminders_title),
                subtitle = stringResource(R.string.settings_reminders_subtitle),
                checked = state.remindersMasterEnabled,
                onCheckedChange = viewModel::setRemindersMasterEnabled
            )
        }
        item(key = "row_notifications") {
            ActionRow(
                icon = Icons.Outlined.Notifications,
                title = stringResource(R.string.settings_notifications_title),
                subtitle = stringResource(
                    if (state.permissions.notificationsGranted) {
                        R.string.settings_notifications_allowed
                    } else {
                        R.string.settings_notifications_blocked
                    }
                ),
                actionLabel = if (state.permissions.notificationsGranted) null else stringResource(R.string.settings_allow),
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(
                            android.Manifest.permission.POST_NOTIFICATIONS
                        )
                    } else {
                        openAppNotificationSettings(context)
                    }
                }
            )
        }
        item(key = "row_exact") {
            ActionRow(
                icon = Icons.Outlined.Alarm,
                title = stringResource(R.string.settings_exact_title),
                subtitle = stringResource(
                    if (state.permissions.exactAlarmsAllowed) {
                        R.string.settings_exact_allowed
                    } else {
                        R.string.settings_exact_blocked
                    }
                ),
                actionLabel = if (state.permissions.exactAlarmsAllowed) null else stringResource(R.string.settings_open_settings),
                onAction = { openExactAlarmSettings(context) }
            )
        }
        item(key = "row_battery") {
            ActionRow(
                icon = Icons.Outlined.BatterySaver,
                title = stringResource(R.string.settings_battery_title),
                subtitle = stringResource(
                    if (state.permissions.batteryExempt) {
                        R.string.settings_battery_allowed
                    } else {
                        R.string.settings_battery_blocked
                    }
                ),
                actionLabel = if (state.permissions.batteryExempt) null else stringResource(R.string.settings_allow),
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
        item(key = "header_idea") {
            SectionHeader(stringResource(R.string.settings_section_idea_engine))
        }
        item(key = "row_idea") {
            ToggleRow(
                icon = Icons.Outlined.AutoAwesome,
                title = stringResource(R.string.settings_idea_engine_title),
                subtitle = stringResource(R.string.settings_idea_engine_subtitle),
                checked = state.ideaEngineEnabled,
                onCheckedChange = viewModel::setIdeaEngineEnabled
            )
        }
        item(key = "row_model") {
            ModelManagementRow(
                state = state.modelDownloadState,
                modelPresent = state.modelPresent,
                modelSizeBytes = state.modelSizeBytes,
                onDownload = viewModel::downloadModel,
                onCancel = viewModel::cancelDownload,
                onDelete = viewModel::deleteModel
            )
        }
        item(key = "header_about") {
            SectionHeader(stringResource(R.string.settings_section_about))
        }
        item(key = "row_about") {
            ActionRow(
                icon = Icons.Outlined.Info,
                title = stringResource(R.string.settings_about_title),
                subtitle = stringResource(R.string.settings_about_version, BuildConfig.VERSION_NAME),
                actionLabel = null,
                onAction = {}
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun RowContainer(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
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
            trailing()
        }
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    RowContainer(icon, title, subtitle) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    RowContainer(icon, title, subtitle) {
        if (actionLabel != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun ModelManagementRow(
    state: com.diego.littly.core.download.ModelDownloadState,
    modelPresent: Boolean,
    modelSizeBytes: Long,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_model_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (state) {
                            is com.diego.littly.core.download.ModelDownloadState.Downloading -> {
                                val progress = if (state.totalBytes != null && state.totalBytes > 0) {
                                    "${(state.bytesDownloaded * 100 / state.totalBytes).toInt()}%"
                                } else {
                                    "Downloading..."
                                }
                                progress
                            }
                            is com.diego.littly.core.download.ModelDownloadState.Completed -> {
                                val sizeMb = modelSizeBytes / (1024 * 1024)
                                stringResource(R.string.settings_model_downloaded, sizeMb)
                            }
                            is com.diego.littly.core.download.ModelDownloadState.Failed -> {
                                state.error
                            }
                            else -> stringResource(R.string.settings_model_not_downloaded)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            when (state) {
                is com.diego.littly.core.download.ModelDownloadState.Downloading -> {
                    Spacer(Modifier.height(12.dp))
                    if (state.totalBytes != null && state.totalBytes > 0) {
                        LinearProgressIndicator(
                            progress = { state.bytesDownloaded.toFloat() / state.totalBytes.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.settings_model_cancel))
                    }
                }
                is com.diego.littly.core.download.ModelDownloadState.Completed -> {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.settings_model_delete))
                        }
                    }
                }
                else -> {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_model_download))
                    }
                }
            }
        }
    }
}
