package tech.nikelyh.rumbo.feature.progress

sealed interface ProgressUiEvent {
    data object Refresh : ProgressUiEvent
}
