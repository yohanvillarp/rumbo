package tech.nikelyh.rumbo.core.model

data class UserSettings(
    val isDarkModeEnabled: Boolean? = null,
    val isNotificationsEnabled: Boolean = true,
    val hasCompletedOnboarding: Boolean = false
)
