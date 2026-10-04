package tech.nikelyh.rumbo.feature.processes

import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProcessSortOrder
import tech.nikelyh.rumbo.core.model.ProcessStatus
import tech.nikelyh.rumbo.core.model.ProcessTypeFilter

/**
 * UI state representation for the processes overview screen.
 */
sealed interface ProcessesUiState {
    data object Loading : ProcessesUiState
    data class Content(
        val activeProcesses: List<Process>,
        val pausedProcesses: List<Process>,
        val completedProcesses: List<Process> = emptyList(),
        val selectedFilter: ProcessStatus = ProcessStatus.ACTIVE,
        val selectedTypeFilter: ProcessTypeFilter = ProcessTypeFilter.ALL,
        val sortOrder: ProcessSortOrder = ProcessSortOrder.RECENT,
        val searchQuery: String = ""
    ) : ProcessesUiState
    data object Empty : ProcessesUiState
    data class Error(val message: String) : ProcessesUiState
}
