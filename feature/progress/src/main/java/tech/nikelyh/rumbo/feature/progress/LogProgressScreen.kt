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

@Composable
internal fun LogProgressScreen(
    uiState: LogProgressUiState,
    onEvent: (LogProgressUiEvent) -> Unit,
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
            title = "Registrar Progreso",
            subtitle = "Evalúa cualitativamente el avance de tu proceso sin requerir una sesión previa."
        )

        // Process Selection
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
                onClick = { onEvent(LogProgressUiEvent.ProcessSelected("general")) },
                label = { Text("General") }
            )
            uiState.availableProcesses.filter { it.id != "general" }.take(3).forEach { process ->
                FilterChip(
                    selected = uiState.selectedProcessId == process.id,
                    onClick = { onEvent(LogProgressUiEvent.ProcessSelected(process.id)) },
                    label = { Text(process.name) }
                )
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
