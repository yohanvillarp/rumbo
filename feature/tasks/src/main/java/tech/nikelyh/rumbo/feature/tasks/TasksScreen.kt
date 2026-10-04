package tech.nikelyh.rumbo.feature.tasks

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikelyh.rumbo.core.designsystem.component.RumboButton
import tech.nikelyh.rumbo.core.designsystem.component.RumboEmptyState
import tech.nikelyh.rumbo.core.designsystem.component.RumboLoadingState
import tech.nikelyh.rumbo.core.designsystem.component.RumboSectionHeader
import tech.nikelyh.rumbo.core.designsystem.component.RumboTaskItem
import tech.nikelyh.rumbo.core.designsystem.component.TaskCompletionCelebration
import tech.nikelyh.rumbo.core.designsystem.component.TaskCompletionDurationDialog
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.Priority
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskSortOrder
import tech.nikelyh.rumbo.core.model.TaskStatus

@Composable
fun TasksRoute(
    onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCreateTask: () -> Unit = {},
    onStartSession: (String, String) -> Unit = { _, _ -> },
    viewModel: TasksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TasksScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is TasksUiEvent.OnTaskSelected -> onTaskClick(event.taskId)
                else -> viewModel.onEvent(event)
            }
        },
        onCreateTaskClick = onNavigateToCreateTask,
        onStartSession = onStartSession,
        modifier = modifier
    )
}

