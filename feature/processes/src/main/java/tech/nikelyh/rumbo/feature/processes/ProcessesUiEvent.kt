package tech.nikelyh.rumbo.feature.processes

import tech.nikelyh.rumbo.core.model.ProcessSortOrder
import tech.nikelyh.rumbo.core.model.ProcessStatus
import tech.nikelyh.rumbo.core.model.ProcessTypeFilter

/**
 * User interaction events dispatched from the processes overview screen.
 */
sealed interface ProcessesUiEvent {
    data class SearchQueryChanged(val query: String) : ProcessesUiEvent
    data class FilterChanged(val status: ProcessStatus) : ProcessesUiEvent
    data class TypeFilterChanged(val typeFilter: ProcessTypeFilter) : ProcessesUiEvent
    data class SortOrderChanged(val sortOrder: ProcessSortOrder) : ProcessesUiEvent
    data class OnProcessSelected(val processId: String) : ProcessesUiEvent
}
