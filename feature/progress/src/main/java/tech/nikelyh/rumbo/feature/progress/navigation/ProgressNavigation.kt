package tech.nikelyh.rumbo.feature.progress.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import tech.nikelyh.rumbo.core.navigation.LogProgressDestination
import tech.nikelyh.rumbo.core.navigation.ProgressDestination
import tech.nikelyh.rumbo.core.navigation.StartSessionDestination
import tech.nikelyh.rumbo.feature.progress.LogProgressRoute
import tech.nikelyh.rumbo.feature.progress.ProgressRoute
import tech.nikelyh.rumbo.feature.progress.StartSessionRoute

fun NavController.navigateToProgress(navOptions: NavOptions? = null) {
    this.navigate(ProgressDestination.route, navOptions)
}

fun NavController.navigateToLogProgress(processId: String? = null, navOptions: NavOptions? = null) {
    val route = if (processId != null) LogProgressDestination.createRoute(processId) else LogProgressDestination.route
    this.navigate(route, navOptions)
}

fun NavController.navigateToStartSession(processId: String? = null, taskId: String? = null, navOptions: NavOptions? = null) {
    this.navigate(StartSessionDestination.createRoute(processId, taskId), navOptions)
}

fun NavGraphBuilder.progressScreen(
    onSessionFinished: () -> Unit,
    onProgressLogged: () -> Unit
) {
    composable(route = ProgressDestination.route) {
        ProgressRoute()
    }

    composable(route = LogProgressDestination.route) {
        LogProgressRoute(
            onProgressLogged = onProgressLogged
        )
    }

    composable(
        route = StartSessionDestination.route,
        arguments = listOf(
            navArgument("processId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument("taskId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
        StartSessionRoute(
            onSessionFinished = onSessionFinished
        )
    }
}
