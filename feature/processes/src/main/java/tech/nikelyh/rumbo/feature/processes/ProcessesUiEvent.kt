package tech.nikelyh.rumbo.feature.processes

import tech.nikelyh.rumbo.core.model.Process

sealed interface ProcessesUiEvent {
    data class SearchQueryChanged(val query: String) : ProcessesUiEvent
    data class CreateProcess(val process: Process) : ProcessesUiEvent
    data class DeleteProcess(val processId: String) : ProcessesUiEvent
    data class ProcessSelected(val processId: String) : ProcessesUiEvent
}
