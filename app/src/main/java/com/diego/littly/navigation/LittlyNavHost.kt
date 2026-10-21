package com.diego.littly.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.diego.littly.feature.stats.StatsScreen
import com.diego.littly.feature.today.TodayScreen

object LittlyRoutes {
    const val ONBOARDING = "onboarding"
    const val TODAY = "today"
    const val HABITS = "habits"
    const val STATS = "stats"
    const val PROFILE = "profile"
    const val HABIT_DETAIL = "habits/{habitId}?generateIdea={generateIdea}"
    const val HABIT_EDIT = "habits/edit?habitId={habitId}"

    fun habitDetail(habitId: Long, generateIdea: Boolean = false): String =
        "habits/$habitId?generateIdea=$generateIdea"

    fun habitEdit(habitId: Long? = null): String =
        if (habitId != null) "habits/edit?habitId=$habitId" else "habits/edit"
}

private data class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
)

private val topLevelDestinations = listOf(
    TopLevelDestination(LittlyRoutes.TODAY, R.string.nav_today, Icons.Rounded.Home),
    TopLevelDestination(LittlyRoutes.HABITS, R.string.nav_habits, Icons.AutoMirrored.Rounded.FormatListBulleted),
    TopLevelDestination(LittlyRoutes.STATS, R.string.nav_stats, Icons.Rounded.BarChart),
    TopLevelDestination(LittlyRoutes.PROFILE, R.string.nav_profile, Icons.Rounded.Person)
)

@Composable
fun LittlyAppRoot(
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = hiltViewModel(),
    deepLinkState: DeepLinkState = hiltViewModel<DeepLinkViewModel>().deepLinkState
) {
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
    val deepLink by deepLinkState.pending.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(deepLink, startDestination) {
        if (startDestination == null) return@LaunchedEffect
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

    val isTopLevel = topLevelDestinations.any { it.route == currentRoute }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevel) {
                LittlyBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(LittlyRoutes.TODAY) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination ?: LittlyRoutes.TODAY,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(LittlyRoutes.ONBOARDING) {
                OnboardingScreen(
                    onDone = {
                        navController.navigate(LittlyRoutes.TODAY) {
                            popUpTo(LittlyRoutes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
            composable(LittlyRoutes.TODAY) {
                TodayScreen(
                    onAddHabit = { navController.navigate(LittlyRoutes.habitEdit()) },
                    onOpenHabit = { habitId ->
                        navController.navigate(LittlyRoutes.habitDetail(habitId))
                    },
                    onOpenHabitWithIdea = { habitId ->
                        navController.navigate(LittlyRoutes.habitDetail(habitId, generateIdea = true))
                    },
                    onSeeAllHabits = {
                        navController.navigate(LittlyRoutes.HABITS) {
                            popUpTo(LittlyRoutes.TODAY) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenProfile = { navController.navigateTopLevel(LittlyRoutes.PROFILE) },
                    onOpenReminders = { navController.navigateTopLevel(LittlyRoutes.PROFILE) }
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
            composable(LittlyRoutes.STATS) {
                StatsScreen(
                    onOpenHabit = { habitId ->
                        navController.navigate(LittlyRoutes.habitDetail(habitId))
                    },
                    onAddHabit = { navController.navigate(LittlyRoutes.habitEdit()) }
                )
            }
            composable(LittlyRoutes.PROFILE) {
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
                    onBack = { navController.popBackStack() },
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

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(LittlyRoutes.TODAY) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun LittlyBottomBar(
    currentRoute: String?,
    onNavigate: (TopLevelDestination) -> Unit
) {
    Column {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            topLevelDestinations.forEach { destination ->
                val selected = currentRoute == destination.route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(destination) },
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = null,
                            modifier = Modifier.size(23.dp)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(destination.labelRes),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
