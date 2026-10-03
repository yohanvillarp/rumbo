package tech.nikelyh.rumbo.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.navigation.HomeDestination
import tech.nikelyh.rumbo.core.navigation.OnboardingDestination
import tech.nikelyh.rumbo.core.navigation.ProcessesDestination
import tech.nikelyh.rumbo.core.navigation.ProgressDestination
import tech.nikelyh.rumbo.core.navigation.SettingsDestination
import tech.nikelyh.rumbo.core.navigation.TasksDestination
import tech.nikelyh.rumbo.feature.home.navigation.homeScreen
import tech.nikelyh.rumbo.feature.home.navigation.navigateToHome
import tech.nikelyh.rumbo.feature.onboarding.navigation.onboardingScreen
import tech.nikelyh.rumbo.feature.processes.navigation.navigateToProcesses
import tech.nikelyh.rumbo.feature.processes.navigation.processesScreen
import tech.nikelyh.rumbo.feature.progress.navigation.navigateToProgress
import tech.nikelyh.rumbo.feature.progress.navigation.progressScreen
import tech.nikelyh.rumbo.feature.settings.navigation.navigateToSettings
import tech.nikelyh.rumbo.feature.settings.navigation.settingsScreen
import tech.nikelyh.rumbo.feature.tasks.navigation.navigateToTasks
import tech.nikelyh.rumbo.feature.tasks.navigation.tasksScreen

enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    HOME(HomeDestination.route, Icons.Default.Home, "Inicio"),
    PROCESSES(ProcessesDestination.route, Icons.Default.ListAlt, "Procesos"),
    TASKS(TasksDestination.route, Icons.Default.Assignment, "Tareas"),
    PROGRESS(ProgressDestination.route, Icons.Default.BarChart, "Progreso"),
    SETTINGS(SettingsDestination.route, Icons.Default.Settings, "Ajustes")
}

@Composable
fun RumboApp(
    navController: NavHostController = rememberNavController()
) {
    RumboTheme {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val shouldShowBottomBar = currentDestination?.route != OnboardingDestination.route

        Scaffold(
            bottomBar = {
                if (shouldShowBottomBar) {
                    RumboBottomBar(
                        destinations = TopLevelDestination.entries,
                        onNavigateToDestination = { destination ->
                            val topLevelNavOptions = navOptions {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            when (destination) {
                                TopLevelDestination.HOME -> navController.navigateToHome(topLevelNavOptions)
                                TopLevelDestination.PROCESSES -> navController.navigateToProcesses(topLevelNavOptions)
                                TopLevelDestination.TASKS -> navController.navigateToTasks(topLevelNavOptions)
                                TopLevelDestination.PROGRESS -> navController.navigateToProgress(topLevelNavOptions)
                                TopLevelDestination.SETTINGS -> navController.navigateToSettings(topLevelNavOptions)
                            }
                        },
                        currentDestination = currentDestination
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = HomeDestination.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                onboardingScreen(
                    onOnboardingFinished = {
                        navController.navigateToHome(
                            navOptions {
                                popUpTo(OnboardingDestination.route) { inclusive = true }
                            }
                        )
                    }
                )
                homeScreen(
                    onNavigateToProcess = { navController.navigateToProcesses() },
                    onNavigateToTask = { navController.navigateToTasks() }
                )
                processesScreen(
                    onProcessClick = { /* Navigate to detail when implemented */ }
                )
                tasksScreen(
                    onTaskClick = { /* Navigate to detail when implemented */ }
                )
                progressScreen()
                settingsScreen()
            }
        }
    }
}

@Composable
private fun RumboBottomBar(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?
) {
    NavigationBar {
        destinations.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = { Icon(imageVector = destination.icon, contentDescription = destination.label) },
                label = { Text(text = destination.label) }
            )
        }
    }
}
