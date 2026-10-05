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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
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
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import tech.nikelyh.rumbo.core.designsystem.component.RumboLoadingState
import tech.nikelyh.rumbo.core.designsystem.component.RumboSplashScreen
import tech.nikelyh.rumbo.core.designsystem.component.RumboTopBar
import tech.nikelyh.rumbo.core.designsystem.theme.RumboAnimationTokens
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.ActiveSessionState
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.navigation.CreateProcessDestination
import tech.nikelyh.rumbo.core.navigation.CreateTaskDestination
import tech.nikelyh.rumbo.core.navigation.EditProcessDestination
import tech.nikelyh.rumbo.core.navigation.EditTaskDestination
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
import tech.nikelyh.rumbo.core.navigation.TutorialDestination
import tech.nikelyh.rumbo.feature.home.navigation.homeScreen
import tech.nikelyh.rumbo.feature.home.navigation.navigateToHome
import tech.nikelyh.rumbo.feature.onboarding.navigation.navigateToTutorial
import tech.nikelyh.rumbo.feature.onboarding.navigation.onboardingScreen
import tech.nikelyh.rumbo.feature.onboarding.navigation.tutorialScreen
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
import tech.nikelyh.rumbo.feature.tasks.navigation.navigateToEditTask
import tech.nikelyh.rumbo.feature.tasks.navigation.navigateToTaskDetail
import tech.nikelyh.rumbo.feature.tasks.navigation.navigateToTasks
import tech.nikelyh.rumbo.feature.tasks.navigation.tasksScreen

import androidx.compose.ui.res.stringResource
import tech.nikelyh.rumbo.core.designsystem.R

