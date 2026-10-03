package tech.nikelyh.rumbo.core.navigation

object OnboardingDestination : RumboNavigationDestination {
    override val route = "onboarding_route"
    override val destination = "onboarding_destination"
}

object HomeDestination : RumboNavigationDestination {
    override val route = "home_route"
    override val destination = "home_destination"
}

object ProcessesDestination : RumboNavigationDestination {
    override val route = "processes_route"
    override val destination = "processes_destination"
}

object TasksDestination : RumboNavigationDestination {
    override val route = "tasks_route"
    override val destination = "tasks_destination"
}

object ProgressDestination : RumboNavigationDestination {
    override val route = "progress_route"
    override val destination = "progress_destination"
}

object SettingsDestination : RumboNavigationDestination {
    override val route = "settings_route"
    override val destination = "settings_destination"
}
