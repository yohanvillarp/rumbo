package tech.nikelyh.rumbo.feature.onboarding

data class OnboardingUiState(
    val name: String = "",
    val nameError: String? = null,
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false
)
