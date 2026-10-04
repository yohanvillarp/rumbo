package tech.nikelyh.rumbo.feature.home

import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.Task

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Content(
        val greeting: String,
        val userName: String,
        val continueProcess: Process?,
        val activeProcesses: List<Process>,
        val todayTasks: List<Task>,
        val allProcesses: List<Process> = emptyList(),
        val userMessage: String? = null
    ) : HomeUiState
    data object Empty : HomeUiState
    data class Error(val message: String) : HomeUiState
}
