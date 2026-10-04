package tech.nikelyh.rumbo.feature.tasks.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import tech.nikelyh.rumbo.core.navigation.CreateTaskDestination
import tech.nikelyh.rumbo.core.navigation.TaskDetailDestination
import tech.nikelyh.rumbo.core.navigation.TasksDestination
import tech.nikelyh.rumbo.feature.tasks.CreateTaskRoute
import tech.nikelyh.rumbo.feature.tasks.TaskDetailRoute
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
    onTaskClick: (String) -> Unit,
    onNavigateToCreateTask: () -> Unit,
    onTaskCreated: () -> Unit,
    onTaskDeleted: () -> Unit,
    onStartSession: (String, String) -> Unit = { _, _ -> }
) {
    composable(route = TasksDestination.route) {
        TasksRoute(
            onTaskClick = onTaskClick,
            onNavigateToCreateTask = onNavigateToCreateTask,
            onStartSession = onStartSession
        )
    }

    composable(route = TaskDetailDestination.route) {
        TaskDetailRoute(
            onTaskDeleted = onTaskDeleted,
            onStartSession = onStartSession
        )
    }

    composable(route = CreateTaskDestination.route) {
        CreateTaskRoute(
            onTaskCreated = onTaskCreated
        )
    }
}
