package tech.nikelyh.rumbo.feature.tasks

import tech.nikelyh.rumbo.core.model.Priority

sealed interface CreateTaskUiEvent {
    data class TitleChanged(val title: String) : CreateTaskUiEvent
    data class DescriptionChanged(val description: String) : CreateTaskUiEvent
    data class ProcessSelected(val processId: String) : CreateTaskUiEvent
    data class PriorityChanged(val priority: Priority) : CreateTaskUiEvent
    data class DurationChanged(val minutes: String) : CreateTaskUiEvent
    data class CostChanged(val cost: String) : CreateTaskUiEvent
    data object SubmitTask : CreateTaskUiEvent
}
