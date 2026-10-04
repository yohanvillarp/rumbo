package tech.nikelyh.rumbo.feature.progress

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import tech.nikelyh.rumbo.core.designsystem.component.RumboCard
import tech.nikelyh.rumbo.core.designsystem.component.RumboEmptyState
import tech.nikelyh.rumbo.core.designsystem.component.RumboLoadingState
import tech.nikelyh.rumbo.core.designsystem.component.RumboSectionHeader
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.feature.progress.chart.ProcessComparisonCard
import tech.nikelyh.rumbo.feature.progress.chart.ProcessDistributionDonutChart
import tech.nikelyh.rumbo.feature.progress.chart.ProcessInvestmentBarChart
import tech.nikelyh.rumbo.feature.progress.chart.ProcessMetricTiles
import tech.nikelyh.rumbo.feature.progress.chart.TaskCompletionGauge
import tech.nikelyh.rumbo.feature.progress.chart.TaskDistributionBar
import tech.nikelyh.rumbo.feature.progress.chart.TaskMetricTiles
import java.util.Locale

@Composable
fun ProgressRoute(
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProgressScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
internal fun ProgressScreen(
    uiState: ProgressUiState,
    onEvent: (ProgressUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        ProgressUiState.Loading -> {
            RumboLoadingState(isLoading = true, modifier = modifier)
        }
        is ProgressUiState.Error -> {
            RumboEmptyState(message = uiState.message, modifier = modifier)
        }
        ProgressUiState.Empty -> {
            RumboEmptyState(
                message = "Sin registros de progreso",
                subtitle = "Inicia sesiones de trabajo o evalúa tu progreso para generar analíticas.",
                icon = Icons.Default.BarChart,
                modifier = modifier
            )
        }
        is ProgressUiState.Content -> {
            ProgressContent(
                uiState = uiState,
                onEvent = onEvent,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ProgressContent(
    uiState: ProgressUiState.Content,
    onEvent: (ProgressUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = uiState.selectedTab.ordinal) {
        AnalyticsTab.entries.size
    }

    LaunchedEffect(pagerState.currentPage) {
        onEvent(ProgressUiEvent.TabSelected(AnalyticsTab.entries[pagerState.currentPage]))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Selector (Swipeable & Clickable)
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth()
        ) {
            AnalyticsTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(tab.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Timeframe Filter (7 días, 30 días, 90 días)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeframeFilter.entries.forEach { timeframe ->
                FilterChip(
                    selected = uiState.selectedTimeframe == timeframe,
                    onClick = { onEvent(ProgressUiEvent.TimeframeSelected(timeframe)) },
                    label = { Text(timeframe.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Pager for Swipeable Tabs
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (AnalyticsTab.entries[page]) {
                AnalyticsTab.PROCESS_ANALYTICS -> {
                    ProcessAnalyticsView(data = uiState.processAnalytics)
                }
                AnalyticsTab.TASK_ANALYTICS -> {
                    TaskAnalyticsView(data = uiState.taskAnalytics)
                }
            }
        }
    }
}

@Composable
private fun ProcessAnalyticsView(
    data: ProcessAnalyticsData
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // High Level Metric Stat Tiles
        item {
            ProcessMetricTiles(data = data)
        }

        // Process Time Distribution Donut Chart
        if (data.processInvestmentSummaries.isNotEmpty()) {
            item {
                RumboSectionHeader(
                    title = "Distribución del Tiempo",
                    subtitle = "Proporción de horas dedicadas por cada proceso"
                )
            }
            item {
                RumboCard(modifier = Modifier.fillMaxWidth()) {
                    ProcessDistributionDonutChart(items = data.processInvestmentSummaries)
                }
            }

            // Process Investment Comparative Bar Chart
            item {
                RumboSectionHeader(
                    title = "Inversión por Proceso",
                    subtitle = "Comparativa de horas acumuladas y costos"
                )
            }
            item {
                RumboCard(modifier = Modifier.fillMaxWidth()) {
                    ProcessInvestmentBarChart(items = data.processInvestmentSummaries)
                }
            }
        }

        // Time Invested vs Declared Progress Comparison
        item {
            RumboSectionHeader(
                title = "Tiempo Invertido vs Progreso Declarado",
                subtitle = "Comparación entre dedicación temporal y nivel cualitativo"
            )
        }

        if (data.comparisons.isEmpty()) {
            item {
                Text(
                    text = "Sin procesos para comparar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            items(data.comparisons, key = { "comparison_${it.processId}" }) { comparison ->
                ProcessComparisonCard(comparison = comparison)
            }
        }

        // Activity Calendar
        item {
            RumboSectionHeader(
                title = "Consistencia y Actividad",
                subtitle = "Días con sesiones registradas en el período"
            )
        }
        item {
            RumboCard(modifier = Modifier.fillMaxWidth()) {
                ContributionCalendar(
                    activeDates = data.activeDaysMap.values.flatten().toSet()
                )
            }
        }
    }
}

@Composable
private fun TaskAnalyticsView(
    data: TaskAnalyticsData
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // High Level Task Metric Tiles
        item {
            TaskMetricTiles(data = data)
        }

        // Task Completion Circular Gauge
        item {
            RumboSectionHeader(
                title = "Tasa de Finalización",
                subtitle = "Porcentaje de tareas completadas del total"
            )
        }
        item {
            RumboCard(modifier = Modifier.fillMaxWidth()) {
                TaskCompletionGauge(data = data)
            }
        }

        // Task Status Proportional Distribution Bar
        item {
            RumboSectionHeader(
                title = "Distribución de Estados",
                subtitle = "Proporción entre completadas, pendientes y vencidas"
            )
        }
        item {
            RumboCard(modifier = Modifier.fillMaxWidth()) {
                TaskDistributionBar(data = data)
            }
        }
    }
}

@Preview(name = "Progress Light", showBackground = true)
@Composable
private fun ProgressScreenPreviewLight() {
    RumboTheme(darkTheme = false) {
        ProgressScreen(
            uiState = ProgressUiState.Content(
                selectedTab = AnalyticsTab.PROCESS_ANALYTICS,
                selectedTimeframe = TimeframeFilter.DAYS_30,
                processAnalytics = ProcessAnalyticsData(
                    totalTimeInvestedMillis = 3600000L * 12,
                    totalSessionsCount = 8,
                    totalActiveDays = 5,
                    totalAccumulatedCost = 350.0,
                    comparisons = listOf(
                        ProcessProgressComparison(
                            processId = "p1",
                            processName = "Desarrollo de Rumbo",
                            colorOrVisualId = "teal",
                            timeInvestedHours = 12.0,
                            declaredProgressLevelLabel = "Alto"
                        )
                    ),
                    processInvestmentSummaries = listOf(
                        ProcessInvestmentItem(
                            processId = "p1",
                            processName = "Desarrollo de Rumbo",
                            colorOrVisualId = "teal",
                            timeInvestedMillis = 3600000L * 12,
                            cost = 350.0
                        )
                    ),
                    activeDaysMap = mapOf("p1" to setOf("2026-10-01", "2026-10-02"))
                ),
                taskAnalytics = TaskAnalyticsData(
                    completedTasksCount = 14,
                    pendingTasksCount = 5,
                    overdueTasksCount = 1,
                    totalTasksCount = 19
                ),
                availableProcesses = emptyList(),
                recentProgressEntries = emptyList(),
                recentWorkSessions = emptyList()
            ),
            onEvent = {}
        )
    }
}
