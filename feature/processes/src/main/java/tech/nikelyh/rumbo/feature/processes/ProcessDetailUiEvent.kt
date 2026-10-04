package tech.nikelyh.rumbo.feature.processes

import tech.nikelyh.rumbo.core.model.Milestone
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskSortOrder

/**
 * User interaction events dispatched from the process detail screen.
 */
sealed interface ProcessDetailUiEvent {
    data object PauseProcess : ProcessDetailUiEvent
    data object ResumeProcess : ProcessDetailUiEvent
    data object FinishProcess : ProcessDetailUiEvent
    data object ReopenProcess : ProcessDetailUiEvent
    data object ArchiveProcess : ProcessDetailUiEvent
    data object DismissUserMessage : ProcessDetailUiEvent
    data class ToggleTaskStatus(val task: Task) : ProcessDetailUiEvent
    data class CompleteTaskWithDuration(val task: Task, val durationMinutes: Long) : ProcessDetailUiEvent
    data class ToggleMilestoneStatus(val milestone: Milestone) : ProcessDetailUiEvent
    data class SaveWeeklyGoal(val description: String) : ProcessDetailUiEvent
    data class CompleteWeeklyGoal(val goalId: String) : ProcessDetailUiEvent
    data class CarryOverWeeklyGoal(val goalId: String) : ProcessDetailUiEvent
    data class DiscardWeeklyGoal(val goalId: String) : ProcessDetailUiEvent
    data class ChangeTaskSortOrder(val order: tech.nikelyh.rumbo.core.model.TaskSortOrder) : ProcessDetailUiEvent
    data object ToggleStar : ProcessDetailUiEvent
}