// 4 Primary Top-Level Destinations
enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val labelResId: Int
) {
    HOME(HomeDestination.route, Icons.Default.Home, R.string.nav_home),
    PROCESSES(ProcessesDestination.route, Icons.AutoMirrored.Filled.ListAlt, R.string.nav_processes),
    TASKS(TasksDestination.route, Icons.AutoMirrored.Filled.Assignment, R.string.nav_tasks),
    PROGRESS(ProgressDestination.route, Icons.Default.BarChart, R.string.nav_progress)
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

    val startDestination = if (hasCompletedOnboarding == false) {
        OnboardingDestination.route
    } else {
        HomeDestination.route
    }

    var isSplashFinished by remember { mutableStateOf(false) }

    LaunchedEffect(isSplashFinished, activeSession?.isRunning) {
        if (isSplashFinished && hasCompletedOnboarding == true && activeSession != null && activeSession.isRunning) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != null && !currentRoute.startsWith("start_session")) {
                navController.navigateToStartSession(
                    processId = activeSession.processId,
                    taskId = activeSession.taskId
                )
            }
        }
    }

    LaunchedEffect(hasCompletedOnboarding) {
        if (hasCompletedOnboarding != null) {
            kotlinx.coroutines.delay(800)
            isSplashFinished = true
        }
    }

    RumboTheme(darkTheme = darkTheme) {
        Crossfade(
            targetState = (hasCompletedOnboarding != null && isSplashFinished),
            animationSpec = tween(durationMillis = 400),
            label = "SplashCrossfade"
        ) { isReady ->
            if (!isReady) {
                RumboSplashScreen()
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isCompact = this.maxWidth < 600.dp

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentRoute = currentDestination?.route

                val isOnboarding = currentRoute == OnboardingDestination.route
                val isTutorial = currentRoute == TutorialDestination.route
                val isSettings = currentRoute == SettingsDestination.route
                val isTopLevel = TopLevelDestination.entries.any { it.route == currentRoute }

                Scaffold(
                    topBar = {
                        if (!isOnboarding) {
                            RumboTopBar(
                                title = when (currentRoute) {
                                    HomeDestination.route -> stringResource(R.string.title_home)
                                    ProcessesDestination.route -> stringResource(R.string.title_processes)
                                    TasksDestination.route -> stringResource(R.string.title_tasks)
                                    ProgressDestination.route -> stringResource(R.string.title_progress)
                                    SettingsDestination.route -> stringResource(R.string.title_settings)
                                    TutorialDestination.route -> stringResource(R.string.title_tutorial)
                                    CreateProcessDestination.route -> stringResource(R.string.title_create_process)
                                    EditProcessDestination.route -> stringResource(R.string.title_edit_process)
                                    CreateTaskDestination.route -> stringResource(R.string.title_create_task)
                                    EditTaskDestination.route -> stringResource(R.string.title_edit_task)
                                    ProcessDetailDestination.route -> stringResource(R.string.title_process_detail)
                                    TaskDetailDestination.route -> stringResource(R.string.title_task_detail)
                                    StartSessionDestination.route -> stringResource(R.string.title_start_session)
                                    LogProgressDestination.route -> stringResource(R.string.title_log_progress)
                                    else -> stringResource(R.string.app_name)
                                },
                                navigationIcon = if (!isTopLevel) Icons.AutoMirrored.Filled.ArrowBack else null,
                                navigationIconContentDescription = stringResource(R.string.action_back),
                                onNavigationClick = { navController.popBackStack() },
                                secondaryActionIcon = if (!isOnboarding && !isTutorial) Icons.AutoMirrored.Filled.HelpOutline else null,
                                secondaryActionContentDescription = stringResource(R.string.title_tutorial),
                                onSecondaryActionClick = { navController.navigateToTutorial() },
                                actionIcon = if (!isSettings) Icons.Default.Settings else null,
                                actionIconContentDescription = stringResource(R.string.title_settings),
                                onActionClick = { navController.navigateToSettings() }
                            )
                        }
                    },
                    bottomBar = {
                        if (isTopLevel && isCompact) {
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
                        if (isTopLevel && !isCompact) {
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
                                onNavigateToProcesses = {
                                    navController.navigateToProcesses()
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
                                onNavigateToCreateProcess = { parentProcessId ->
                                    navController.navigateToCreateProcess(parentProcessId)
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
                                },
                                onNavigateBack = {
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
                                onNavigateToEditTask = { taskId ->
                                    navController.navigateToEditTask(taskId)
                                },
                                onTaskCreated = {
                                    navController.popBackStack()
                                },
                                onTaskUpdated = {
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
                                                popUpTo(navController.graph.id) { inclusive = true }
                                            }
                                        )
                                    }
                                },
                                onSessionPaused = { taskId, processId ->
                                    val previousRoute = navController.previousBackStackEntry?.destination?.route
                                    if (previousRoute == TaskDetailDestination.route) {
                                        navController.popBackStack()
                                    } else if (taskId != null) {
                                        navController.navigateToTaskDetail(
                                            taskId = taskId,
                                            navOptions = navOptions {
                                                popUpTo(StartSessionDestination.route) { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        )
                                    } else if (previousRoute == ProcessDetailDestination.route) {
                                        navController.popBackStack()
                                    } else if (processId != null && processId != Process.GENERAL_PROCESS_ID) {
                                        navController.navigateToProcessDetail(
                                            processId = processId,
                                            navOptions = navOptions {
                                                popUpTo(StartSessionDestination.route) { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        )
                                    } else {
                                        if (!navController.popBackStack()) {
                                            navController.navigateToTasks(
                                                navOptions = navOptions {
                                                    popUpTo(navController.graph.id) { inclusive = true }
                                                }
                                            )
                                        }
                                    }
                                },
                                onProgressLogged = {
                                    navController.popBackStack()
                                }
                            )
                            settingsScreen(
                                onResetCompleted = {
                                    navController.navigate(OnboardingDestination.route) {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                            tutorialScreen(
                                onClose = { navController.popBackStack() }
                            )
                        }
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
            saveState = false
        }
        launchSingleTop = true
        restoreState = false
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
            val labelText = stringResource(destination.labelResId)
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = { Icon(imageVector = destination.icon, contentDescription = labelText) },
                label = { Text(text = labelText) }
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
            val labelText = stringResource(destination.labelResId)
            NavigationRailItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = { Icon(imageVector = destination.icon, contentDescription = labelText) },
                label = { Text(text = labelText) }
            )
        }
    }
}
