package tech.nikelyh.rumbo.core.model

/**
 * User preferences configuration.
 *
 * @property isDarkModeEnabled Explicit dark theme preference (null = follow system).
 * @property isNotificationsEnabled Whether notifications are enabled.
 * @property hasCompletedOnboarding Whether the user has completed the initial onboarding tutorial.
 * @property languageCode Selected language code (null = follow device system language, "es", "en").
 */
data class UserSettings(
    val isDarkModeEnabled: Boolean? = null,
    val isNotificationsEnabled: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val languageCode: String? = null
)
