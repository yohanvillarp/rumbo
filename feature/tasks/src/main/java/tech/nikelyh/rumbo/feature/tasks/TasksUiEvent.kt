package tech.nikelyh.rumbo.feature.tasks

import tech.nikelyh.rumbo.core.model.Priority
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskSortOrder

sealed interface TasksUiEvent {
    data class FilterChanged(val filter: TaskFilter) : TasksUiEvent
    data class ProcessFilterChanged(val processId: String?) : TasksUiEvent
    data class PriorityFilterChanged(val priority: Priority?) : TasksUiEvent
    data class SortOrderChanged(val sortOrder: TaskSortOrder) : TasksUiEvent
    data class SearchQueryChanged(val query: String) : TasksUiEvent
    data class ToggleTaskStatus(val task: Task) : TasksUiEvent
    data class CompleteTaskWithDuration(val task: Task, val durationMinutes: Long) : TasksUiEvent
    data class OnTaskSelected(val taskId: String) : TasksUiEvent
}
