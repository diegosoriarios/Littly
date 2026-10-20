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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diego.littly.R
import com.diego.littly.core.download.ModelDownloadState
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 4 })
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onNotificationPermissionResult(granted)
        scope.launch { pagerState.animateScrollToPage(2) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> RemindersPage(
                        onAllowNotifications = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(
                                    android.Manifest.permission.POST_NOTIFICATIONS
                                )
                            } else {
                                scope.launch { pagerState.animateScrollToPage(2) }
                            }
                        }
                    )
                    2 -> BatteryPage(
                        onOptimize = {
                            requestIgnoreBatteryOptimizations(context)
                        }
                    )
                    else -> ModelDownloadPage(
                        downloadState = downloadState,
                        onDownload = { viewModel.startModelDownload() },
                        onDone = {
                            viewModel.complete()
                            onDone()
                        }
                    )
                }
            }
        }
        DotsIndicator(
            pageCount = 4,
            currentPage = pagerState.currentPage,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            if (pagerState.currentPage < 3) {
                TextButton(onClick = {
                    scope.launch { pagerState.scrollToPage(3) }
                }) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            }
            Spacer(Modifier.weight(1f))
            if (pagerState.currentPage < 3) {
                Button(
                    onClick = {
                        val page = pagerState.currentPage
                        scope.launch {
                            if (page < 3) {
                                pagerState.animateScrollToPage(page + 1)
                            }
                        }
                    },
                    modifier = Modifier.height(52.dp)
                ) {
                    Text(stringResource(R.string.onboarding_next))
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    PageScaffold(
        icon = { Icon(Icons.Outlined.LocalFlorist, null, Modifier.size(32.dp)) },
        title = stringResource(R.string.onboarding_welcome_title),
        body = stringResource(R.string.onboarding_welcome_body)
    )
}

@Composable
private fun RemindersPage(onAllowNotifications: () -> Unit) {
    PageScaffold(
        icon = { Icon(Icons.Outlined.NotificationsActive, null, Modifier.size(32.dp)) },
        title = stringResource(R.string.onboarding_reminders_title),
        body = stringResource(R.string.onboarding_reminders_body)
    ) {
        Button(
            onClick = onAllowNotifications,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(stringResource(R.string.onboarding_allow_notifications))
        }
    }
}

@Composable
private fun BatteryPage(onOptimize: () -> Unit) {
    PageScaffold(
        icon = { Icon(Icons.Outlined.BatteryChargingFull, null, Modifier.size(32.dp)) },
        title = stringResource(R.string.onboarding_battery_title),
        body = stringResource(R.string.onboarding_battery_body)
    ) {
        OutlinedButton(
            onClick = onOptimize,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(stringResource(R.string.onboarding_battery_action))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_battery_oem_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ModelDownloadPage(
    downloadState: ModelDownloadState,
    onDownload: () -> Unit,
    onDone: () -> Unit
) {
    PageScaffold(
        icon = { Icon(Icons.Outlined.Download, null, Modifier.size(32.dp)) },
        title = stringResource(R.string.onboarding_model_title),
        body = stringResource(R.string.onboarding_model_body)
    ) {
        when (downloadState) {
            is ModelDownloadState.Idle -> {
                Button(
                    onClick = onDownload,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(stringResource(R.string.onboarding_model_download))
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
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.onboarding_model_downloading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is ModelDownloadState.Completed -> {
                Text(
                    text = stringResource(R.string.onboarding_model_complete),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(stringResource(R.string.onboarding_done))
                }
            }
            is ModelDownloadState.Failed -> {
                Text(
                    text = stringResource(R.string.onboarding_model_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onDownload,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Retry")
                }
            }
        }
        
        if (downloadState !is ModelDownloadState.Downloading) {
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onDone) {
                Text(stringResource(R.string.onboarding_model_skip))
            }
        }
    }
}

@Composable
private fun PageScaffold(
    icon: @Composable () -> Unit,
    title: String,
    body: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                icon()
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, content = content)
    }
}

@Composable
private fun DotsIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            Box(
                modifier = Modifier
                    .size(if (selected) 10.dp else 8.dp)
                    .background(
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape
                    )
            )
        }
    }
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
