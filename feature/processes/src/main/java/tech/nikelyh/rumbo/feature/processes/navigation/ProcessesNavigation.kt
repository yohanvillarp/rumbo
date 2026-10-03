package tech.nikelyh.rumbo.feature.processes.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import tech.nikelyh.rumbo.core.navigation.ProcessesDestination
import tech.nikelyh.rumbo.feature.processes.ProcessesRoute

fun NavController.navigateToProcesses(navOptions: NavOptions? = null) {
    this.navigate(ProcessesDestination.route, navOptions)
}

fun NavGraphBuilder.processesScreen(
    onProcessClick: (String) -> Unit
) {
    composable(route = ProcessesDestination.route) {
        ProcessesRoute(onProcessClick = onProcessClick)
    }
}
