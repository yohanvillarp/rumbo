package tech.nikelyh.rumbo.feature.processes

import tech.nikelyh.rumbo.core.model.Milestone
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProgressEntry
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.WeeklyGoal
import tech.nikelyh.rumbo.core.model.WorkSession

sealed interface ProcessDetailUiState {
    data object Loading : ProcessDetailUiState
    data class Content(
        val process: Process,
        val pendingTasks: List<Task>,
        val completedTasks: List<Task>,
        val milestones: List<Milestone>,
        val workSessions: List<WorkSession>,
        val totalTimeInvestedMillis: Long,
        val progressEntries: List<ProgressEntry>,
        val weeklyGoal: WeeklyGoal? = null,
        val subProcesses: List<Process> = emptyList(),
        val parentProcess: Process? = null,
        val completionBlockedReason: String? = null,
        val userMessage: String? = null,
        val isDeleted: Boolean = false,
        val taskSortOrder: tech.nikelyh.rumbo.core.model.TaskSortOrder = tech.nikelyh.rumbo.core.model.TaskSortOrder.DUE_DATE
    ) : ProcessDetailUiState
    data class Error(val message: String) : ProcessDetailUiState
    data object Deleted : ProcessDetailUiState
}
