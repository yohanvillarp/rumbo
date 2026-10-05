package tech.nikelyh.rumbo.feature.processes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikelyh.rumbo.core.designsystem.component.RumboButton
import tech.nikelyh.rumbo.core.designsystem.component.RumboLoadingState
import tech.nikelyh.rumbo.core.designsystem.component.RumboSectionHeader
import tech.nikelyh.rumbo.core.designsystem.theme.ProcessColors
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme

@Composable
fun EditProcessRoute(
    onProcessEdited: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditProcessViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onProcessEdited()
        }
    }

    if (uiState.isLoading) {
        RumboLoadingState(isLoading = true, modifier = modifier)
    } else {
        EditProcessScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditProcessScreen(
    uiState: EditProcessUiState,
    onEvent: (EditProcessUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.dueDateEpochMillis ?: System.currentTimeMillis()
    )
    val dueDateFormatted = remember(uiState.dueDateEpochMillis) {
        uiState.dueDateEpochMillis?.let {
            val instant = java.time.Instant.ofEpochMilli(it)
            val zone = java.time.ZoneId.systemDefault()
            instant.atZone(zone).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy, hh:mm a"))
        } ?: ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RumboSectionHeader(
            title = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.title_edit_process),
            subtitle = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_form_edit_subtitle)
        )

        // Name
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { onEvent(EditProcessUiEvent.NameChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_form_name_label)) },
            placeholder = { Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_form_name_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
            isError = uiState.nameError != null,
            supportingText = {
                uiState.nameError?.let { error ->
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
            onValueChange = { onEvent(EditProcessUiEvent.DescriptionChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_form_desc_label)) },
            placeholder = { Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_form_desc_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            maxLines = 3,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            )
        )

        // Fecha y hora límite (opcional)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = dueDateFormatted,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_form_due_date_label)) },
                placeholder = { Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_form_due_date_placeholder)) },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.dueDateEpochMillis != null) {
                            IconButton(onClick = { onEvent(EditProcessUiEvent.DueDateChanged(null)) }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_form_clear_due_date)
                                )
                            }
                        }
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.task_form_choose_date))
                        }
                        if (uiState.dueDateEpochMillis != null) {
                            IconButton(onClick = { showTimePicker = true }) {
                                Icon(Icons.Default.Schedule, contentDescription = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.task_form_choose_time))
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
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(end = if (uiState.dueDateEpochMillis != null) 140.dp else 48.dp)
                    .clickable { showDatePicker = true }
            )
        }

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

                                onEvent(EditProcessUiEvent.DueDateChanged(combinedMillis))
                            }
                            showDatePicker = false
                            showTimePicker = true
                        }
                    ) {
                        Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.dialog_accept))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.action_cancel))
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
                            onEvent(EditProcessUiEvent.DueDateChanged(updatedZdt.toInstant().toEpochMilli()))
                            showTimePicker = false
                        }
                    ) {
                        Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.dialog_accept))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.action_cancel))
                    }
                },
                title = { Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.task_form_time_limit_title)) },
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

        // Circular Color Picker (No English text labels!)
        Text(
            text = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_form_color_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProcessColors.options.forEach { (colorKey, colorValue) ->
                val isSelected = uiState.colorOrVisualId == colorKey
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colorValue)
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onEvent(EditProcessUiEvent.ColorChanged(colorKey)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_form_color_selected),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        RumboButton(
            onClick = { onEvent(EditProcessUiEvent.SubmitProcess) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSubmitting
        ) {
            Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_form_save_changes))
        }
    }
}

@Preview(name = "Edit Process Light", showBackground = true)
@Composable
private fun EditProcessScreenPreviewLight() {
    RumboTheme(darkTheme = false) {
        EditProcessScreen(
            uiState = EditProcessUiState(name = "Desarrollo de Rumbo"),
            onEvent = {}
        )
    }
}
