package tech.nikelyh.rumbo.feature.settings

sealed interface SettingsUiEvent {
    data class ToggleDarkMode(val enabled: Boolean) : SettingsUiEvent
    data class ToggleNotifications(val enabled: Boolean) : SettingsUiEvent
}
