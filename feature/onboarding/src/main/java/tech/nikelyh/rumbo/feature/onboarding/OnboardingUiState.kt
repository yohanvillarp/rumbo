package tech.nikelyh.rumbo.feature.onboarding

/**
 * UI state for the initial onboarding and tutorial flow.
 *
 * @property name User's input name.
 * @property nameError Validation error message if name is invalid.
 * @property isSubmitting True while completing onboarding and persisting data.
 * @property isCompleted True once onboarding is fully finished.
 * @property selectedLanguageCode Currently selected language code, or null for system default.
 */
data class OnboardingUiState(
    val name: String = "",
    val nameError: String? = null,
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
    val selectedLanguageCode: String? = null
)
