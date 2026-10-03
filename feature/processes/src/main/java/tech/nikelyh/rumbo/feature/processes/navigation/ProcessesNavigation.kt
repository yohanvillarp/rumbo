package tech.nikelyh.rumbo.feature.processes.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import tech.nikelyh.rumbo.core.navigation.CreateProcessDestination
import tech.nikelyh.rumbo.core.navigation.ProcessDetailDestination
import tech.nikelyh.rumbo.core.navigation.ProcessesDestination
import tech.nikelyh.rumbo.feature.processes.CreateProcessRoute
import tech.nikelyh.rumbo.feature.processes.ProcessDetailRoute
import tech.nikelyh.rumbo.feature.processes.ProcessesRoute

fun NavController.navigateToProcesses(navOptions: NavOptions? = null) {
    this.navigate(ProcessesDestination.route, navOptions)
}

fun NavController.navigateToProcessDetail(processId: String, navOptions: NavOptions? = null) {
    this.navigate(ProcessDetailDestination.createRoute(processId), navOptions)
}

fun NavController.navigateToCreateProcess(navOptions: NavOptions? = null) {
    this.navigate(CreateProcessDestination.route, navOptions)
}

fun NavGraphBuilder.processesScreen(
    onProcessClick: (String) -> Unit,
    onNavigateToCreateProcess: () -> Unit,
    onNavigateToCreateTask: (String) -> Unit,
    onNavigateToLogProgress: (String) -> Unit,
    onNavigateToStartSession: (String) -> Unit,
    onNavigateToEditProcess: (String) -> Unit,
    onProcessCreated: () -> Unit
) {
    composable(route = ProcessesDestination.route) {
        ProcessesRoute(
            onProcessClick = onProcessClick,
            onNavigateToCreateProcess = onNavigateToCreateProcess
        )
    }

    composable(route = ProcessDetailDestination.route) {
        ProcessDetailRoute(
            onNavigateToEditProcess = onNavigateToEditProcess,
            onNavigateToCreateTask = onNavigateToCreateTask,
            onNavigateToLogProgress = onNavigateToLogProgress,
            onNavigateToStartSession = onNavigateToStartSession
        )
    }

    composable(route = CreateProcessDestination.route) {
        CreateProcessRoute(
            onProcessCreated = onProcessCreated
        )
    }
}
