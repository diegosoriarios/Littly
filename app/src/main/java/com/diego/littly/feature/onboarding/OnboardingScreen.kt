package com.diego.littly.feature.onboarding

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diego.littly.R
import com.diego.littly.core.designsystem.component.LittlyAvatar
import com.diego.littly.core.designsystem.component.LittlyCard
import com.diego.littly.core.designsystem.component.LittlyPrimaryButton
import com.diego.littly.core.designsystem.component.LittlySecondaryButton
import com.diego.littly.core.designsystem.theme.LittlySpacing
import com.diego.littly.core.designsystem.theme.littly
import com.diego.littly.core.download.ModelDownloadState
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 4

@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()

    val advance = {
        scope.launch {
            if (pagerState.currentPage < PAGE_COUNT - 1) {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
        Unit
    }
    val finish = {
        viewModel.complete(onDone)
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onNotificationPermissionResult(granted)
        if (pagerState.currentPage < PAGE_COUNT - 1) {
            scope.launch { pagerState.animateScrollToPage(2) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = LittlySpacing.xl)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = LittlySpacing.sm)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = finish) {
                Text(
                    text = stringResource(R.string.onboarding_skip_setup),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Spacer(Modifier.height(LittlySpacing.md))
        SegmentedProgress(currentPage = pagerState.currentPage)
        Spacer(Modifier.height(LittlySpacing.sm))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> NamePage(
                    name = name,
                    onNameChange = viewModel::onNameChange,
                    onContinue = advance
                )
                1 -> RemindersPage(
                    onAllow = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(
                                android.Manifest.permission.POST_NOTIFICATIONS
                            )
                        } else {
                            viewModel.onNotificationPermissionResult(true)
                            advance()
                        }
                    },
                    onNotNow = advance
                )
                2 -> BackgroundPage(
                    onContinue = advance,
                    onAllowBackground = { requestIgnoreBatteryOptimizations(context) }
                )
                else -> IdeasPage(
                    downloadState = downloadState,
                    onDownload = viewModel::startModelDownload,
                    onCancel = viewModel::cancelDownload,
                    onRetry = viewModel::retryDownload,
                    onFinish = finish
                )
            }
        }
    }
}

@Composable
private fun SegmentedProgress(currentPage: Int) {
    val progressDescription = stringResource(R.string.onboarding_progress, currentPage + 1, PAGE_COUNT)
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = progressDescription
            }
    ) {
        repeat(PAGE_COUNT) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (index <= currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                        }
                    )
            )
        }
    }
}

@Composable
private fun PageScaffold(
    icon: ImageVector? = null,
    avatarName: String? = null,
    title: String,
    body: String,
    content: @Composable () -> Unit = {},
    actions: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = LittlySpacing.lg, bottom = LittlySpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (avatarName != null) {
            LittlyAvatar(name = avatarName, size = 76.dp)
        } else if (icon != null) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        Spacer(Modifier.height(LittlySpacing.xl))
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(LittlySpacing.sm))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(LittlySpacing.xl))
        content()
        Spacer(Modifier.height(LittlySpacing.xxl))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            actions()
        }
    }
}

@Composable
private fun NamePage(
    name: String,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    PageScaffold(
        avatarName = name,
        title = stringResource(R.string.onboarding_name_title),
        body = stringResource(R.string.onboarding_name_body),
        content = {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.onboarding_name_label)) },
                placeholder = { Text(stringResource(R.string.onboarding_name_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )
        },
        actions = {
            LittlyPrimaryButton(
                text = stringResource(R.string.onboarding_continue),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
private fun RemindersPage(onAllow: () -> Unit, onNotNow: () -> Unit) {
    PageScaffold(
        icon = Icons.Outlined.NotificationsActive,
        title = stringResource(R.string.onboarding_reminders_title),
        body = stringResource(R.string.onboarding_reminders_body),
        actions = {
            LittlyPrimaryButton(
                text = stringResource(R.string.onboarding_allow_notifications),
                onClick = onAllow,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(LittlySpacing.xs))
            TextButton(onClick = onNotNow) {
                Text(stringResource(R.string.onboarding_not_now))
            }
        }
    )
}

@Composable
private fun BackgroundPage(onContinue: () -> Unit, onAllowBackground: () -> Unit) {
    PageScaffold(
        icon = Icons.Outlined.BatteryChargingFull,
        title = stringResource(R.string.onboarding_battery_title),
        body = stringResource(R.string.onboarding_battery_body),
        content = {
            Text(
                text = stringResource(R.string.onboarding_battery_oem_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        },
        actions = {
            LittlyPrimaryButton(
                text = stringResource(R.string.onboarding_continue),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(LittlySpacing.xs))
            LittlySecondaryButton(
                text = stringResource(R.string.onboarding_battery_action),
                onClick = onAllowBackground,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
private fun IdeasPage(
    downloadState: ModelDownloadState,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onFinish: () -> Unit
) {
    PageScaffold(
        icon = Icons.Rounded.AutoAwesome,
        title = stringResource(R.string.onboarding_model_title),
        body = stringResource(R.string.onboarding_model_body),
        content = {
            Text(
                text = stringResource(R.string.onboarding_ideas_optional),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(LittlySpacing.sm))
            LittlyCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    when (downloadState) {
                        is ModelDownloadState.Idle -> {
                            LittlySecondaryButton(
                                text = stringResource(R.string.onboarding_model_download),
                                onClick = onDownload,
                                icon = Icons.Outlined.Download,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is ModelDownloadState.WaitingForNetwork -> {
                            Text(
                                text = stringResource(R.string.onboarding_model_waiting),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.onboarding_model_waiting_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(LittlySpacing.sm))
                            TextButton(onClick = onRetry) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                        is ModelDownloadState.Downloading -> {
                            val progress = if (downloadState.totalBytes != null && downloadState.totalBytes > 0) {
                                downloadState.bytesDownloaded.toFloat() / downloadState.totalBytes.toFloat()
                            } else {
                                null
                            }
                            if (progress != null) {
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.onboarding_model_downloading),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(LittlySpacing.xs))
                            TextButton(onClick = onCancel) {
                                Text(stringResource(R.string.onboarding_model_cancel))
                            }
                        }
                        is ModelDownloadState.Completed -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.littly.success
                                )
                                Spacer(Modifier.width(LittlySpacing.xs))
                                Text(
                                    text = stringResource(R.string.onboarding_model_complete),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        is ModelDownloadState.Failed -> {
                            Text(
                                text = stringResource(R.string.onboarding_model_failed),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = downloadState.error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(LittlySpacing.sm))
                            TextButton(onClick = onRetry) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
            }
        },
        actions = {
            LittlyPrimaryButton(
                text = stringResource(R.string.onboarding_done),
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(LittlySpacing.xs))
            TextButton(onClick = onFinish) {
                Text(stringResource(R.string.onboarding_model_skip))
            }
        }
    )
}

private fun requestIgnoreBatteryOptimizations(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
    if (powerManager.isIgnoringBatteryOptimizations(context.packageName)) return
    runCatching {
        context.startActivity(
            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}")
            )
        )
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 390, heightDp = 780)
@Composable
private fun NamePagePreview() {
    com.diego.littly.core.designsystem.theme.LittlyTheme(
        themeMode = com.diego.littly.core.domain.model.ThemeMode.LIGHT
    ) {
        Box(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        ) {
            NamePage(name = "Alex", onNameChange = {}, onContinue = {})
        }
    }
}
