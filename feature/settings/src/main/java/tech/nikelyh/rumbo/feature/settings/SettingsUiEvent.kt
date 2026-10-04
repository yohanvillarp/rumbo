package tech.nikelyh.rumbo.feature.settings

import tech.nikelyh.rumbo.core.model.AppLanguage

/**
 * User interaction events dispatched from the settings screen.
 */
sealed interface SettingsUiEvent {
    data class ToggleDarkMode(val enabled: Boolean) : SettingsUiEvent
    data class ToggleNotifications(val enabled: Boolean) : SettingsUiEvent
    data class ChangeLanguage(val language: AppLanguage) : SettingsUiEvent
    data object ResetApplicationData : SettingsUiEvent
}
