package tech.nikelyh.rumbo.feature.processes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import tech.nikelyh.rumbo.core.designsystem.component.RumboCard
import tech.nikelyh.rumbo.core.designsystem.component.RumboProcessCard
import tech.nikelyh.rumbo.core.designsystem.component.RumboEmptyState
import tech.nikelyh.rumbo.core.designsystem.component.RumboLoadingState
import tech.nikelyh.rumbo.core.designsystem.component.RumboOutlinedButton
import tech.nikelyh.rumbo.core.designsystem.component.RumboSectionHeader
import tech.nikelyh.rumbo.core.designsystem.component.RumboTaskItem
import tech.nikelyh.rumbo.core.designsystem.component.TaskCompletionDurationDialog
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.GoalStatus
import tech.nikelyh.rumbo.core.model.Priority
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProcessStatus
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus
import tech.nikelyh.rumbo.core.model.WeeklyGoal

@Composable
fun ProcessDetailRoute(
    onNavigateToEditProcess: (String) -> Unit,
    onNavigateToCreateTask: (String) -> Unit,
    onNavigateToStartSession: (String, String?) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCreateProcess: (String?) -> Unit = {},
    onNavigateToProcessDetail: (String) -> Unit = {},
    onNavigateToLogProgress: ((String) -> Unit)? = null,
    onNavigateToTask: (String) -> Unit = {},
    viewModel: ProcessDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProcessDetailScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToEditProcess = { onNavigateToEditProcess(viewModel.processId) },
        onNavigateToCreateTask = { onNavigateToCreateTask(viewModel.processId) },
        onNavigateToStartSession = { taskId -> onNavigateToStartSession(viewModel.processId, taskId) },
        onNavigateToCreateProcess = onNavigateToCreateProcess,
        onNavigateToProcessDetail = onNavigateToProcessDetail,
        onNavigateToTask = onNavigateToTask,
        modifier = modifier
    )
}

