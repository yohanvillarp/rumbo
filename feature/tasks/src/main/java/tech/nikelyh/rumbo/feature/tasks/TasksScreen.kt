package tech.nikelyh.rumbo.feature.tasks

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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        RumboSectionHeader(
            title = "Tareas",
            subtitle = "Tus pasos y actividades del día a día"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { onEvent(TasksUiEvent.SearchQueryChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar tareas...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Tabs (Status: Pendientes, Para hoy, Vencidas, Completadas, Todas)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            FilterChip(
                selected = uiState.selectedFilter == TaskFilter.PENDING,
                onClick = { onEvent(TasksUiEvent.FilterChanged(TaskFilter.PENDING)) },
                label = { Text("Pendientes") }
            )
            FilterChip(
                selected = uiState.selectedFilter == TaskFilter.TODAY,
                onClick = { onEvent(TasksUiEvent.FilterChanged(TaskFilter.TODAY)) },
                label = { Text("Para hoy") }
            )
            FilterChip(
                selected = uiState.selectedFilter == TaskFilter.OVERDUE,
                onClick = { onEvent(TasksUiEvent.FilterChanged(TaskFilter.OVERDUE)) },
                label = { Text("Vencidas") }
            )
            FilterChip(
                selected = uiState.selectedFilter == TaskFilter.COMPLETED,
                onClick = { onEvent(TasksUiEvent.FilterChanged(TaskFilter.COMPLETED)) },
                label = { Text("Completadas") }
            )
            FilterChip(
                selected = uiState.selectedFilter == TaskFilter.ALL,
                onClick = { onEvent(TasksUiEvent.FilterChanged(TaskFilter.ALL)) },
                label = { Text("Todas") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Secondary Filters: Sorting criteria & Process/Priority filters
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            FilterChip(
                selected = uiState.taskSortOrder == TaskSortOrder.DUE_DATE,
                onClick = { onEvent(TasksUiEvent.SortOrderChanged(TaskSortOrder.DUE_DATE)) },
                label = { Text("Próximas a vencer") }
            )
            FilterChip(
                selected = uiState.taskSortOrder == TaskSortOrder.RECENT,
                onClick = { onEvent(TasksUiEvent.SortOrderChanged(TaskSortOrder.RECENT)) },
                label = { Text("Más recientes") }
            )
            FilterChip(
                selected = uiState.taskSortOrder == TaskSortOrder.PRIORITY,
                onClick = { onEvent(TasksUiEvent.SortOrderChanged(TaskSortOrder.PRIORITY)) },
                label = { Text("Mayor prioridad") }
            )

            // Priority filter toggle
            FilterChip(
                selected = uiState.selectedPriority == Priority.HIGH,
                onClick = {
                    val nextPriority = if (uiState.selectedPriority == Priority.HIGH) null else Priority.HIGH
                    onEvent(TasksUiEvent.PriorityFilterChanged(nextPriority))
                },
                label = { Text("Solo Alta") }
            )

            // Process filters
            uiState.availableProcesses.filter { it.id != "general" }.forEach { proc ->
                FilterChip(
                    selected = uiState.selectedProcessId == proc.id,
                    onClick = {
                        val nextProc = if (uiState.selectedProcessId == proc.id) null else proc.id
                        onEvent(TasksUiEvent.ProcessFilterChanged(nextProc))
                    },
                    label = { Text(proc.name) }
                )
            }
        }

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
