package tech.nikelyh.rumbo.feature.tasks.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import tech.nikelyh.rumbo.core.navigation.CreateTaskDestination
import tech.nikelyh.rumbo.core.navigation.EditTaskDestination
import tech.nikelyh.rumbo.core.navigation.TaskDetailDestination
import tech.nikelyh.rumbo.core.navigation.TasksDestination
import tech.nikelyh.rumbo.feature.tasks.CreateTaskRoute
import tech.nikelyh.rumbo.feature.tasks.EditTaskRoute
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

fun NavController.navigateToEditTask(taskId: String, navOptions: NavOptions? = null) {
    this.navigate(EditTaskDestination.createRoute(taskId), navOptions)
}

fun NavGraphBuilder.tasksScreen(
    onTaskClick: (String) -> Unit,
    onNavigateToCreateTask: () -> Unit,
    onNavigateToEditTask: (String) -> Unit = {},
    onTaskCreated: () -> Unit,
    onTaskUpdated: () -> Unit = {},
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
            onNavigateToEditTask = onNavigateToEditTask,
            onStartSession = onStartSession
        )
    }

    composable(route = CreateTaskDestination.route) {
        CreateTaskRoute(
            onTaskCreated = onTaskCreated
        )
    }

    composable(route = EditTaskDestination.route) {
        EditTaskRoute(
            onTaskUpdated = onTaskUpdated
        )
    }
}
