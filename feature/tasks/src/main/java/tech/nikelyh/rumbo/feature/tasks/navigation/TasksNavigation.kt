package tech.nikelyh.rumbo.feature.tasks.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import tech.nikelyh.rumbo.core.navigation.TasksDestination
import tech.nikelyh.rumbo.feature.tasks.TasksRoute

fun NavController.navigateToTasks(navOptions: NavOptions? = null) {
    this.navigate(TasksDestination.route, navOptions)
}

fun NavGraphBuilder.tasksScreen(
    onTaskClick: (String) -> Unit
) {
    composable(route = TasksDestination.route) {
        TasksRoute(onTaskClick = onTaskClick)
    }
}
