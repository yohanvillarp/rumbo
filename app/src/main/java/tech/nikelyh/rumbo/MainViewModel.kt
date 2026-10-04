package tech.nikelyh.rumbo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import tech.nikelyh.rumbo.core.data.repository.SettingsRepository
import javax.inject.Inject

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Success(
        val hasCompletedOnboarding: Boolean,
        val isDarkMode: Boolean? = null
    ) : MainUiState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = settingsRepository.userSettings
        .map { 
            MainUiState.Success(
                hasCompletedOnboarding = it.hasCompletedOnboarding,
                isDarkMode = it.isDarkModeEnabled
            ) 
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState.Loading
        )
}
