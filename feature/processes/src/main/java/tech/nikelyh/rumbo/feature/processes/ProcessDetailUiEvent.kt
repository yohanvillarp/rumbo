package tech.nikelyh.rumbo.feature.processes

import tech.nikelyh.rumbo.core.model.Milestone
import tech.nikelyh.rumbo.core.model.Task

sealed interface ProcessDetailUiEvent {
    data object PauseProcess : ProcessDetailUiEvent
    data object ResumeProcess : ProcessDetailUiEvent
    data object FinishProcess : ProcessDetailUiEvent
    data object ArchiveProcess : ProcessDetailUiEvent
    data class ToggleTaskStatus(val task: Task) : ProcessDetailUiEvent
    data class ToggleMilestoneStatus(val milestone: Milestone) : ProcessDetailUiEvent
}
