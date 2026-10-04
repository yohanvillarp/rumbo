package tech.nikelyh.rumbo.feature.home

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import tech.nikelyh.rumbo.core.designsystem.component.MascotState
import tech.nikelyh.rumbo.core.designsystem.component.RumboButton
import tech.nikelyh.rumbo.core.designsystem.component.RumboCard
import tech.nikelyh.rumbo.core.designsystem.component.RumboEmptyState
import tech.nikelyh.rumbo.core.designsystem.component.RumboLoadingState
import tech.nikelyh.rumbo.core.designsystem.component.RumboOutlinedButton
import tech.nikelyh.rumbo.core.designsystem.component.RumboProcessCard
import tech.nikelyh.rumbo.core.designsystem.component.RumboSectionHeader
import tech.nikelyh.rumbo.core.designsystem.component.RumboTaskItem
import tech.nikelyh.rumbo.core.designsystem.component.TaskCompletionDurationDialog
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.Priority
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProcessStatus
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus

@Composable
fun HomeRoute(
    onNavigateToProcess: (String) -> Unit,
    onNavigateToTask: (String) -> Unit,
    onNavigateToCreateProcess: () -> Unit = {},
    onNavigateToCreateTask: () -> Unit = {},
    onNavigateToLogProgress: () -> Unit = {},
    onNavigateToStartSession: (String?, String?) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is HomeUiEvent.OnProcessClick -> onNavigateToProcess(event.processId)
                is HomeUiEvent.OnTaskClick -> onNavigateToTask(event.taskId)
                HomeUiEvent.OnCreateProcessClick -> onNavigateToCreateProcess()
                HomeUiEvent.OnCreateTaskClick -> onNavigateToCreateTask()
                HomeUiEvent.OnLogProgressClick -> onNavigateToLogProgress()
                HomeUiEvent.OnStartSessionClick -> onNavigateToStartSession(null, null)
                else -> viewModel.onEvent(event)
            }
        },
        onNavigateToStartSession = onNavigateToStartSession,
        modifier = modifier
    )
}

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    onNavigateToStartSession: (String?, String?) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    when (uiState) {
        HomeUiState.Loading -> {
            RumboLoadingState(isLoading = true, modifier = modifier)
        }
        is HomeUiState.Error -> {
            RumboEmptyState(
                message = uiState.message,
                modifier = modifier
            )
        }
        HomeUiState.Empty -> {
            RumboEmptyState(
                message = "Bienvenido a Rumbo",
                subtitle = "¿Qué te gustaría comenzar hoy?",
                mascotState = MascotState.DEFAULT,
                actionLabel = "Crear primer proceso",
                onActionClick = { onEvent(HomeUiEvent.OnCreateProcessClick) },
                modifier = modifier
            )
        }
        is HomeUiState.Content -> {
            HomeContent(
                uiState = uiState,
                onEvent = onEvent,
                onNavigateToStartSession = onNavigateToStartSession,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState.Content,
    onEvent: (HomeUiEvent) -> Unit,
    onNavigateToStartSession: (String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var taskToCompleteWithDuration by remember { mutableStateOf<Task?>(null) }
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Greeting Header
        item {
            Column {
                Text(
                    text = uiState.greeting,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¿En qué deseas avanzar hoy?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                )
            }
        }

        // Section 1: Continue (Featured Active Process)
        uiState.continueProcess?.let { process ->
            item {
                RumboCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEvent(HomeUiEvent.OnProcessClick(process.id)) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Proceso activo destacado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = process.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    process.nextAction?.let { action ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Siguiente acción: $action",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    RumboButton(
                        onClick = { onEvent(HomeUiEvent.OnProcessClick(process.id)) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (!process.nextAction.isNullOrBlank()) "Reanudar Actividad" else "Reanudar Proceso",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // Section 2: Active Processes (Max 3)
        item {
            RumboSectionHeader(
                title = "Procesos Activos",
                subtitle = "Procesos en seguimiento"
            )
        }

        if (uiState.activeProcesses.isEmpty()) {
            item {
                RumboEmptyState(
                    message = "No hay procesos activos",
                    subtitle = "Crea un proceso para iniciar tus actividades",
                    mascotState = MascotState.RESTING,
                    modifier = Modifier.height(160.dp)
                )
            }
        } else {
            items(uiState.activeProcesses, key = { it.id }) { processItem ->
                RumboProcessCard(
                    process = processItem,
                    onClick = { onEvent(HomeUiEvent.OnProcessClick(processItem.id)) }
                )
            }
        }

        // Section 3: Today Tasks
        item {
            RumboSectionHeader(
                title = "Hoy",
                subtitle = "Tareas pendientes para hoy"
            )
        }

        if (uiState.todayTasks.isEmpty()) {
            item {
                RumboEmptyState(
                    message = "Sin tareas pendientes para hoy",
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    modifier = Modifier.height(140.dp)
                )
            }
        } else {
            items(uiState.todayTasks, key = { it.id }) { taskItem ->
                val taskProcess = uiState.allProcesses.find { it.id == taskItem.processId }
                RumboTaskItem(
                    task = taskItem,
                    processColorOrVisualId = taskProcess?.colorOrVisualId,
                    processName = taskProcess?.name,
                    onToggleStatus = { task ->
                        if (!task.isCompleted) {
                            taskToCompleteWithDuration = task
                        } else {
                            onEvent(HomeUiEvent.OnToggleTaskStatus(task))
                        }
                    },
                    onClick = { onEvent(HomeUiEvent.OnTaskClick(taskItem.id)) },
                    onStartSession = { task -> onNavigateToStartSession(task.processId, task.id) }
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
                onEvent(HomeUiEvent.CompleteTaskWithDuration(task, minutes))
                taskToCompleteWithDuration = null
            },
            onDismiss = { taskToCompleteWithDuration = null }
        )
    }
}
}

@Preview(name = "Home Light", showBackground = true)
@Composable
private fun HomeScreenPreviewLight() {
    RumboTheme(darkTheme = false) {
        HomeScreen(
            uiState = HomeUiState.Content(
                greeting = "Buenos días, Yohan",
                userName = "Yohan",
                continueProcess = Process(
                    id = "p1",
                    name = "Organización del Hogar",
                    description = "Planificar limpieza profunda y compras del mes.",
                    status = ProcessStatus.ACTIVE,
                    createdAtEpochMillis = 1000L,
                    colorOrVisualId = "teal",
                    accumulatedDirectCost = 150.0,
                    nextAction = "Hacer inventario de despensa"
                ),
                activeProcesses = listOf(
                    Process(
                        id = "p1",
                        name = "Organización del Hogar",
                        description = "Planificar limpieza profunda y compras del mes.",
                        status = ProcessStatus.ACTIVE,
                        createdAtEpochMillis = 1000L,
                        colorOrVisualId = "teal",
                        accumulatedDirectCost = 150.0,
                        nextAction = "Hacer inventario de despensa"
                    )
                ),
                todayTasks = listOf(
                    Task(
                        id = "t1",
                        processId = "p1",
                        title = "Comprar frutas y verduras",
                        description = "Ir al mercado central por la mañana",
                        status = TaskStatus.PENDING,
                        priority = Priority.HIGH,
                        createdAtEpochMillis = 1000L,
                        cost = 0.0
                    )
                )
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Home Dark", showBackground = true)
@Composable
private fun HomeScreenPreviewDark() {
    RumboTheme(darkTheme = true) {
        HomeScreen(
            uiState = HomeUiState.Content(
                greeting = "Buenas noches, Yohan",
                userName = "Yohan",
                continueProcess = null,
                activeProcesses = emptyList(),
                todayTasks = emptyList()
            ),
            onEvent = {}
        )
    }
}
