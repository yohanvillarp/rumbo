package tech.nikelyh.rumbo.feature.settings

import tech.nikelyh.rumbo.core.model.UserSettings

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val userSettings: UserSettings) : SettingsUiState
}
