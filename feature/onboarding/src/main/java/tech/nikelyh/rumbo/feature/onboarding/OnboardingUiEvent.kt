package tech.nikelyh.rumbo.feature.onboarding

import tech.nikelyh.rumbo.core.model.AppLanguage

/**
 * User interaction events for onboarding and tutorial setup.
 */
sealed interface OnboardingUiEvent {
    data class NameChanged(val name: String) : OnboardingUiEvent
    data class ChangeLanguage(val language: AppLanguage) : OnboardingUiEvent
    data object SubmitName : OnboardingUiEvent
}
