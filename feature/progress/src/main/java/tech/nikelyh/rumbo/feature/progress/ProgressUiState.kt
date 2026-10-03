package tech.nikelyh.rumbo.feature.progress

import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProgressEntry
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.WorkSession

data class ProcessProgressComparison(
    val processId: String,
    val processName: String,
    val colorOrVisualId: String,
    val timeInvestedHours: Double,
    val declaredProgressLevelLabel: String
)

data class TaskAnalyticsData(
    val completedTasksCount: Int,
    val pendingTasksCount: Int,
    val overdueTasksCount: Int,
    val totalTasksCount: Int
)

data class ProcessAnalyticsData(
    val totalTimeInvestedMillis: Long,
    val totalSessionsCount: Int,
    val totalActiveDays: Int,
    val totalAccumulatedCost: Double,
    val comparisons: List<ProcessProgressComparison>,
    val processInvestmentSummaries: List<ProcessInvestmentItem>,
    val activeDaysMap: Map<String, Set<String>> // processId -> set of YYYY-MM-DD
)

data class ProcessInvestmentItem(
    val processId: String,
    val processName: String,
    val colorOrVisualId: String,
    val timeInvestedMillis: Long,
    val cost: Double
)

sealed interface ProgressUiState {
    data object Loading : ProgressUiState
    data class Content(
        val selectedTab: AnalyticsTab = AnalyticsTab.PROCESS_ANALYTICS,
        val selectedTimeframe: TimeframeFilter = TimeframeFilter.DAYS_30,
        val processAnalytics: ProcessAnalyticsData,
        val taskAnalytics: TaskAnalyticsData,
        val availableProcesses: List<Process>,
        val recentProgressEntries: List<ProgressEntry>,
        val recentWorkSessions: List<WorkSession>
    ) : ProgressUiState
    data object Empty : ProgressUiState
    data class Error(val message: String) : ProgressUiState
}
