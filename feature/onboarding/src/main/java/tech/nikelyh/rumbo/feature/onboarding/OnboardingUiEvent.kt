package tech.nikelyh.rumbo.feature.onboarding

sealed interface OnboardingUiEvent {
    data object NextPage : OnboardingUiEvent
    data object PreviousPage : OnboardingUiEvent
    data object CompleteOnboarding : OnboardingUiEvent
}
