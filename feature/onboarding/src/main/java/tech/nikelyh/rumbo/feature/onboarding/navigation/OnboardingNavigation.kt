package tech.nikelyh.rumbo.feature.onboarding.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import tech.nikelyh.rumbo.core.navigation.OnboardingDestination
import tech.nikelyh.rumbo.core.navigation.TutorialDestination
import tech.nikelyh.rumbo.feature.onboarding.OnboardingRoute
import tech.nikelyh.rumbo.feature.onboarding.TutorialScreen

fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) {
    this.navigate(OnboardingDestination.route, navOptions)
}

fun NavController.navigateToTutorial(navOptions: NavOptions? = null) {
    this.navigate(TutorialDestination.route, navOptions)
}

fun NavGraphBuilder.onboardingScreen(
    onOnboardingFinished: () -> Unit
) {
    composable(route = OnboardingDestination.route) {
        OnboardingRoute(onOnboardingFinished = onOnboardingFinished)
    }
}

fun NavGraphBuilder.tutorialScreen(
    onClose: () -> Unit
) {
    composable(route = TutorialDestination.route) {
        TutorialScreen(
            onFinishTutorial = onClose,
            showLanguageSelector = false
        )
    }
}
