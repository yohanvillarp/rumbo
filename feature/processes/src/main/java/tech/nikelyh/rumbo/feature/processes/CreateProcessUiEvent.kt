package tech.nikelyh.rumbo.feature.processes

sealed interface CreateProcessUiEvent {
    data class NameChanged(val name: String) : CreateProcessUiEvent
    data class DescriptionChanged(val description: String) : CreateProcessUiEvent
    data class ColorChanged(val colorOrVisualId: String) : CreateProcessUiEvent
    data class ParentProcessSelected(val parentId: String?) : CreateProcessUiEvent
    data class DueDateChanged(val millis: Long?) : CreateProcessUiEvent
    data object SubmitProcess : CreateProcessUiEvent
}
