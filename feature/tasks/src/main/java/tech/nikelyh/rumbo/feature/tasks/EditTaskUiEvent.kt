package tech.nikelyh.rumbo.feature.tasks

import tech.nikelyh.rumbo.core.model.Priority

sealed interface EditTaskUiEvent {
    data class TitleChanged(val title: String) : EditTaskUiEvent
    data class DescriptionChanged(val description: String) : EditTaskUiEvent
    data class ProcessSelected(val processId: String) : EditTaskUiEvent
    data class PriorityChanged(val priority: Priority) : EditTaskUiEvent
    data class DurationChanged(val minutes: String) : EditTaskUiEvent
    data class CostChanged(val cost: String) : EditTaskUiEvent
    data class DueDateChanged(val millis: Long?) : EditTaskUiEvent
    data object SubmitTask : EditTaskUiEvent
}