@Composable
internal fun TasksScreen(
    uiState: TasksUiState,
    onEvent: (TasksUiEvent) -> Unit,
    onCreateTaskClick: () -> Unit,
    onStartSession: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    when (uiState) {
        TasksUiState.Loading -> {
            RumboLoadingState(isLoading = true, modifier = modifier)
        }
        is TasksUiState.Error -> {
            RumboEmptyState(message = uiState.message, modifier = modifier)
        }
        TasksUiState.Empty -> {
            RumboEmptyState(
                message = "Sin tareas por ahora",
                subtitle = "Agrega una tarea para saber exactamente qué hacer hoy o en los próximos días.",
                icon = Icons.AutoMirrored.Filled.Assignment,
                actionLabel = "Crear Tarea",
                onActionClick = onCreateTaskClick,
                modifier = modifier
            )
        }
        is TasksUiState.Content -> {
            TasksContent(
                uiState = uiState,
                onEvent = onEvent,
                onCreateTaskClick = onCreateTaskClick,
                onStartSession = onStartSession,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun TasksContent(
    uiState: TasksUiState.Content,
    onEvent: (TasksUiEvent) -> Unit,
    onCreateTaskClick: () -> Unit,
    onStartSession: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var taskToCompleteWithDuration by remember { mutableStateOf<Task?>(null) }
    var celebratingTaskTitle by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { onEvent(TasksUiEvent.SearchQueryChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar tareas...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            singleLine = true,
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status Filter Row (Pending, Today, Overdue, Completed, All)
        TaskStatusFilterRow(
            selectedFilter = uiState.selectedFilter,
            onFilterSelected = { filter -> onEvent(TasksUiEvent.FilterChanged(filter)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Secondary Criteria Filter Row (Sorting, Priority, Process filter)
        TaskSecondaryFilterRow(
            taskSortOrder = uiState.taskSortOrder,
            onSortOrderSelected = { order -> onEvent(TasksUiEvent.SortOrderChanged(order)) },
            selectedPriority = uiState.selectedPriority,
            onPriorityToggle = {
                val nextPriority = if (uiState.selectedPriority == Priority.HIGH) null else Priority.HIGH
                onEvent(TasksUiEvent.PriorityFilterChanged(nextPriority))
            },
            availableProcesses = uiState.availableProcesses,
            selectedProcessId = uiState.selectedProcessId,
            onProcessSelected = { processId -> onEvent(TasksUiEvent.ProcessFilterChanged(processId)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tasks List
        if (uiState.tasks.isEmpty()) {
            RumboEmptyState(
                message = "No hay tareas en este filtro",
                icon = Icons.AutoMirrored.Filled.Assignment,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize()
            ) {
                items(uiState.tasks, key = { it.id }) { taskItem ->
                    val taskProcess = uiState.availableProcesses.find { it.id == taskItem.processId }
                    RumboTaskItem(
                        task = taskItem,
                        processColorOrVisualId = taskProcess?.colorOrVisualId,
                        processName = taskProcess?.name,
                        onToggleStatus = { task ->
                            if (!task.isCompleted) {
                                taskToCompleteWithDuration = task
                            } else {
                                onEvent(TasksUiEvent.ToggleTaskStatus(task))
                            }
                        },
                        onClick = { onEvent(TasksUiEvent.OnTaskSelected(taskItem.id)) },
                        onStartSession = { task -> onStartSession(task.id, task.processId) }
                    )
                }
            }
        }

        if (taskToCompleteWithDuration != null) {
            val task = taskToCompleteWithDuration!!
            val sessionMinutes = if (task.timeWorkedMillis > 0L) {
                (task.timeWorkedMillis + 59_999L) / 60_000L
            } else 0L
            val minMinutes = if (sessionMinutes > 0L) sessionMinutes else 1L
            TaskCompletionDurationDialog(
                taskTitle = task.title,
                initialMinutes = if (sessionMinutes > 0L) sessionMinutes else 0L,
                minMinutes = minMinutes,
                onConfirm = { minutes ->
                    celebratingTaskTitle = task.title
                    onEvent(TasksUiEvent.CompleteTaskWithDuration(task, minutes))
                    taskToCompleteWithDuration = null
                },
                onDismiss = { taskToCompleteWithDuration = null }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        RumboButton(
            onClick = onCreateTaskClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nueva Tarea")
        }
    }

    if (celebratingTaskTitle != null) {
        TaskCompletionCelebration(
            taskTitle = celebratingTaskTitle!!,
            onDismiss = { celebratingTaskTitle = null }
        )
    }
}
}

/**
 * Renders horizontally scrollable filter chips for task completion status and deadlines.
 *
 * @param selectedFilter Currently active [TaskFilter].
 * @param onFilterSelected Callback invoked when a status filter chip is clicked.
 * @param modifier Optional [Modifier] for layout adjustments.
 */
@Composable
private fun TaskStatusFilterRow(
    selectedFilter: TaskFilter,
    onFilterSelected: (TaskFilter) -> Unit,
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
            selected = selectedFilter == TaskFilter.PENDING,
            onClick = { onFilterSelected(TaskFilter.PENDING) },
            label = { Text("Pendientes") }
        )
        FilterChip(
            selected = selectedFilter == TaskFilter.TODAY,
            onClick = { onFilterSelected(TaskFilter.TODAY) },
            label = { Text("Para hoy") }
        )
        FilterChip(
            selected = selectedFilter == TaskFilter.OVERDUE,
            onClick = { onFilterSelected(TaskFilter.OVERDUE) },
            label = { Text("Vencidas") }
        )
        FilterChip(
            selected = selectedFilter == TaskFilter.COMPLETED,
            onClick = { onFilterSelected(TaskFilter.COMPLETED) },
            label = { Text("Completadas") }
        )
        FilterChip(
            selected = selectedFilter == TaskFilter.ALL,
            onClick = { onFilterSelected(TaskFilter.ALL) },
            label = { Text("Todas") }
        )
    }
}

/**
 * Renders horizontally scrollable filter chips for task sorting criteria, priority filtering,
 * and contextual parent process selection.
 *
 * @param taskSortOrder Current [TaskSortOrder] applied to the list.
 * @param onSortOrderSelected Callback invoked when a sort order chip is selected.
 * @param selectedPriority Active [Priority] filter, or null if unfiltered.
 * @param onPriorityToggle Callback invoked to toggle high-priority filtering.
 * @param availableProcesses List of available processes for contextual filtering.
 * @param selectedProcessId Currently selected process ID filter, or null if all processes are included.
 * @param onProcessSelected Callback invoked with the selected process ID or null to reset.
 * @param modifier Optional [Modifier] for layout adjustments.
 */
@Composable
private fun TaskSecondaryFilterRow(
    taskSortOrder: TaskSortOrder,
    onSortOrderSelected: (TaskSortOrder) -> Unit,
    selectedPriority: Priority?,
    onPriorityToggle: () -> Unit,
    availableProcesses: List<tech.nikelyh.rumbo.core.model.Process>,
    selectedProcessId: String?,
    onProcessSelected: (String?) -> Unit,
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
            selected = taskSortOrder == TaskSortOrder.DUE_DATE,
            onClick = { onSortOrderSelected(TaskSortOrder.DUE_DATE) },
            label = { Text("Próximas a vencer") }
        )
        FilterChip(
            selected = taskSortOrder == TaskSortOrder.RECENT,
            onClick = { onSortOrderSelected(TaskSortOrder.RECENT) },
            label = { Text("Más recientes") }
        )
        FilterChip(
            selected = taskSortOrder == TaskSortOrder.PRIORITY,
            onClick = { onSortOrderSelected(TaskSortOrder.PRIORITY) },
            label = { Text("Mayor prioridad") }
        )

        FilterChip(
            selected = selectedPriority == Priority.HIGH,
            onClick = onPriorityToggle,
            label = { Text("Solo Alta") }
        )

        availableProcesses.filter { it.id != "general" }.forEach { proc ->
            FilterChip(
                selected = selectedProcessId == proc.id,
                onClick = {
                    val nextProc = if (selectedProcessId == proc.id) null else proc.id
                    onProcessSelected(nextProc)
                },
                label = { Text(proc.name) }
            )
        }
    }
}

@Preview(name = "Tasks Light", showBackground = true)
@Composable
private fun TasksScreenPreviewLight() {
    RumboTheme(darkTheme = false) {
        TasksScreen(
            uiState = TasksUiState.Content(
                tasks = listOf(
                    Task(
                        id = "t1",
                        processId = "general",
                        title = "Diseñar componentes en :core:designsystem",
                        description = "Garantizar accesibilidad y previews",
                        status = TaskStatus.PENDING,
                        priority = Priority.HIGH,
                        createdAtEpochMillis = 1000L,
                        cost = 0.0
                    )
                ),
                availableProcesses = emptyList()
            ),
            onEvent = {},
            onCreateTaskClick = {}
        )
    }
}
