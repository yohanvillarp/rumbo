package tech.nikelyh.rumbo.feature.processes

sealed interface EditProcessUiEvent {
    data class NameChanged(val name: String) : EditProcessUiEvent
    data class DescriptionChanged(val description: String) : EditProcessUiEvent
    data class ColorChanged(val colorOrVisualId: String) : EditProcessUiEvent
    data class DueDateChanged(val millis: Long?) : EditProcessUiEvent
    data object SubmitProcess : EditProcessUiEvent
}
