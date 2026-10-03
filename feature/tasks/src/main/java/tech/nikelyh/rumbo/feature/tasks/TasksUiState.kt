package tech.nikelyh.rumbo.feature.tasks

import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.Task

sealed interface TasksUiState {
    data object Loading : TasksUiState
    data class Content(
        val tasks: List<Task>,
        val availableProcesses: List<Process>,
        val selectedFilter: TaskFilter = TaskFilter.PENDING,
        val selectedProcessId: String? = null,
        val searchQuery: String = ""
    ) : TasksUiState
    data object Empty : TasksUiState
    data class Error(val message: String) : TasksUiState
}
