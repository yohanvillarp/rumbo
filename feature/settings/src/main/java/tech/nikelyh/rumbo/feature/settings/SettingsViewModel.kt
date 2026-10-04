package tech.nikelyh.rumbo.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.nikelyh.rumbo.core.data.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsRepository.userSettings
        .map { SettingsUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState.Loading
        )

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.ToggleDarkMode -> {
                viewModelScope.launch {
                    settingsRepository.setDarkMode(event.enabled)
                }
            }
            is SettingsUiEvent.ToggleNotifications -> {
                viewModelScope.launch {
                    settingsRepository.setNotifications(event.enabled)
                }
            }
            SettingsUiEvent.ResetApplicationData -> {
                viewModelScope.launch {
                    settingsRepository.resetApplicationData()
                }
            }
        }
    }
}