@Composable
internal fun ProcessDetailScreen(
    uiState: ProcessDetailUiState,
    onEvent: (ProcessDetailUiEvent) -> Unit,
    onNavigateToEditProcess: () -> Unit,
    onNavigateToCreateTask: () -> Unit,
    onNavigateToStartSession: (String?) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCreateProcess: (String?) -> Unit = {},
    onNavigateToProcessDetail: (String) -> Unit = {},
    onNavigateToTask: (String) -> Unit = {}
) {
    when (uiState) {
        ProcessDetailUiState.Loading -> {
            RumboLoadingState(isLoading = true, modifier = modifier)
        }
        is ProcessDetailUiState.Error -> {
            RumboEmptyState(message = uiState.message, modifier = modifier)
        }
        is ProcessDetailUiState.Content -> {
            ProcessDetailContent(
                uiState = uiState,
                onEvent = onEvent,
                onNavigateToEditProcess = onNavigateToEditProcess,
                onNavigateToCreateTask = onNavigateToCreateTask,
                onNavigateToStartSession = onNavigateToStartSession,
                onNavigateToCreateProcess = onNavigateToCreateProcess,
                onNavigateToProcessDetail = onNavigateToProcessDetail,
                onNavigateToTask = onNavigateToTask,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ProcessDetailContent(
    uiState: ProcessDetailUiState.Content,
    onEvent: (ProcessDetailUiEvent) -> Unit,
    onNavigateToEditProcess: () -> Unit,
    onNavigateToCreateTask: () -> Unit,
    onNavigateToStartSession: (String?) -> Unit,
    onNavigateToCreateProcess: (String?) -> Unit,
    onNavigateToProcessDetail: (String) -> Unit,
    onNavigateToTask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val process = uiState.process
    var showWeeklyGoalDialog by remember { mutableStateOf(false) }
    var weeklyGoalInput by remember { mutableStateOf("") }
    var taskToCompleteWithDuration by remember { mutableStateOf<Task?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            RumboCard(modifier = Modifier.fillMaxWidth()) {
                val processStatusSpanish = when (process.status) {
                    ProcessStatus.ACTIVE -> "Activo"
                    ProcessStatus.PAUSED -> "Pausado"
                    ProcessStatus.COMPLETED -> "Completado"
                    ProcessStatus.ARCHIVED -> "Archivado"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = process.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = processStatusSpanish,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                process.description?.let { desc ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                process.nextAction?.let { action ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Siguiente acción: $action",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val timeHours = uiState.totalTimeInvestedMillis / (1000 * 60 * 60)
                val timeMinutes = (uiState.totalTimeInvestedMillis / (1000 * 60)) % 60
                Text(
                    text = "Invertido: ${timeHours}h ${timeMinutes}m  •  Costo: $${process.accumulatedDirectCost}  •  Sesiones: ${uiState.workSessions.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (uiState.parentProcess != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable { onNavigateToProcessDetail(uiState.parentProcess.id) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountTree,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Subproceso de: ${uiState.parentProcess.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                if (process.isSystem) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Espacio para tus tareas y actividades cotidianas",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Primary Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!process.isFinished) {
                    RumboButton(
                        onClick = onNavigateToCreateTask,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddTask, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Nueva Tarea")
                    }

                    if (!process.isSystem) {
                        RumboOutlinedButton(
                            onClick = { onNavigateToCreateProcess(process.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AccountTree, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nuevo Proceso")
                        }
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Este proceso ha sido completado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    RumboButton(
                        onClick = { onEvent(ProcessDetailUiEvent.ReopenProcess) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reabrir Proceso")
                    }
                }

                if (!process.isSystem) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RumboOutlinedButton(
                            onClick = onNavigateToEditProcess,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar")
                        }

                        if (process.isActive || process.status == ProcessStatus.PAUSED) {
                            OutlinedButton(
                                onClick = { onEvent(ProcessDetailUiEvent.FinishProcess) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Finalizar")
                            }
                        }
                    }
                }
            }
        }

        // Section: Weekly Goal
        item {
            RumboSectionHeader(title = "Objetivo Semanal")
            val goal = uiState.weeklyGoal

            if (goal != null) {
                val goalStatusSpanish = when (goal.status) {
                    GoalStatus.PENDING -> "Pendiente"
                    GoalStatus.IN_PROGRESS -> "En progreso"
                    GoalStatus.ACHIEVED -> "Alcanzado"
                    GoalStatus.CANCELLED -> "Cancelado"
                }

                RumboCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Semana: ${goal.weekIdentifier}  •  Estado: $goalStatusSpanish",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RumboButton(
                                onClick = { onEvent(ProcessDetailUiEvent.CompleteWeeklyGoal(goal.id)) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Completar")
                            }
                            RumboOutlinedButton(
                                onClick = { onEvent(ProcessDetailUiEvent.CarryOverWeeklyGoal(goal.id)) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mover")
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = {
                                    weeklyGoalInput = goal.description
                                    showWeeklyGoalDialog = true
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Editar")
                            }
                            TextButton(
                                onClick = { onEvent(ProcessDetailUiEvent.DiscardWeeklyGoal(goal.id)) }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Descartar", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            } else {
                RumboOutlinedButton(
                    onClick = {
                        weeklyGoalInput = ""
                        showWeeklyGoalDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Definir Objetivo Semanal")
                }
            }
        }

        // Section: Subprocesses (shown when sub-processes exist)
        if (uiState.subProcesses.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RumboSectionHeader(
                        title = "Subprocesos (${uiState.subProcesses.size})",
                        modifier = Modifier.weight(1f)
                    )
                    if (!process.isFinished && !process.isSystem) {
                        TextButton(onClick = { onNavigateToCreateProcess(process.id) }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nuevo Subproceso")
                        }
                    }
                }
            }

            items(uiState.subProcesses, key = { it.id }) { subProcess ->
                RumboProcessCard(
                    process = subProcess,
                    onClick = { onNavigateToProcessDetail(subProcess.id) }
                )
            }
        }

        // Section: Milestones (shown when milestones exist)
        if (uiState.milestones.isNotEmpty()) {
            item {
                RumboSectionHeader(title = "Hitos clave")
            }

            items(uiState.milestones, key = { it.id }) { milestone ->
                RumboCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = milestone.isCompleted,
                                onCheckedChange = { onEvent(ProcessDetailUiEvent.ToggleMilestoneStatus(milestone)) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = milestone.title,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Section: Pending Tasks
        item {
            RumboSectionHeader(title = "Tareas Pendientes")
        }

        if (uiState.pendingTasks.isEmpty()) {
            item {
                Text(
                    text = "Sin tareas pendientes asignadas a este proceso.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            items(uiState.pendingTasks, key = { it.id }) { taskItem ->
                RumboTaskItem(
                    task = taskItem,
                    processColorOrVisualId = process.colorOrVisualId,
                    processName = process.name,
                    onToggleStatus = { task ->
                        if (!task.isCompleted) {
                            taskToCompleteWithDuration = task
                        } else {
                            onEvent(ProcessDetailUiEvent.ToggleTaskStatus(task))
                        }
                    },
                    onClick = { onNavigateToTask(taskItem.id) },
                    onStartSession = { task -> onNavigateToStartSession(task.id) }
                )
            }
        }

        // Section: Completed Tasks (Preserved & Visible)
        if (uiState.completedTasks.isNotEmpty()) {
            item {
                RumboSectionHeader(title = "Tareas Completadas")
            }

            items(uiState.completedTasks, key = { it.id }) { taskItem ->
                RumboTaskItem(
                    task = taskItem,
                    processColorOrVisualId = process.colorOrVisualId,
                    processName = process.name,
                    onToggleStatus = { onEvent(ProcessDetailUiEvent.ToggleTaskStatus(it)) },
                    onClick = { onNavigateToTask(taskItem.id) }
                )
            }
        }
    }

    if (uiState.userMessage != null) {
        AlertDialog(
            onDismissRequest = { onEvent(ProcessDetailUiEvent.DismissUserMessage) },
            title = { Text("No se puede finalizar el proceso") },
            text = { Text(uiState.userMessage) },
            confirmButton = {
                TextButton(onClick = { onEvent(ProcessDetailUiEvent.DismissUserMessage) }) {
                    Text("Entendido")
                }
            }
        )
    }

    if (showWeeklyGoalDialog) {
        AlertDialog(
            onDismissRequest = { showWeeklyGoalDialog = false },
            title = { Text("Objetivo Semanal") },
            text = {
                OutlinedTextField(
                    value = weeklyGoalInput,
                    onValueChange = { weeklyGoalInput = it },
                    label = { Text("Descripción del objetivo") },
                    placeholder = { Text("Ej. Completar las tareas clave de la semana") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (weeklyGoalInput.isNotBlank()) {
                            onEvent(ProcessDetailUiEvent.SaveWeeklyGoal(weeklyGoalInput.trim()))
                            showWeeklyGoalDialog = false
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeeklyGoalDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
                onEvent(ProcessDetailUiEvent.CompleteTaskWithDuration(task, minutes))
                taskToCompleteWithDuration = null
            },
            onDismiss = { taskToCompleteWithDuration = null }
        )
    }
}

@Preview(name = "Process Detail Light", showBackground = true)
@Composable
private fun ProcessDetailScreenPreviewLight() {
    RumboTheme(darkTheme = false) {
        ProcessDetailScreen(
            uiState = ProcessDetailUiState.Content(
                process = Process(
                    id = "p1",
                    name = "Desarrollo de Rumbo",
                    description = "Implementar la capa de persistencia y features principales.",
                    status = ProcessStatus.ACTIVE,
                    createdAtEpochMillis = 1000L,
                    colorOrVisualId = "teal",
                    accumulatedDirectCost = 250.0,
                    nextAction = "Escribir pruebas unitarias"
                ),
                pendingTasks = listOf(
                    Task(
                        id = "t1",
                        processId = "p1",
                        title = "Implementar ProcessDetailScreen",
                        status = TaskStatus.PENDING,
                        priority = Priority.HIGH,
                        createdAtEpochMillis = 1000L
                    )
                ),
                completedTasks = emptyList(),
                milestones = emptyList(),
                workSessions = emptyList(),
                totalTimeInvestedMillis = 3600000L * 3,
                progressEntries = emptyList(),
                weeklyGoal = WeeklyGoal(
                    id = "g1",
                    processId = "p1",
                    weekIdentifier = "2026-W40",
                    description = "Completar las tareas clave de la semana"
                )
            ),
            onEvent = {},
            onNavigateToEditProcess = {},
            onNavigateToCreateTask = {},
            onNavigateToStartSession = {}
        )
    }
}
