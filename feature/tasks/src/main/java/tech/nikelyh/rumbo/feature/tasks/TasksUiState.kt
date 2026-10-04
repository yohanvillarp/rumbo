package tech.nikelyh.rumbo.feature.tasks

import tech.nikelyh.rumbo.core.model.Priority
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskSortOrder

/**
 * UI state representation for the tasks screen.
 */
sealed interface TasksUiState {
    data object Loading : TasksUiState
    data class Content(
        val tasks: List<Task>,
        val availableProcesses: List<Process>,
        val selectedFilter: TaskFilter = TaskFilter.PENDING,
        val selectedProcessId: String? = null,
        val selectedPriority: Priority? = null,
        val taskSortOrder: TaskSortOrder = TaskSortOrder.DUE_DATE,
        val searchQuery: String = ""
    ) : TasksUiState
    data object Empty : TasksUiState
    data class Error(val message: String) : TasksUiState
}
