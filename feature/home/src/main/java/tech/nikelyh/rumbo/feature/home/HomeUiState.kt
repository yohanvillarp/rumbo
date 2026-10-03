package tech.nikelyh.rumbo.feature.home

import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.UserProgress

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val recentProcesses: List<Process>,
        val pendingTasks: List<Task>,
        val progress: UserProgress
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
