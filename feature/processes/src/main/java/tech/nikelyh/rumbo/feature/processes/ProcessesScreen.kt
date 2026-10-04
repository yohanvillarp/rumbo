package tech.nikelyh.rumbo.feature.processes

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikelyh.rumbo.core.designsystem.component.MascotState
import tech.nikelyh.rumbo.core.designsystem.component.RumboButton
import tech.nikelyh.rumbo.core.designsystem.component.RumboEmptyState
import tech.nikelyh.rumbo.core.designsystem.component.RumboLoadingState
import tech.nikelyh.rumbo.core.designsystem.component.RumboProcessCard
import tech.nikelyh.rumbo.core.designsystem.component.RumboSectionHeader
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProcessSortOrder
import tech.nikelyh.rumbo.core.model.ProcessStatus
import tech.nikelyh.rumbo.core.model.ProcessTypeFilter

@Composable
fun ProcessesRoute(
    onProcessClick: (String) -> Unit,
    onNavigateToCreateProcess: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProcessesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProcessesScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is ProcessesUiEvent.OnProcessSelected -> onProcessClick(event.processId)
                else -> viewModel.onEvent(event)
            }
        },
        onCreateProcessClick = onNavigateToCreateProcess,
        modifier = modifier
    )
}

@Composable
internal fun ProcessesScreen(
    uiState: ProcessesUiState,
    onEvent: (ProcessesUiEvent) -> Unit,
    onCreateProcessClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        ProcessesUiState.Loading -> {
            RumboLoadingState(isLoading = true, modifier = modifier)
        }
        is ProcessesUiState.Error -> {
            RumboEmptyState(message = uiState.message, modifier = modifier)
        }
        ProcessesUiState.Empty -> {
            RumboEmptyState(
                message = "Aún no tienes procesos",
                subtitle = "Crea un proceso para dividir un proyecto grande en tareas más sencillas.",
                mascotState = MascotState.DEFAULT,
                actionLabel = "Crear Proceso",
                onActionClick = onCreateProcessClick,
                modifier = modifier
            )
        }
        is ProcessesUiState.Content -> {
            ProcessesContent(
                uiState = uiState,
                onEvent = onEvent,
                onCreateProcessClick = onCreateProcessClick,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ProcessesContent(
    uiState: ProcessesUiState.Content,
    onEvent: (ProcessesUiEvent) -> Unit,
    onCreateProcessClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header & Create Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RumboSectionHeader(
                title = "Procesos",
                subtitle = "Tus metas y proyectos en marcha"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { onEvent(ProcessesUiEvent.SearchQueryChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar procesos...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status Filter Row (Active, Paused, Completed)
        ProcessStatusFilterRow(
            selectedFilter = uiState.selectedFilter,
            activeCount = uiState.activeProcesses.size,
            pausedCount = uiState.pausedProcesses.size,
            completedCount = uiState.completedProcesses.size,
            onFilterSelected = { status -> onEvent(ProcessesUiEvent.FilterChanged(status)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Secondary Filters: Hierarchy Type & Sorting Criteria
        ProcessSecondaryFilterRow(
            selectedTypeFilter = uiState.selectedTypeFilter,
            onTypeFilterSelected = { typeFilter -> onEvent(ProcessesUiEvent.TypeFilterChanged(typeFilter)) },
            sortOrder = uiState.sortOrder,
            onSortOrderSelected = { order -> onEvent(ProcessesUiEvent.SortOrderChanged(order)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Process List
        val displayList = when (uiState.selectedFilter) {
            ProcessStatus.ACTIVE -> uiState.activeProcesses
            ProcessStatus.PAUSED -> uiState.pausedProcesses
            ProcessStatus.COMPLETED, ProcessStatus.ARCHIVED -> uiState.completedProcesses
        }

        if (displayList.isEmpty()) {
            val emptyMessage = when (uiState.selectedFilter) {
                ProcessStatus.ACTIVE -> "Sin procesos activos"
                ProcessStatus.PAUSED -> "Sin procesos pausados"
                else -> "Sin procesos completados"
            }
            RumboEmptyState(
                message = emptyMessage,
                subtitle = "Usa el botón inferior para crear un nuevo proceso.",
                mascotState = MascotState.RESTING,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize()
            ) {
                items(displayList, key = { it.id }) { processItem ->
                    RumboProcessCard(
                        process = processItem,
                        onClick = { onEvent(ProcessesUiEvent.OnProcessSelected(processItem.id)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        RumboButton(
            onClick = onCreateProcessClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nuevo Proceso")
        }
    }
}

/**
 * Horizontally scrollable status filter chips for processes (Active, Paused, Completed).
 *
 * @param selectedFilter Currently active [ProcessStatus].
 * @param activeCount Number of active processes available.
 * @param pausedCount Number of paused processes available.
 * @param completedCount Number of completed processes available.
 * @param onFilterSelected Callback invoked when a status filter chip is clicked.
 * @param modifier Optional [Modifier] for layout adjustments.
 */
@Composable
private fun ProcessStatusFilterRow(
    selectedFilter: ProcessStatus,
    activeCount: Int,
    pausedCount: Int,
    completedCount: Int,
    onFilterSelected: (ProcessStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        FilterChip(
            selected = selectedFilter == ProcessStatus.ACTIVE,
            onClick = { onFilterSelected(ProcessStatus.ACTIVE) },
            label = { Text("Activos ($activeCount)") }
        )
        FilterChip(
            selected = selectedFilter == ProcessStatus.PAUSED,
            onClick = { onFilterSelected(ProcessStatus.PAUSED) },
            label = { Text("Pausados ($pausedCount)") }
        )
        FilterChip(
            selected = selectedFilter == ProcessStatus.COMPLETED,
            onClick = { onFilterSelected(ProcessStatus.COMPLETED) },
            label = { Text("Completados ($completedCount)") }
        )
    }
}

/**
 * Horizontally scrollable secondary filter chips for process hierarchy type and sort ordering.
 *
 * @param selectedTypeFilter Currently active [ProcessTypeFilter] (All, Main, Subprocess).
 * @param onTypeFilterSelected Callback invoked when a process hierarchy type is selected.
 * @param sortOrder Currently active [ProcessSortOrder] (Recent, Name, Accumulated Cost).
 * @param onSortOrderSelected Callback invoked when a sort order is selected.
 * @param modifier Optional [Modifier] for layout adjustments.
 */
@Composable
private fun ProcessSecondaryFilterRow(
    selectedTypeFilter: ProcessTypeFilter,
    onTypeFilterSelected: (ProcessTypeFilter) -> Unit,
    sortOrder: ProcessSortOrder,
    onSortOrderSelected: (ProcessSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        FilterChip(
            selected = selectedTypeFilter == ProcessTypeFilter.ALL,
            onClick = { onTypeFilterSelected(ProcessTypeFilter.ALL) },
            label = { Text("Todos") }
        )
        FilterChip(
            selected = selectedTypeFilter == ProcessTypeFilter.MAIN,
            onClick = { onTypeFilterSelected(ProcessTypeFilter.MAIN) },
            label = { Text("Principales") }
        )
        FilterChip(
            selected = selectedTypeFilter == ProcessTypeFilter.SUBPROCESS,
            onClick = { onTypeFilterSelected(ProcessTypeFilter.SUBPROCESS) },
            label = { Text("Subprocesos") }
        )
        FilterChip(
            selected = sortOrder == ProcessSortOrder.RECENT,
            onClick = { onSortOrderSelected(ProcessSortOrder.RECENT) },
            label = { Text("Más recientes") }
        )
        FilterChip(
            selected = sortOrder == ProcessSortOrder.NAME,
            onClick = { onSortOrderSelected(ProcessSortOrder.NAME) },
            label = { Text("Alfabético (A-Z)") }
        )
        FilterChip(
            selected = sortOrder == ProcessSortOrder.ACCUMULATED_COST,
            onClick = { onSortOrderSelected(ProcessSortOrder.ACCUMULATED_COST) },
            label = { Text("Mayor inversión") }
        )
    }
}

@Preview(name = "Processes Light", showBackground = true)
@Composable
private fun ProcessesScreenPreviewLight() {
    RumboTheme(darkTheme = false) {
        ProcessesScreen(
            uiState = ProcessesUiState.Content(
                activeProcesses = listOf(
                    Process(
                        id = "p1",
                        name = "Proyecto Jardinería",
                        description = "Renovación del jardín y siembra de plantas ornamentales.",
                        status = ProcessStatus.ACTIVE,
                        createdAtEpochMillis = 1000L,
                        colorOrVisualId = "teal",
                        nextAction = "Comprar tierra y macetas"
                    )
                ),
                pausedProcesses = emptyList()
            ),
            onEvent = {},
            onCreateProcessClick = {}
        )
    }
}
