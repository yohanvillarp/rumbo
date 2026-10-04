package tech.nikelyh.rumbo.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import tech.nikelyh.rumbo.core.designsystem.component.RumboLoadingState
import tech.nikelyh.rumbo.core.designsystem.component.RumboTopBar
import tech.nikelyh.rumbo.core.designsystem.theme.RumboAnimationTokens
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.ActiveSessionState
import tech.nikelyh.rumbo.core.navigation.CreateProcessDestination
import tech.nikelyh.rumbo.core.navigation.CreateTaskDestination
import tech.nikelyh.rumbo.core.navigation.EditProcessDestination
import tech.nikelyh.rumbo.core.navigation.HomeDestination
import tech.nikelyh.rumbo.core.navigation.LogProgressDestination
import tech.nikelyh.rumbo.core.navigation.OnboardingDestination
import tech.nikelyh.rumbo.core.navigation.ProcessDetailDestination
import tech.nikelyh.rumbo.core.navigation.ProcessesDestination
import tech.nikelyh.rumbo.core.navigation.ProgressDestination
import tech.nikelyh.rumbo.core.navigation.SettingsDestination
import tech.nikelyh.rumbo.core.navigation.StartSessionDestination
import tech.nikelyh.rumbo.core.navigation.TaskDetailDestination
import tech.nikelyh.rumbo.core.navigation.TasksDestination
import tech.nikelyh.rumbo.feature.home.navigation.homeScreen
import tech.nikelyh.rumbo.feature.home.navigation.navigateToHome
import tech.nikelyh.rumbo.feature.onboarding.navigation.onboardingScreen
import tech.nikelyh.rumbo.feature.processes.navigation.navigateToCreateProcess
import tech.nikelyh.rumbo.feature.processes.navigation.navigateToEditProcess
import tech.nikelyh.rumbo.feature.processes.navigation.navigateToProcessDetail
import tech.nikelyh.rumbo.feature.processes.navigation.navigateToProcesses
import tech.nikelyh.rumbo.feature.processes.navigation.processesScreen
import tech.nikelyh.rumbo.feature.progress.navigation.navigateToLogProgress
import tech.nikelyh.rumbo.feature.progress.navigation.navigateToProgress
import tech.nikelyh.rumbo.feature.progress.navigation.navigateToStartSession
import tech.nikelyh.rumbo.feature.progress.navigation.progressScreen
import tech.nikelyh.rumbo.feature.settings.navigation.navigateToSettings
import tech.nikelyh.rumbo.feature.settings.navigation.settingsScreen
import tech.nikelyh.rumbo.feature.tasks.navigation.navigateToCreateTask
import tech.nikelyh.rumbo.feature.tasks.navigation.navigateToTaskDetail
import tech.nikelyh.rumbo.feature.tasks.navigation.navigateToTasks
import tech.nikelyh.rumbo.feature.tasks.navigation.tasksScreen

// 4 Primary Top-Level Destinations
enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    HOME(HomeDestination.route, Icons.Default.Home, "Inicio"),
    PROCESSES(ProcessesDestination.route, Icons.AutoMirrored.Filled.ListAlt, "Procesos"),
    TASKS(TasksDestination.route, Icons.AutoMirrored.Filled.Assignment, "Tareas"),
    PROGRESS(ProgressDestination.route, Icons.Default.BarChart, "Progreso")
}

