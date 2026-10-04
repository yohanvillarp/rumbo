package tech.nikelyh.rumbo.feature.processes.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import tech.nikelyh.rumbo.core.navigation.CreateProcessDestination
import tech.nikelyh.rumbo.core.navigation.EditProcessDestination
import tech.nikelyh.rumbo.core.navigation.ProcessDetailDestination
import tech.nikelyh.rumbo.core.navigation.ProcessesDestination
import tech.nikelyh.rumbo.feature.processes.CreateProcessRoute
import tech.nikelyh.rumbo.feature.processes.EditProcessRoute
import tech.nikelyh.rumbo.feature.processes.ProcessDetailRoute
import tech.nikelyh.rumbo.feature.processes.ProcessesRoute

fun NavController.navigateToProcesses(navOptions: NavOptions? = null) {
    this.navigate(ProcessesDestination.route, navOptions)
}

fun NavController.navigateToProcessDetail(processId: String, navOptions: NavOptions? = null) {
    this.navigate(ProcessDetailDestination.createRoute(processId), navOptions)
}

fun NavController.navigateToCreateProcess(parentProcessId: String? = null, navOptions: NavOptions? = null) {
    this.navigate(CreateProcessDestination.createRoute(parentProcessId), navOptions)
}

fun NavController.navigateToEditProcess(processId: String, navOptions: NavOptions? = null) {
    this.navigate(EditProcessDestination.createRoute(processId), navOptions)
}

fun NavGraphBuilder.processesScreen(
    onProcessClick: (String) -> Unit,
    onNavigateToCreateProcess: (String?) -> Unit,
    onNavigateToCreateTask: (String) -> Unit,
    onNavigateToLogProgress: (String) -> Unit,
    onNavigateToStartSession: (String, String?) -> Unit,
    onNavigateToEditProcess: (String) -> Unit,
    onNavigateToTask: (String) -> Unit,
    onProcessCreated: () -> Unit,
    onProcessEdited: () -> Unit
) {
    composable(route = ProcessesDestination.route) {
        ProcessesRoute(
            onProcessClick = onProcessClick,
            onNavigateToCreateProcess = { onNavigateToCreateProcess(null) }
        )
    }

    composable(route = ProcessDetailDestination.route) {
        ProcessDetailRoute(
            onNavigateToEditProcess = onNavigateToEditProcess,
            onNavigateToCreateTask = onNavigateToCreateTask,
            onNavigateToLogProgress = onNavigateToLogProgress,
            onNavigateToStartSession = onNavigateToStartSession,
            onNavigateToCreateProcess = onNavigateToCreateProcess,
            onNavigateToProcessDetail = onProcessClick,
            onNavigateToTask = onNavigateToTask
        )
    }

    composable(route = CreateProcessDestination.route) {
        CreateProcessRoute(
            onProcessCreated = onProcessCreated
        )
    }

    composable(route = EditProcessDestination.route) {
        EditProcessRoute(
            onProcessEdited = onProcessEdited
        )
    }
}
