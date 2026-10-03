package tech.nikelyh.rumbo.feature.progress.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import tech.nikelyh.rumbo.core.navigation.ProgressDestination
import tech.nikelyh.rumbo.feature.progress.ProgressRoute

fun NavController.navigateToProgress(navOptions: NavOptions? = null) {
    this.navigate(ProgressDestination.route, navOptions)
}

fun NavGraphBuilder.progressScreen() {
    composable(route = ProgressDestination.route) {
        ProgressRoute()
    }
}
