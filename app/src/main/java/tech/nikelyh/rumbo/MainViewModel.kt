package tech.nikelyh.rumbo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import tech.nikelyh.rumbo.core.data.repository.SettingsRepository
import tech.nikelyh.rumbo.core.data.repository.WorkSessionRepository
import tech.nikelyh.rumbo.core.model.ActiveSessionState
import javax.inject.Inject

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Success(
        val hasCompletedOnboarding: Boolean,
        val isDarkMode: Boolean? = null,
        val activeSession: ActiveSessionState? = null
    ) : MainUiState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    workSessionRepository: WorkSessionRepository
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = combine(
        settingsRepository.userSettings,
        workSessionRepository.activeSessionState
    ) { settings, sessionState ->
        MainUiState.Success(
            hasCompletedOnboarding = settings.hasCompletedOnboarding,
            isDarkMode = settings.isDarkModeEnabled,
            activeSession = if (sessionState.hasActiveSession && sessionState.isRunning) sessionState else null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState.Loading
    )
}
