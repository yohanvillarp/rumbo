package tech.nikelyh.rumbo.feature.tasks

import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.WorkSession

sealed interface TaskDetailUiState {
    data object Loading : TaskDetailUiState
    data class Content(
        val task: Task,
        val processName: String,
        val sessions: List<WorkSession> = emptyList(),
        val hasStartedSession: Boolean = false,
        val activeSessionAccumulatedMillis: Long = 0L,
        val isActiveSessionRunning: Boolean = false,
        val hasActiveSession: Boolean = false
    ) : TaskDetailUiState
    data class Error(val message: String) : TaskDetailUiState
}