@Composable
fun RumboApp(
    hasCompletedOnboarding: Boolean?,
    isDarkMode: Boolean? = null,
    activeSession: ActiveSessionState? = null,
    navController: NavHostController = rememberNavController()
) {
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = isDarkMode ?: isSystemDark

    val isSessionRunning = hasCompletedOnboarding == true && activeSession != null && activeSession.isRunning

    val startDestination = when {
        hasCompletedOnboarding == false -> OnboardingDestination.route
        isSessionRunning -> StartSessionDestination.createRoute(activeSession?.processId, activeSession?.taskId)
        else -> HomeDestination.route
    }

    LaunchedEffect(activeSession?.isRunning) {
        if (hasCompletedOnboarding == true && activeSession != null && activeSession.isRunning) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute?.startsWith("start_session") != true) {
                navController.navigateToStartSession(
                    processId = activeSession.processId,
                    taskId = activeSession.taskId
                )
            }
        }
    }

    RumboTheme(darkTheme = darkTheme) {
        if (hasCompletedOnboarding == null) {
            RumboLoadingState(isLoading = true)
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isCompact = this.maxWidth < 600.dp

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentRoute = currentDestination?.route

                val isOnboarding = currentRoute == OnboardingDestination.route
                val isSettings = currentRoute == SettingsDestination.route
                val isTopLevel = TopLevelDestination.entries.any { it.route == currentRoute }

                Scaffold(
                    topBar = {
                        if (!isOnboarding) {
                            RumboTopBar(
                                title = when (currentRoute) {
                                    HomeDestination.route -> "Inicio"
                                    ProcessesDestination.route -> "Procesos"
                                    TasksDestination.route -> "Tareas"
                                    ProgressDestination.route -> "Progreso"
                                    SettingsDestination.route -> "Configuración"
                                    CreateProcessDestination.route -> "Nuevo Proceso"
                                    EditProcessDestination.route -> "Editar Proceso"
                                    CreateTaskDestination.route -> "Nueva Tarea"
                                    ProcessDetailDestination.route -> "Detalle de Proceso"
                                    TaskDetailDestination.route -> "Detalle de Tarea"
                                    StartSessionDestination.route -> "Sesión de Trabajo"
                                    LogProgressDestination.route -> "Registrar Progreso"
                                    else -> "Rumbo"
                                },
                                navigationIcon = if (!isTopLevel) Icons.AutoMirrored.Filled.ArrowBack else null,
                                navigationIconContentDescription = "Regresar",
                                onNavigationClick = { navController.popBackStack() },
                                actionIcon = if (!isSettings) Icons.Default.Settings else null,
                                actionIconContentDescription = "Configuración",
                                onActionClick = { navController.navigateToSettings() }
                            )
                        }
                    },
                    bottomBar = {
                        if (!isOnboarding && isCompact) {
                            RumboBottomBar(
                                destinations = TopLevelDestination.entries,
                                onNavigateToDestination = { destination ->
                                    navigateToTopLevelDestination(navController, destination)
                                },
                                currentDestination = currentDestination
                            )
                        }
                    }
                ) { paddingValues ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        if (!isOnboarding && !isCompact) {
                            RumboNavigationRail(
                                destinations = TopLevelDestination.entries,
                                onNavigateToDestination = { destination ->
                                    navigateToTopLevelDestination(navController, destination)
                                },
                                currentDestination = currentDestination
                            )
                        }

                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.weight(1f),
                            enterTransition = { fadeIn(animationSpec = tween(RumboAnimationTokens.DurationFast)) },
                            exitTransition = { fadeOut(animationSpec = tween(RumboAnimationTokens.DurationFast)) },
                            popEnterTransition = { fadeIn(animationSpec = tween(RumboAnimationTokens.DurationFast)) },
                            popExitTransition = { fadeOut(animationSpec = tween(RumboAnimationTokens.DurationFast)) }
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
                                onNavigateToProcess = { processId ->
                                    navController.navigateToProcessDetail(processId)
                                },
                                onNavigateToTask = { taskId ->
                                    navController.navigateToTaskDetail(taskId)
                                },
                                onNavigateToCreateProcess = {
                                    navController.navigateToCreateProcess()
                                },
                                onNavigateToCreateTask = {
                                    navController.navigateToCreateTask()
                                },
                                onNavigateToLogProgress = {
                                    navController.navigateToLogProgress()
                                },
                                onNavigateToStartSession = { processId, taskId ->
                                    navController.navigateToStartSession(processId = processId, taskId = taskId)
                                }
                            )
                            processesScreen(
                                onProcessClick = { processId ->
                                    navController.navigateToProcessDetail(processId)
                                },
                                onNavigateToCreateProcess = {
                                    navController.navigateToCreateProcess()
                                },
                                onNavigateToCreateTask = { processId ->
                                    navController.navigateToCreateTask(processId)
                                },
                                onNavigateToLogProgress = { processId ->
                                    navController.navigateToLogProgress(processId)
                                },
                                onNavigateToStartSession = { processId, taskId ->
                                    navController.navigateToStartSession(processId = processId, taskId = taskId)
                                },
                                onNavigateToEditProcess = { processId ->
                                    navController.navigateToEditProcess(processId)
                                },
                                onNavigateToTask = { taskId ->
                                    navController.navigateToTaskDetail(taskId)
                                },
                                onProcessCreated = {
                                    navController.popBackStack()
                                },
                                onProcessEdited = {
                                    navController.popBackStack()
                                }
                            )
                            tasksScreen(
                                onTaskClick = { taskId ->
                                    navController.navigateToTaskDetail(taskId)
                                },
                                onNavigateToCreateTask = {
                                    navController.navigateToCreateTask()
                                },
                                onTaskCreated = {
                                    navController.popBackStack()
                                },
                                onTaskDeleted = {
                                    navController.popBackStack()
                                },
                                onStartSession = { taskId, processId ->
                                    navController.navigateToStartSession(processId = processId, taskId = taskId)
                                }
                            )
                            progressScreen(
                                onSessionFinished = {
                                    if (!navController.popBackStack()) {
                                        navController.navigateToHome(
                                            navOptions {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        )
                                    }
                                },
                                onProgressLogged = {
                                    navController.popBackStack()
                                }
                            )
                            settingsScreen(
                                onResetCompleted = {
                                    navController.navigate(OnboardingDestination.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun navigateToTopLevelDestination(
    navController: NavHostController,
    destination: TopLevelDestination
) {
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

@Composable
private fun RumboNavigationRail(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?
) {
    NavigationRail {
        destinations.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationRailItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = { Icon(imageVector = destination.icon, contentDescription = destination.label) },
                label = { Text(text = destination.label) }
            )
        }
    }
}
