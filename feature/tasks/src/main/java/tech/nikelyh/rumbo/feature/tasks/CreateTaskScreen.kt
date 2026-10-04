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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateTaskScreen(
    uiState: CreateTaskUiState,
    onEvent: (CreateTaskUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var processDropdownExpanded by remember { mutableStateOf(false) }

    val selectedProcess = uiState.availableProcesses.firstOrNull { it.id == uiState.selectedProcessId }
    val selectedProcessLabel = when {
        uiState.selectedProcessId == "general" -> "General (Proceso por defecto)"
        selectedProcess != null -> selectedProcess.name
        else -> "General (Proceso por defecto)"
    }

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
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            )
        )

        // Description
        OutlinedTextField(
            value = uiState.description,
            onValueChange = { onEvent(CreateTaskUiEvent.DescriptionChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descripción (opcional)") },
            placeholder = { Text("Detalles específicos de la tarea") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            maxLines = 3,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            )
        )

        // Process Selection Dropdown List
        ExposedDropdownMenuBox(
            expanded = processDropdownExpanded,
            onExpandedChange = { processDropdownExpanded = !processDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedProcessLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Proceso Asignado") },
                leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = processDropdownExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = processDropdownExpanded,
                onDismissRequest = { processDropdownExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("General (Proceso por defecto)") },
                    onClick = {
                        onEvent(CreateTaskUiEvent.ProcessSelected("general"))
                        processDropdownExpanded = false
                    }
                )
                uiState.availableProcesses.filter { it.id != "general" }.forEach { process ->
                    DropdownMenuItem(
                        text = { Text(process.name) },
                        onClick = {
                            onEvent(CreateTaskUiEvent.ProcessSelected(process.id))
                            processDropdownExpanded = false
                        }
                    )
                }
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

        // Estimated Duration (STRICT Digits Only + KeyboardType.Number)
        OutlinedTextField(
            value = uiState.estimatedDurationMinutesInput,
            onValueChange = { input ->
                if (input.all { it.isDigit() }) {
                    onEvent(CreateTaskUiEvent.DurationChanged(input))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Duración Estimada (minutos, opcional)") },
            placeholder = { Text("30") },
            leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )

        // Cost (KeyboardType.Decimal)
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
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            )
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
