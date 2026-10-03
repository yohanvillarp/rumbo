package tech.nikelyh.rumbo.feature.progress

import tech.nikelyh.rumbo.core.model.ProgressLevel

sealed interface StartSessionUiEvent {
    data class ProcessSelected(val processId: String) : StartSessionUiEvent
    data class TaskSelected(val taskId: String?) : StartSessionUiEvent
    data object ToggleTimer : StartSessionUiEvent
    data object FinishTimer : StartSessionUiEvent
    data class NoteChanged(val note: String) : StartSessionUiEvent
    data class ToggleSaveProgress(val save: Boolean) : StartSessionUiEvent
    data class ProgressLevelSelected(val level: ProgressLevel) : StartSessionUiEvent
    data object SubmitSession : StartSessionUiEvent
}
