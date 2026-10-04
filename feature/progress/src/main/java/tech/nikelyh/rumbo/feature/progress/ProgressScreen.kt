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
        // High Level Process Summary
        item {
            RumboCard(modifier = Modifier.fillMaxWidth()) {
                val totalHours = data.totalTimeInvestedMillis / (1000 * 3600)
                val totalMins = (data.totalTimeInvestedMillis / (1000 * 60)) % 60
                Text(
                    text = "Métricas Generales de Procesos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tiempo total: ${totalHours}h ${totalMins}m  •  Sesiones: ${data.totalSessionsCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Días activos: ${data.totalActiveDays}  •  Costo acumulado: $${data.totalAccumulatedCost}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Contribution Calendar
        item {
            RumboCard(modifier = Modifier.fillMaxWidth()) {
                ContributionCalendar(
                    activeDates = data.activeDaysMap.values.flatten().toSet()
                )
            }
        }

        // Time Invested vs Declared Progress Comparison
        item {
            RumboSectionHeader(
                title = "Tiempo Invertido vs Progreso Declarado",
                subtitle = "Comparación transparente entre dedicación y evaluación cualitativa"
            )
            RumboCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text(
                        text = "El tiempo invertido y el progreso cualitativo son conceptos independientes.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (data.comparisons.isEmpty()) {
            item {
                Text(
                    text = "Sin procesos para comparar.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(data.comparisons, key = { "comparison_${it.processId}" }) { comparison ->
                RumboCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Proceso ${comparison.processName}: ${comparison.timeInvestedHours} horas invertidas, progreso declarado ${comparison.declaredProgressLevelLabel}."
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = comparison.processName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Tiempo invertido: ${String.format(Locale.getDefault(), "%.1f", comparison.timeInvestedHours)}h",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(
                            text = "Progreso: ${comparison.declaredProgressLevelLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Investment Summary Table
        item {
            RumboSectionHeader(title = "Inversión Acumulada por Proceso")
        }

        items(data.processInvestmentSummaries, key = { "summary_${it.processId}" }) { summary ->
            RumboCard(modifier = Modifier.fillMaxWidth()) {
                val hours = summary.timeInvestedMillis / (1000 * 3600)
                val mins = (summary.timeInvestedMillis / (1000 * 60)) % 60
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = summary.processName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Tiempo: ${hours}h ${mins}m  •  Costo: $${summary.cost}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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
        item {
            RumboSectionHeader(
                title = "Métricas de Tareas",
                subtitle = "Desglose exclusivo del estado de tareas"
            )
        }

        item {
            RumboCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tareas Completadas: ${data.completedTasksCount}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Tareas Pendientes: ${data.pendingTasksCount}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Tareas Vencidas: ${data.overdueTasksCount}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Total Registradas: ${data.totalTasksCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
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
