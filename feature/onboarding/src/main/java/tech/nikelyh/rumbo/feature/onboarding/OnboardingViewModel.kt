package tech.nikelyh.rumbo.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.nikelyh.rumbo.core.data.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onEvent(event: OnboardingUiEvent) {
        when (event) {
            OnboardingUiEvent.NextPage -> {
                _uiState.update { current ->
                    if (current.currentPage < current.totalPages - 1) {
                        current.copy(currentPage = current.currentPage + 1)
                    } else current
                }
            }
            OnboardingUiEvent.PreviousPage -> {
                _uiState.update { current ->
                    if (current.currentPage > 0) {
                        current.copy(currentPage = current.currentPage - 1)
                    } else current
                }
            }
            OnboardingUiEvent.CompleteOnboarding -> {
                viewModelScope.launch {
                    settingsRepository.setOnboardingCompleted(true)
                    _uiState.update { it.copy(isCompleted = true) }
                }
            }
        }
    }
}
