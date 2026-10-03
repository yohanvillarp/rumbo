package tech.nikelyh.rumbo.feature.tasks

import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus

sealed interface TasksUiEvent {
    data class FilterByStatus(val status: TaskStatus?) : TasksUiEvent
    data class ToggleTaskStatus(val task: Task) : TasksUiEvent
    data class DeleteTask(val taskId: String) : TasksUiEvent
    data class TaskSelected(val taskId: String) : TasksUiEvent
}
