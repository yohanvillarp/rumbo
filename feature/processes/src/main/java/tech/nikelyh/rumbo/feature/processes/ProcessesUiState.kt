package tech.nikelyh.rumbo.feature.processes

import tech.nikelyh.rumbo.core.model.Process

sealed interface ProcessesUiState {
    data object Loading : ProcessesUiState
    data class Success(
        val processes: List<Process>,
        val searchQuery: String = ""
    ) : ProcessesUiState
    data class Error(val message: String) : ProcessesUiState
}
