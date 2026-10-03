package tech.nikelyh.rumbo.feature.tasks

sealed interface TaskDetailUiEvent {
    data object ToggleStatus : TaskDetailUiEvent
    data object DeleteTask : TaskDetailUiEvent
}
