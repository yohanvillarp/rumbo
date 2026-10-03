package tech.nikelyh.rumbo.feature.progress

import tech.nikelyh.rumbo.core.model.UserProgress

sealed interface ProgressUiState {
    data object Loading : ProgressUiState
    data class Success(
        val userProgress: UserProgress
    ) : ProgressUiState
    data class Error(val message: String) : ProgressUiState
}
