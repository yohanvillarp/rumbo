package tech.nikelyh.rumbo.feature.progress

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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikelyh.rumbo.core.designsystem.component.RumboButton
import tech.nikelyh.rumbo.core.designsystem.component.RumboSectionHeader
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.ProgressLevel

@Composable
fun LogProgressRoute(
    onProgressLogged: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LogProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onProgressLogged()
        }
    }

    LogProgressScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LogProgressScreen(
    uiState: LogProgressUiState,
    onEvent: (LogProgressUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var processDropdownExpanded by remember { mutableStateOf(false) }
    val selectedProcess = uiState.availableProcesses.firstOrNull { it.id == uiState.selectedProcessId }
    val selectedProcessLabel = if (uiState.selectedProcessId == "general" || selectedProcess == null) {
        "General"
    } else {
        selectedProcess.name
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                        onEvent(LogProgressUiEvent.ProcessSelected("general"))
                        processDropdownExpanded = false
                    }
                )
                uiState.availableProcesses.filter { it.id != "general" }.forEach { process ->
                    DropdownMenuItem(
                        text = { Text(process.name) },
                        onClick = {
                            onEvent(LogProgressUiEvent.ProcessSelected(process.id))
                            processDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Progress Level Selection (LOW, MEDIUM, HIGH)
        Text(
            text = "Nivel Cualitativo de Progreso",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProgressLevel.entries.forEach { level ->
                FilterChip(
                    selected = uiState.progressLevel == level,
                    onClick = { onEvent(LogProgressUiEvent.LevelSelected(level)) },
                    label = { Text(level.label) }
                )
            }
        }

        // Optional Note Input
        OutlinedTextField(
            value = uiState.note,
            onValueChange = { onEvent(LogProgressUiEvent.NoteChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nota de evaluación (opcional)") },
            placeholder = { Text("Reflexión breve o hito alcanzado") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        RumboButton(
            onClick = { onEvent(LogProgressUiEvent.SubmitProgress) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSubmitting
        ) {
            Text("Guardar Evaluación")
        }
    }
}

@Preview(name = "Log Progress Light", showBackground = true)
@Composable
private fun LogProgressScreenPreviewLight() {
    RumboTheme(darkTheme = false) {
        LogProgressScreen(
            uiState = LogProgressUiState(),
            onEvent = {}
        )
    }
}
