package tech.nikelyh.rumbo.feature.onboarding

sealed interface OnboardingUiEvent {
    data class NameChanged(val name: String) : OnboardingUiEvent
    data object SubmitName : OnboardingUiEvent
}
