package com.diego.littly.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.diego.littly.R
import com.diego.littly.core.navigation.DeepLink
import com.diego.littly.core.navigation.DeepLinkState
import com.diego.littly.feature.habits.HabitDetailScreen
import com.diego.littly.feature.habits.HabitEditScreen
import com.diego.littly.feature.habits.HabitListScreen
import com.diego.littly.feature.onboarding.OnboardingScreen
import com.diego.littly.feature.settings.SettingsScreen
import javax.inject.Inject

object LittlyRoutes {
    const val ONBOARDING = "onboarding"
    const val HABITS = "habits"
    const val SETTINGS = "settings"
    const val HABIT_DETAIL = "habits/{habitId}?generateIdea={generateIdea}"
    const val HABIT_EDIT = "habits/edit?habitId={habitId}"

    fun habitDetail(habitId: Long, generateIdea: Boolean = false): String =
        "habits/$habitId?generateIdea=$generateIdea"

    fun habitEdit(habitId: Long? = null): String =
        if (habitId != null) "habits/edit?habitId=$habitId" else "habits/edit"
}

@Composable
fun LittlyAppRoot(
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = hiltViewModel(),
    deepLinkState: DeepLinkState = hiltViewModel<DeepLinkViewModel>().deepLinkState
) {
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
    val deepLink by deepLinkState.pending.collectAsStateWithLifecycle()

    LaunchedEffect(deepLink) {
        when (val link = deepLink) {
            is DeepLink.HabitDetail -> {
                navController.navigate(LittlyRoutes.habitDetail(link.habitId, link.generateIdea))
                deepLinkState.consume()
            }
            null -> Unit
        }
    }

    if (startDestination == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val onHabits = currentRoute == LittlyRoutes.HABITS

    Scaffold(
        topBar = {
            LittlyTopBar(
                route = currentRoute,
                editHabitId = backStackEntry?.arguments?.getLong("habitId") ?: -1L,
                onOpenSettings = { navController.navigate(LittlyRoutes.SETTINGS) },
                onBack = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination ?: LittlyRoutes.HABITS,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(LittlyRoutes.ONBOARDING) {
                OnboardingScreen(
                    onDone = {
                        navController.navigate(LittlyRoutes.HABITS) {
                            popUpTo(LittlyRoutes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
            composable(LittlyRoutes.HABITS) {
                HabitListScreen(
                    onAddHabit = { navController.navigate(LittlyRoutes.habitEdit()) },
                    onOpenHabit = { habitId ->
                        navController.navigate(LittlyRoutes.habitDetail(habitId))
                    }
                )
            }
            composable(LittlyRoutes.SETTINGS) {
                SettingsScreen()
            }
            composable(
                route = LittlyRoutes.HABIT_DETAIL,
                arguments = listOf(
                    navArgument("habitId") { type = NavType.LongType },
                    navArgument("generateIdea") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) {
                HabitDetailScreen(
                    onEdit = { habitId ->
                        navController.navigate(LittlyRoutes.habitEdit(habitId))
                    }
                )
            }
            composable(
                route = LittlyRoutes.HABIT_EDIT,
                arguments = listOf(
                    navArgument("habitId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) {
                HabitEditScreen(onDone = { navController.popBackStack() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LittlyTopBar(
    route: String?,
    editHabitId: Long,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
) {
    val title = when (route) {
        LittlyRoutes.HABITS -> stringResource(R.string.app_name)
        LittlyRoutes.SETTINGS -> stringResource(R.string.settings_title)
        LittlyRoutes.HABIT_DETAIL -> stringResource(R.string.habit_detail_title)
        LittlyRoutes.HABIT_EDIT ->
            if (editHabitId > 0) {
                stringResource(R.string.edit_habit_title)
            } else {
                stringResource(R.string.new_habit_title)
            }
        else -> stringResource(R.string.app_name)
    }
    val showBack = route != LittlyRoutes.HABITS

    CenterAlignedTopAppBar(
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back)
                    )
                }
            }
        },
        actions = {
            if (route == LittlyRoutes.HABITS) {
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.cd_settings)
                    )
                }
            }
        }
    )
}
