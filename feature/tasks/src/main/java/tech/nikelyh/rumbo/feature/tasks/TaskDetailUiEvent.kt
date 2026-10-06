package tech.nikelyh.rumbo.feature.tasks

sealed interface TaskDetailUiEvent {
    data object ToggleStatus : TaskDetailUiEvent
    data class CompleteWithDuration(val durationMinutes: Long) : TaskDetailUiEvent
    data object DeleteTask : TaskDetailUiEvent
    data class DeleteSession(val sessionId: String) : TaskDetailUiEvent
}
