package tech.nikelyh.rumbo.feature.processes

import tech.nikelyh.rumbo.core.model.ProcessStatus

sealed interface ProcessesUiEvent {
    data class SearchQueryChanged(val query: String) : ProcessesUiEvent
    data class FilterChanged(val status: ProcessStatus) : ProcessesUiEvent
    data class OnProcessSelected(val processId: String) : ProcessesUiEvent
}
