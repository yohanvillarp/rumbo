package tech.nikelyh.rumbo.feature.tasks.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import tech.nikelyh.rumbo.core.navigation.CreateTaskDestination
import tech.nikelyh.rumbo.core.navigation.TaskDetailDestination
import tech.nikelyh.rumbo.core.navigation.TasksDestination
import tech.nikelyh.rumbo.feature.tasks.TasksRoute

fun NavController.navigateToTasks(navOptions: NavOptions? = null) {
    this.navigate(TasksDestination.route, navOptions)
}

fun NavController.navigateToTaskDetail(taskId: String, navOptions: NavOptions? = null) {
    this.navigate(TaskDetailDestination.createRoute(taskId), navOptions)
}

fun NavController.navigateToCreateTask(processId: String? = null, navOptions: NavOptions? = null) {
    this.navigate(CreateTaskDestination.createRoute(processId), navOptions)
}

fun NavGraphBuilder.tasksScreen(
    onTaskClick: (String) -> Unit
) {
    composable(route = TasksDestination.route) {
        TasksRoute(onTaskClick = onTaskClick)
    }

    composable(route = TaskDetailDestination.route) { backStackEntry ->
        val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Detalle de la Tarea: $taskId")
        }
    }

    composable(route = CreateTaskDestination.route) { backStackEntry ->
        val processId = backStackEntry.arguments?.getString("processId")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Crear Tarea ${if (processId != null) "para Proceso: $processId" else ""}")
        }
    }
}
