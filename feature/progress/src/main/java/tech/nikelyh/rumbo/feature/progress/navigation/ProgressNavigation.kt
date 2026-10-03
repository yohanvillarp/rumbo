package tech.nikelyh.rumbo.feature.progress.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import tech.nikelyh.rumbo.core.navigation.LogProgressDestination
import tech.nikelyh.rumbo.core.navigation.ProgressDestination
import tech.nikelyh.rumbo.core.navigation.StartSessionDestination
import tech.nikelyh.rumbo.feature.progress.ProgressRoute

fun NavController.navigateToProgress(navOptions: NavOptions? = null) {
    this.navigate(ProgressDestination.route, navOptions)
}

fun NavController.navigateToLogProgress(processId: String, navOptions: NavOptions? = null) {
    this.navigate(LogProgressDestination.createRoute(processId), navOptions)
}

fun NavController.navigateToStartSession(processId: String? = null, taskId: String? = null, navOptions: NavOptions? = null) {
    this.navigate(StartSessionDestination.createRoute(processId, taskId), navOptions)
}

fun NavGraphBuilder.progressScreen() {
    composable(route = ProgressDestination.route) {
        ProgressRoute()
    }

    composable(route = LogProgressDestination.route) { backStackEntry ->
        val processId = backStackEntry.arguments?.getString("processId") ?: ""
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Registrar Evaluación de Progreso para Proceso: $processId")
        }
    }

    composable(route = StartSessionDestination.route) { backStackEntry ->
        val processId = backStackEntry.arguments?.getString("processId")
        val taskId = backStackEntry.arguments?.getString("taskId")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Iniciar Sesión de Trabajo (Proceso: ${processId ?: "N/A"}, Tarea: ${taskId ?: "N/A"})")
        }
    }
}
