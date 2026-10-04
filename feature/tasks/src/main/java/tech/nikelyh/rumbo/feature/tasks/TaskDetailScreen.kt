package tech.nikelyh.rumbo.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.Priority
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus

import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import tech.nikelyh.rumbo.core.designsystem.component.TaskCompletionDurationDialog
import java.time.format.DateTimeFormatter

private val TASK_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, hh:mm a")

@Composable
fun TaskDetailRoute(
    onTaskDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    onStartSession: (String, String) -> Unit = { _, _ -> },
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TaskDetailScreen(
        uiState = uiState,
        onEvent = { event ->
            if (event is TaskDetailUiEvent.DeleteTask) {
                viewModel.onEvent(event)
                onTaskDeleted()
            } else {
                viewModel.onEvent(event)
            }
        },
        onStartSession = onStartSession,
        modifier = modifier
    )
}

@Composable
internal fun TaskDetailScreen(
    uiState: TaskDetailUiState,
    onEvent: (TaskDetailUiEvent) -> Unit,
    onStartSession: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    when (uiState) {
        TaskDetailUiState.Loading -> {
            RumboLoadingState(isLoading = true, modifier = modifier)
        }
        is TaskDetailUiState.Error -> {
            RumboEmptyState(message = uiState.message, modifier = modifier)
        }
        is TaskDetailUiState.Content -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RumboSectionHeader(title = "Detalle de Tarea")

                val task = uiState.task
                val statusSpanish = when (task.status) {
                    TaskStatus.PENDING -> "Pendiente"
                    TaskStatus.IN_PROGRESS -> "En progreso"
                    TaskStatus.COMPLETED -> "Completada"
                    TaskStatus.CANCELLED -> "Cancelada"
                }
                val prioritySpanish = when (task.priority) {
                    Priority.LOW -> "Baja"
                    Priority.MEDIUM -> "Media"
                    Priority.HIGH -> "Alta"
                }

                RumboCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = statusSpanish,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Proceso: ${uiState.processName}  •  Prioridad: $prioritySpanish",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    task.description?.let { desc ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    if (task.cost > 0.0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Costo: $${task.cost}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (task.timeWorkedMillis > 0) {
                        val minutes = task.timeWorkedMillis / 60000
                        val hours = minutes / 60
                        val remainingMinutes = minutes % 60
                        val timeText = if (hours > 0) "${hours}h ${remainingMinutes}m" else "${minutes}m"
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tiempo trabajado: $timeText",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    task.estimatedDurationMinutes?.let { minutes ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Duración estimada: $minutes min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    task.dueDateEpochMillis?.let { dueMillis ->
                        val dateText = remember(dueMillis) {
                            val instant = java.time.Instant.ofEpochMilli(dueMillis)
                            val zone = java.time.ZoneId.systemDefault()
                            val zonedDateTime = instant.atZone(zone)
                            zonedDateTime.format(TASK_DATE_FORMATTER)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Fecha límite: $dateText",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Primary action is Iniciar / Continuar Sesión
                val isTaskStarted = uiState.hasStartedSession || task.timeWorkedMillis > 0L || task.status == TaskStatus.IN_PROGRESS
                RumboButton(
                    onClick = { onStartSession(task.id, task.processId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isTaskStarted) Icons.Default.PlayCircle else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTaskStarted) "Continuar Sesión" else "Iniciar Sesión")
                }

                RumboOutlinedButton(
                    onClick = {
                        if (!task.isCompleted && task.timeWorkedMillis == 0L) {
                            showCompletionDialog = true
                        } else {
                            onEvent(TaskDetailUiEvent.ToggleStatus)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.RadioButtonUnchecked else Icons.Default.CheckCircle,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (task.isCompleted) "Marcar Pendiente" else "Marcar Completada")
                }

                RumboOutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar Tarea", color = MaterialTheme.colorScheme.error)
                }

                if (showCompletionDialog) {
                    TaskCompletionDurationDialog(
                        taskTitle = task.title,
                        onConfirm = { minutes ->
                            onEvent(TaskDetailUiEvent.CompleteWithDuration(minutes))
                            showCompletionDialog = false
                        },
                        onDismiss = { showCompletionDialog = false }
                    )
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Confirmar eliminación") },
                        text = { Text("¿Deseas eliminar la tarea '${task.title}'? Esta acción no se puede deshacer.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    onEvent(TaskDetailUiEvent.DeleteTask)
                                }
                            ) {
                                Text("Eliminar", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview(name = "Task Detail Light", showBackground = true)
@Composable
private fun TaskDetailScreenPreviewLight() {
    RumboTheme(darkTheme = false) {
        TaskDetailScreen(
            uiState = TaskDetailUiState.Content(
                task = Task(
                    id = "t1",
                    processId = "p1",
                    title = "Diseñar UI de TasksScreen",
                    description = "Garantizar que pertenezca a un proceso y valide costos.",
                    status = TaskStatus.PENDING,
                    priority = Priority.HIGH,
                    createdAtEpochMillis = 1000L,
                    cost = 0.0,
                    estimatedDurationMinutes = 45
                ),
                processName = "Desarrollo de Rumbo"
            ),
            onEvent = {}
        )
    }
}
