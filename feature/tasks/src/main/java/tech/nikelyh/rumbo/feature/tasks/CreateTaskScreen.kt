package tech.nikelyh.rumbo.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import java.time.format.DateTimeFormatter

private val TASK_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, hh:mm a")

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
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.dueDateEpochMillis ?: System.currentTimeMillis()
    )

    val selectableProcesses = uiState.availableProcesses.filter { it.id != "general" && !it.isFinished }
    val selectedProcess = selectableProcesses.firstOrNull { it.id == uiState.selectedProcessId }
    val selectedProcessLabel = selectedProcess?.name ?: if (selectableProcesses.isEmpty()) "Sin procesos disponibles" else "Selecciona un proceso"

    val dueDateFormatted = remember(uiState.dueDateEpochMillis) {
        val dueMillis = uiState.dueDateEpochMillis
        if (dueMillis != null) {
            val instant = java.time.Instant.ofEpochMilli(dueMillis)
            val zone = java.time.ZoneId.systemDefault()
            val zonedDateTime = instant.atZone(zone)
            zonedDateTime.format(TASK_DATE_FORMATTER)
        } else ""
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
            placeholder = { Text("Ej. Revisar presupuesto del mes") },
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

        // Process Selection Dropdown List (No General, No Completed Processes)
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
                isError = uiState.processError != null,
                supportingText = {
                    uiState.processError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = processDropdownExpanded,
                onDismissRequest = { processDropdownExpanded = false }
            ) {
                if (selectableProcesses.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay procesos activos disponibles") },
                        enabled = false,
                        onClick = {}
                    )
                } else {
                    selectableProcesses.forEach { process ->
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
        }

        // Fecha de Caducidad (Solicitud requerida con hora, por defecto 11:59 PM)
        OutlinedTextField(
            value = dueDateFormatted,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            label = { Text("Fecha y Hora de Caducidad *") },
            placeholder = { Text("Toca para elegir fecha (por defecto 11:59 PM)") },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Elegir fecha")
                    }
                    if (uiState.dueDateEpochMillis != null) {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Elegir hora")
                        }
                    }
                }
            },
            isError = uiState.dueDateError != null,
            supportingText = {
                uiState.dueDateError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedUtc = datePickerState.selectedDateMillis
                            if (selectedUtc != null) {
                                val utcDate = java.time.Instant.ofEpochMilli(selectedUtc)
                                    .atZone(java.time.ZoneOffset.UTC)
                                    .toLocalDate()
                                val existingZdt = uiState.dueDateEpochMillis?.let {
                                    java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault())
                                }
                                val hour = existingZdt?.hour ?: 23
                                val minute = existingZdt?.minute ?: 59

                                val combinedMillis = utcDate.atTime(hour, minute)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()

                                onEvent(CreateTaskUiEvent.DueDateChanged(combinedMillis))
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker && uiState.dueDateEpochMillis != null) {
            val currentZdt = java.time.Instant.ofEpochMilli(uiState.dueDateEpochMillis)
                .atZone(java.time.ZoneId.systemDefault())
            val timePickerState = rememberTimePickerState(
                initialHour = currentZdt.hour,
                initialMinute = currentZdt.minute,
                is24Hour = false
            )

            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val updatedZdt = currentZdt.toLocalDate()
                                .atTime(timePickerState.hour, timePickerState.minute)
                                .atZone(java.time.ZoneId.systemDefault())
                            onEvent(CreateTaskUiEvent.DueDateChanged(updatedZdt.toInstant().toEpochMilli()))
                            showTimePicker = false
                        }
                    ) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("Hora de vencimiento") },
                text = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TimePicker(state = timePickerState)
                    }
                }
            )
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
