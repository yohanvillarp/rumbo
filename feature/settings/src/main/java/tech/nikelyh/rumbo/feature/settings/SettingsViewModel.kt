package tech.nikelyh.rumbo.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.nikelyh.rumbo.core.data.repository.SettingsRepository
import tech.nikelyh.rumbo.core.model.UserSettings
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val isResettingFlow = MutableStateFlow(false)
    private val _resetCompleted = MutableSharedFlow<Unit>()
    val resetCompleted: SharedFlow<Unit> = _resetCompleted.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.userSettings,
        isResettingFlow
    ) { settings, resetting ->
        SettingsUiState.Success(
            userSettings = settings,
            isResetting = resetting
        )
    }.stateIn(
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
                    isResettingFlow.value = true
                    try {
                        settingsRepository.resetApplicationData()
                        _resetCompleted.emit(Unit)
                    } finally {
                        isResettingFlow.value = false
                    }
                }
            }
        }
    }
}
