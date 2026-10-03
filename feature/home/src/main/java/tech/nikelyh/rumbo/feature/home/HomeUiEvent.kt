package tech.nikelyh.rumbo.feature.home

sealed interface HomeUiEvent {
    data object Refresh : HomeUiEvent
    data class OnProcessClick(val processId: String) : HomeUiEvent
    data class OnTaskClick(val taskId: String) : HomeUiEvent
}
