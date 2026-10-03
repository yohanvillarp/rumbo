package tech.nikelyh.rumbo.feature.tasks

import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus

sealed interface TasksUiState {
    data object Loading : TasksUiState
    data class Success(
        val tasks: List<Task>,
        val filterStatus: TaskStatus? = null
    ) : TasksUiState
    data class Error(val message: String) : TasksUiState
}
