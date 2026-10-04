package tech.nikelyh.rumbo.feature.home

import tech.nikelyh.rumbo.core.model.Task

sealed interface HomeUiEvent {
    data object Refresh : HomeUiEvent
    data class OnProcessClick(val processId: String) : HomeUiEvent
    data class OnTaskClick(val taskId: String) : HomeUiEvent
    data class OnToggleTaskStatus(val task: Task) : HomeUiEvent
    data class CompleteTaskWithDuration(val task: Task, val durationMinutes: Long) : HomeUiEvent
    data object OnCreateTaskClick : HomeUiEvent
    data object OnCreateProcessClick : HomeUiEvent
    data object OnLogProgressClick : HomeUiEvent
    data object OnStartSessionClick : HomeUiEvent
}
