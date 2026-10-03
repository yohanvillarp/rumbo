package tech.nikelyh.rumbo.feature.progress

sealed interface ProgressUiEvent {
    data class TabSelected(val tab: AnalyticsTab) : ProgressUiEvent
    data class TimeframeSelected(val timeframe: TimeframeFilter) : ProgressUiEvent
}
