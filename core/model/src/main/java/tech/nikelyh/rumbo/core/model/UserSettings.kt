package tech.nikelyh.rumbo.core.model

data class UserSettings(
    val isDarkModeEnabled: Boolean = false,
    val isNotificationsEnabled: Boolean = true,
    val hasCompletedOnboarding: Boolean = false
)
