package com.example.ui.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.BreathingModuleType
import com.example.ui.screens.CustomPatternsScreen
import com.example.ui.screens.ExerciseSessionScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PracticeHistoryScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.SettingsScreen
import com.example.viewmodel.PranayamaViewModel

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Explore : AppRoute("explore")
    data object CustomPatterns : AppRoute("custom_patterns")
    data object PracticeHistory : AppRoute("practice_history")
    data object Progress : AppRoute("progress")
    data object Settings : AppRoute("settings")
    data object PracticeSession : AppRoute("practice_session/{moduleType}?patternId={patternId}") {
        fun createRoute(moduleType: BreathingModuleType, patternId: String? = null): String {
            return if (patternId.isNullOrBlank()) {
                "practice_session/${moduleType.name}"
            } else {
                "practice_session/${moduleType.name}?patternId=$patternId"
            }
        }
    }
}

private data class NavTabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val allNavigationItems = listOf(
    NavTabItem(AppRoute.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    NavTabItem(AppRoute.Explore.route, "Explore", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_explore"),
    NavTabItem(AppRoute.CustomPatterns.route, "Custom", Icons.Filled.Tune, Icons.Outlined.Tune, "nav_custom"),
    NavTabItem(AppRoute.PracticeHistory.route, "History", Icons.Filled.History, Icons.Outlined.History, "nav_history"),
    NavTabItem(AppRoute.Progress.route, "Progress", Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_progress"),
    NavTabItem(AppRoute.Settings.route, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
)

private val compactBottomBarItems = listOf(
    allNavigationItems[0], // Home
    allNavigationItems[1], // Explore
    allNavigationItems[2], // Custom
    allNavigationItems[4], // Progress (links directly to History as well)
    allNavigationItems[5]  // Settings
)

@Composable
fun PranayamaAppNav(viewModel: PranayamaViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val isSessionRoute = currentDestination?.route?.startsWith("practice_session/") == true

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val customPatterns by viewModel.customPatterns.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val progressSummary by viewModel.progressSummary.collectAsStateWithLifecycle()
    val recentExercises by viewModel.recentExercises.collectAsStateWithLifecycle()
    val activeExercise by viewModel.activeExercise.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val completedSessionDialog by viewModel.completedSessionDialog.collectAsStateWithLifecycle()

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun launchExercise(moduleType: BreathingModuleType, customPatternId: String? = null) {
        viewModel.loadExercise(moduleType, customPatternId)
        navController.navigate(AppRoute.PracticeSession.createRoute(moduleType, customPatternId))
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 640.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useNavigationRail && !isSessionRoute) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp
                    ) {
                        compactBottomBarItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true ||
                                (item.route == AppRoute.Progress.route &&
                                    currentDestination?.route == AppRoute.PracticeHistory.route)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail && !isSessionRoute) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        allNavigationItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = AppRoute.Home.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(AppRoute.Home.route) {
                        HomeScreen(
                            settings = settings,
                            progressSummary = progressSummary,
                            recentExercises = recentExercises,
                            onStartExercise = { module -> launchExercise(module) },
                            onOpenCustomPatterns = { navigateTopLevel(AppRoute.CustomPatterns.route) },
                            onOpenProgress = { navigateTopLevel(AppRoute.Progress.route) },
                            onOpenHistory = { navController.navigate(AppRoute.PracticeHistory.route) }
                        )
                    }

                    composable(AppRoute.Explore.route) {
                        ExploreScreen(
                            onStartExercise = { module -> launchExercise(module) },
                            onOpenCustomPatterns = { navigateTopLevel(AppRoute.CustomPatterns.route) }
                        )
                    }

                    composable(AppRoute.CustomPatterns.route) {
                        CustomPatternsScreen(
                            patterns = customPatterns,
                            onStartPattern = { pattern ->
                                launchExercise(BreathingModuleType.CUSTOM, pattern.id)
                            },
                            onSavePattern = { pattern -> viewModel.saveCustomPattern(pattern) },
                            onDeletePattern = { id -> viewModel.deleteCustomPattern(id) }
                        )
                    }

                    composable(AppRoute.PracticeHistory.route) {
                        PracticeHistoryScreen(
                            sessions = sessions,
                            onDeleteSession = { id -> viewModel.deleteSession(id) },
                            onStartPractice = { launchExercise(BreathingModuleType.THREE_STAGE) }
                        )
                    }

                    composable(AppRoute.Progress.route) {
                        ProgressScreen(
                            summary = progressSummary,
                            settings = settings,
                            onOpenHistory = { navController.navigate(AppRoute.PracticeHistory.route) }
                        )
                    }

                    composable(AppRoute.Settings.route) {
                        SettingsScreen(
                            settings = settings,
                            onUpdateSettings = { transform -> viewModel.updateSettings(transform) },
                            onSetBeepTone = { tone -> viewModel.setBeepTone(tone) },
                            onPreviewBeep = { viewModel.previewBeep() },
                            onSetTheme = { pref -> viewModel.setThemePreference(pref) },
                            onSetTimerDisplay = { style -> viewModel.setTimerDisplayStyle(style) },
                            onExportJson = { viewModel.exportDataJson() },
                            onResetAllData = { viewModel.resetAllData() }
                        )
                    }

                    composable(
                        route = AppRoute.PracticeSession.route,
                        arguments = listOf(
                            navArgument("moduleType") { type = NavType.StringType },
                            navArgument("patternId") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) {
                        ExerciseSessionScreen(
                            exercise = activeExercise,
                            timerState = timerState,
                            settings = settings,
                            completedSessionDialog = completedSessionDialog,
                            onBack = { navController.popBackStack() },
                            onStart = { viewModel.startTimer() },
                            onPause = { viewModel.pauseTimer() },
                            onResume = { viewModel.resumeTimer() },
                            onReset = { viewModel.resetTimer() },
                            onFinishAndSave = { viewModel.finishAndSaveSession() },
                            onUpdatePatternConfig = { pattern, kConfig ->
                                viewModel.updateActivePatternConfig(pattern, kConfig)
                            },
                            onToggleSound = { viewModel.toggleQuickSound() },
                            onToggleVoice = { viewModel.toggleQuickVoice() },
                            onDismissCompletionDialog = { viewModel.dismissCompletedSessionDialog() }
                        )
                    }
                }
            }
        }
    }
}
