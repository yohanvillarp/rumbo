package tech.nikelyh.rumbo.feature.progress

import tech.nikelyh.rumbo.core.model.ProgressLevel

sealed interface LogProgressUiEvent {
    data class ProcessSelected(val processId: String) : LogProgressUiEvent
    data class LevelSelected(val level: ProgressLevel) : LogProgressUiEvent
    data class NoteChanged(val note: String) : LogProgressUiEvent
    data object SubmitProgress : LogProgressUiEvent
}
