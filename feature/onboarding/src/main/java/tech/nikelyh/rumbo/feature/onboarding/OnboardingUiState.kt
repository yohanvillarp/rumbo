package tech.nikelyh.rumbo.feature.onboarding

data class OnboardingUiState(
    val currentPage: Int = 0,
    val totalPages: Int = 3,
    val isCompleted: Boolean = false
)
