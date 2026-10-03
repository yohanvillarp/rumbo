package tech.nikelyh.rumbo.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import tech.nikelyh.rumbo.core.navigation.SettingsDestination
import tech.nikelyh.rumbo.feature.settings.SettingsRoute

fun NavController.navigateToSettings(navOptions: NavOptions? = null) {
    this.navigate(SettingsDestination.route, navOptions)
}

fun NavGraphBuilder.settingsScreen() {
    composable(route = SettingsDestination.route) {
        SettingsRoute()
    }
}
