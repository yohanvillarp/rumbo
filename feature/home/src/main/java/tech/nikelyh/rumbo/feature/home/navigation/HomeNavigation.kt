package tech.nikelyh.rumbo.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import tech.nikelyh.rumbo.core.navigation.HomeDestination
import tech.nikelyh.rumbo.feature.home.HomeRoute

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    this.navigate(HomeDestination.route, navOptions)
}

fun NavGraphBuilder.homeScreen(
    onNavigateToProcess: (String) -> Unit,
    onNavigateToTask: (String) -> Unit
) {
    composable(route = HomeDestination.route) {
        HomeRoute(
            onNavigateToProcess = onNavigateToProcess,
            onNavigateToTask = onNavigateToTask
        )
    }
}
