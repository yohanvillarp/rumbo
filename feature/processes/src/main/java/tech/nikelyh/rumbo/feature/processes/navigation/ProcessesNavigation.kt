package tech.nikelyh.rumbo.feature.processes.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import tech.nikelyh.rumbo.core.navigation.CreateProcessDestination
import tech.nikelyh.rumbo.core.navigation.ProcessDetailDestination
import tech.nikelyh.rumbo.core.navigation.ProcessesDestination
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
    onProcessClick: (String) -> Unit
) {
    composable(route = ProcessesDestination.route) {
        ProcessesRoute(onProcessClick = onProcessClick)
    }

    composable(route = ProcessDetailDestination.route) { backStackEntry ->
        val processId = backStackEntry.arguments?.getString("processId") ?: ""
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Detalle del Proceso: $processId")
        }
    }

    composable(route = CreateProcessDestination.route) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Crear Proceso")
        }
    }
}
