package tech.nikelyh.rumbo.feature.tasks

import tech.nikelyh.rumbo.core.model.Task

sealed interface TaskDetailUiState {
    data object Loading : TaskDetailUiState
    data class Content(
        val task: Task,
        val processName: String,
        val hasStartedSession: Boolean = false
    ) : TaskDetailUiState
    data class Error(val message: String) : TaskDetailUiState
}
