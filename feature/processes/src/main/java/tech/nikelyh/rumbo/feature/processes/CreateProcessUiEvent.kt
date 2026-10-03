package tech.nikelyh.rumbo.feature.processes

sealed interface CreateProcessUiEvent {
    data class NameChanged(val name: String) : CreateProcessUiEvent
    data class DescriptionChanged(val description: String) : CreateProcessUiEvent
    data class ColorChanged(val colorOrVisualId: String) : CreateProcessUiEvent
    data class CostChanged(val cost: String) : CreateProcessUiEvent
    data class NextActionChanged(val nextAction: String) : CreateProcessUiEvent
    data object SubmitProcess : CreateProcessUiEvent
}
