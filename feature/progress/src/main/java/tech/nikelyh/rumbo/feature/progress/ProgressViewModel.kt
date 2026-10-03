package tech.nikelyh.rumbo.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import tech.nikelyh.rumbo.core.data.repository.ProcessRepository
import tech.nikelyh.rumbo.core.data.repository.ProgressRepository
import tech.nikelyh.rumbo.core.data.repository.TaskRepository
import tech.nikelyh.rumbo.core.data.repository.WorkSessionRepository
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProgressEntry
import tech.nikelyh.rumbo.core.model.ProgressLevel
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.WorkSession
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    processRepository: ProcessRepository,
    taskRepository: TaskRepository,
    workSessionRepository: WorkSessionRepository,
    progressRepository: ProgressRepository
) : ViewModel() {

    private val selectedTab = MutableStateFlow(AnalyticsTab.PROCESS_ANALYTICS)
    private val selectedTimeframe = MutableStateFlow(TimeframeFilter.DAYS_30)

    val uiState: StateFlow<ProgressUiState> = combine(
        processRepository.getProcesses(),
        taskRepository.getAllTasks(),
        workSessionRepository.getAllWorkSessions(),
        progressRepository.getAllProgressEntries(),
        selectedTab,
        selectedTimeframe
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val processes = flows[0] as List<Process>
        @Suppress("UNCHECKED_CAST")
        val tasks = flows[1] as List<Task>
        @Suppress("UNCHECKED_CAST")
        val sessions = flows[2] as List<WorkSession>
        @Suppress("UNCHECKED_CAST")
        val progressEntries = flows[3] as List<ProgressEntry>
        @Suppress("UNCHECKED_CAST")
        val tab = flows[4] as AnalyticsTab
        @Suppress("UNCHECKED_CAST")
        val timeframe = flows[5] as TimeframeFilter

        val userProcesses = processes.filter { it.id != Process.GENERAL_PROCESS_ID }
        val timeframeCutoffMillis = System.currentTimeMillis() - (timeframe.days * 24L * 60 * 60 * 1000)

        val filteredSessions = sessions.filter { it.startTimeEpochMillis >= timeframeCutoffMillis }
        val filteredProgressEntries = progressEntries.filter { it.dateEpochMillis >= timeframeCutoffMillis }

        val totalTimeInvestedMillis = filteredSessions.sumOf { it.durationMillis }
        val totalSessionsCount = filteredSessions.size
        val totalAccumulatedCost = userProcesses.sumOf { it.accumulatedDirectCost }

        val activeDaysMap = mutableMapOf<String, MutableSet<String>>()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())

        filteredSessions.forEach { session ->
            val dateStr = dateFormatter.format(Instant.ofEpochMilli(session.startTimeEpochMillis))
            activeDaysMap.getOrPut(session.processId) { mutableSetOf() }.add(dateStr)
        }

        val totalActiveDays = activeDaysMap.values.flatten().toSet().size

        val comparisons = userProcesses.map { process ->
            val processSessions = filteredSessions.filter { it.processId == process.id }
            val timeInvestedHours = processSessions.sumOf { it.durationMillis } / (1000.0 * 3600)

            val latestProgressEntry = filteredProgressEntries
                .filter { it.processId == process.id }
                .maxByOrNull { it.dateEpochMillis }

            val declaredLabel = if (latestProgressEntry != null) {
                ProgressLevel.fromValue(latestProgressEntry.progressLevel).label
            } else {
                "Sin registro"
            }

            ProcessProgressComparison(
                processId = process.id,
                processName = process.name,
                colorOrVisualId = process.colorOrVisualId,
                timeInvestedHours = timeInvestedHours,
                declaredProgressLevelLabel = declaredLabel
            )
        }

        val investmentSummaries = userProcesses.map { process ->
            val timeMillis = filteredSessions.filter { it.processId == process.id }.sumOf { it.durationMillis }
            ProcessInvestmentItem(
                processId = process.id,
                processName = process.name,
                colorOrVisualId = process.colorOrVisualId,
                timeInvestedMillis = timeMillis,
                cost = process.accumulatedDirectCost
            )
        }

        val processAnalyticsData = ProcessAnalyticsData(
            totalTimeInvestedMillis = totalTimeInvestedMillis,
            totalSessionsCount = totalSessionsCount,
            totalActiveDays = totalActiveDays,
            totalAccumulatedCost = totalAccumulatedCost,
            comparisons = comparisons,
            processInvestmentSummaries = investmentSummaries,
            activeDaysMap = activeDaysMap
        )

        // Task Analytics Computation (Strictly Separated)
        val completedTasksCount = tasks.count { it.isCompleted }
        val pendingTasksCount = tasks.count { !it.isCompleted }
        val overdueTasksCount = tasks.count { task ->
            val due = task.dueDateEpochMillis
            due != null && due < System.currentTimeMillis() && !task.isCompleted
        }

        val taskAnalyticsData = TaskAnalyticsData(
            completedTasksCount = completedTasksCount,
            pendingTasksCount = pendingTasksCount,
            overdueTasksCount = overdueTasksCount,
            totalTasksCount = tasks.size
        )

        if (userProcesses.isEmpty() && tasks.isEmpty()) {
            ProgressUiState.Empty
        } else {
            ProgressUiState.Content(
                selectedTab = tab,
                selectedTimeframe = timeframe,
                processAnalytics = processAnalyticsData,
                taskAnalytics = taskAnalyticsData,
                availableProcesses = userProcesses,
                recentProgressEntries = filteredProgressEntries.take(5),
                recentWorkSessions = filteredSessions.take(5)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressUiState.Loading
    )

    fun onEvent(event: ProgressUiEvent) {
        when (event) {
            is ProgressUiEvent.TabSelected -> selectedTab.value = event.tab
            is ProgressUiEvent.TimeframeSelected -> selectedTimeframe.value = event.timeframe
        }
    }
}
