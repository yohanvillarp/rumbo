package tech.nikelyh.rumbo.feature.processes

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import tech.nikelyh.rumbo.core.designsystem.component.RumboEmptyState
import tech.nikelyh.rumbo.core.designsystem.component.RumboLoadingState
import tech.nikelyh.rumbo.core.designsystem.component.RumboOutlinedButton
import tech.nikelyh.rumbo.core.designsystem.component.RumboSectionHeader
import tech.nikelyh.rumbo.core.designsystem.component.RumboTaskItem
import tech.nikelyh.rumbo.core.designsystem.component.TaskCompletionDurationDialog
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
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
    onNavigateToTask: (String) -> Unit = {},
    modifier: Modifier = Modifier
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
                        text = process.status.name,
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
            }
        }

        // Section: Weekly Goal
        item {
            RumboSectionHeader(title = "Objetivo Semanal")
            val goal = uiState.weeklyGoal

            if (goal != null) {
                RumboCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Semana: ${goal.weekIdentifier}  •  Estado: ${goal.status.name}",
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

        // Primary Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RumboButton(
                    onClick = {
                        val nextTaskId = uiState.pendingTasks.firstOrNull()?.id
                        onNavigateToStartSession(nextTaskId)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (uiState.pendingTasks.isNotEmpty()) "Iniciar Sesión en Tarea" else "Iniciar Sesión")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RumboOutlinedButton(
                        onClick = onNavigateToCreateTask,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AddTask, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nueva Tarea")
                    }
                    RumboOutlinedButton(
                        onClick = onNavigateToEditProcess,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (process.isActive) {
                        OutlinedButton(
                            onClick = { onEvent(ProcessDetailUiEvent.PauseProcess) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pausar")
                        }
                    } else if (process.status == ProcessStatus.PAUSED) {
                        OutlinedButton(
                            onClick = { onEvent(ProcessDetailUiEvent.ResumeProcess) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reactivar")
                        }
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

        // Section: Milestones
        item {
            RumboSectionHeader(title = "Hitos / Milestones")
        }

        if (uiState.milestones.isEmpty()) {
            item {
                Text(
                    text = "Sin hitos definidos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
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
                    onToggleStatus = { task ->
                        if (!task.isCompleted && task.timeWorkedMillis == 0L) {
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
                    onToggleStatus = { onEvent(ProcessDetailUiEvent.ToggleTaskStatus(it)) },
                    onClick = { onNavigateToTask(taskItem.id) }
                )
            }
        }
    }

    if (uiState.userMessage != null) {
        AlertDialog(
            onDismissRequest = { onEvent(ProcessDetailUiEvent.DismissUserMessage) },
            title = { Text("Tareas Pendientes Existentes") },
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
                    placeholder = { Text("Ej. Terminar el dashboard OLAP") },
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
        TaskCompletionDurationDialog(
            taskTitle = taskToCompleteWithDuration!!.title,
            onConfirm = { minutes ->
                onEvent(ProcessDetailUiEvent.CompleteTaskWithDuration(taskToCompleteWithDuration!!, minutes))
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
                    description = "Terminar el dashboard OLAP"
                )
            ),
            onEvent = {},
            onNavigateToEditProcess = {},
            onNavigateToCreateTask = {},
            onNavigateToStartSession = {}
        )
    }
}
