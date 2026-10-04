package tech.nikelyh.rumbo.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikelyh.rumbo.core.designsystem.component.RumboButton
import tech.nikelyh.rumbo.core.designsystem.component.RumboSectionHeader
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.Priority

@Composable
fun CreateTaskRoute(
    onTaskCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateTaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onTaskCreated()
        }
    }

    CreateTaskScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
internal fun CreateTaskScreen(
    uiState: CreateTaskUiState,
    onEvent: (CreateTaskUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RumboSectionHeader(
            title = "Nueva Tarea",
            subtitle = "Define el título, asigna un proceso y prioridad."
        )

        // Title
        OutlinedTextField(
            value = uiState.title,
            onValueChange = { onEvent(CreateTaskUiEvent.TitleChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Título de la tarea *") },
            placeholder = { Text("Ej. Configurar módulo :core:data") },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null) },
            isError = uiState.titleError != null,
            supportingText = {
                uiState.titleError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            singleLine = true
        )

        // Description
        OutlinedTextField(
            value = uiState.description,
            onValueChange = { onEvent(CreateTaskUiEvent.DescriptionChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descripción (opcional)") },
            placeholder = { Text("Detalles específicos de la tarea") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            maxLines = 3
        )

        // Process Selection (Defaulting to General)
        Text(
            text = "Proceso Asignado",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedProcessId == "general",
                onClick = { onEvent(CreateTaskUiEvent.ProcessSelected("general")) },
                label = { Text("General (Proceso por defecto)") }
            )
            uiState.availableProcesses.filter { it.id != "general" }.take(2).forEach { process ->
                FilterChip(
                    selected = uiState.selectedProcessId == process.id,
                    onClick = { onEvent(CreateTaskUiEvent.ProcessSelected(process.id)) },
                    label = { Text(process.name) }
                )
            }
        }

        // Priority Selection (In Spanish)
        Text(
            text = "Prioridad",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Priority.entries.forEach { priority ->
                val SpanishLabel = when (priority) {
                    Priority.LOW -> "Baja"
                    Priority.MEDIUM -> "Media"
                    Priority.HIGH -> "Alta"
                }
                FilterChip(
                    selected = uiState.priority == priority,
                    onClick = { onEvent(CreateTaskUiEvent.PriorityChanged(priority)) },
                    label = { Text(SpanishLabel) }
                )
            }
        }

        // Estimated Duration
        OutlinedTextField(
            value = uiState.estimatedDurationMinutesInput,
            onValueChange = { onEvent(CreateTaskUiEvent.DurationChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Duración Estimada (minutos, opcional)") },
            placeholder = { Text("30") },
            leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
            singleLine = true
        )

        // Cost
        OutlinedTextField(
            value = uiState.costInput,
            onValueChange = { onEvent(CreateTaskUiEvent.CostChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Costo ($)") },
            placeholder = { Text("0.0") },
            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
            isError = uiState.costError != null,
            supportingText = {
                uiState.costError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        RumboButton(
            onClick = { onEvent(CreateTaskUiEvent.SubmitTask) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSubmitting
        ) {
            Text("Guardar Tarea")
        }
    }
}

@Preview(name = "Create Task Light", showBackground = true)
@Composable
private fun CreateTaskScreenPreviewLight() {
    RumboTheme(darkTheme = false) {
        CreateTaskScreen(
            uiState = CreateTaskUiState(),
            onEvent = {}
        )
    }
}
