package tech.nikelyh.rumbo.feature.onboarding.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import tech.nikelyh.rumbo.core.navigation.OnboardingDestination
import tech.nikelyh.rumbo.feature.onboarding.OnboardingRoute

fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) {
    this.navigate(OnboardingDestination.route, navOptions)
}

fun NavGraphBuilder.onboardingScreen(
    onOnboardingFinished: () -> Unit
) {
    composable(route = OnboardingDestination.route) {
        OnboardingRoute(onOnboardingFinished = onOnboardingFinished)
    }
}
