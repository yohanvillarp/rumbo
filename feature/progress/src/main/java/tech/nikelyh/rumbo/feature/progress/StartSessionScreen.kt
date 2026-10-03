package tech.nikelyh.rumbo.feature.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikelyh.rumbo.core.designsystem.component.RumboButton
import tech.nikelyh.rumbo.core.designsystem.component.RumboCard
import tech.nikelyh.rumbo.core.designsystem.component.RumboOutlinedButton
import tech.nikelyh.rumbo.core.designsystem.component.RumboSectionHeader
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.ProgressLevel
import java.util.Locale

@Composable
fun StartSessionRoute(
    onSessionFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StartSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSessionFinished()
        }
    }

    StartSessionScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
internal fun StartSessionScreen(
    uiState: StartSessionUiState,
    onEvent: (StartSessionUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RumboSectionHeader(
            title = "Sesión de Trabajo",
            subtitle = "Sigue tu tiempo de concentración de forma serena y sin interrupciones."
        )

        // Process Selection
        Text(
            text = "Proceso",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedProcessId == "general",
                onClick = { onEvent(StartSessionUiEvent.ProcessSelected("general")) },
                label = { Text("General") }
            )
            uiState.availableProcesses.filter { it.id != "general" }.take(3).forEach { process ->
                FilterChip(
                    selected = uiState.selectedProcessId == process.id,
                    onClick = { onEvent(StartSessionUiEvent.ProcessSelected(process.id)) },
                    label = { Text(process.name) }
                )
            }
        }

        // Timer Display Card
        RumboCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatMillis(uiState.elapsedTimeMillis),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = MaterialTheme.typography.titleLarge.fontSize * 1.8f
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RumboOutlinedButton(
                        onClick = { onEvent(StartSessionUiEvent.ToggleTimer) }
                    ) {
                        Icon(
                            imageVector = if (uiState.isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (uiState.isTimerRunning) "Pausar" else "Continuar")
                    }

                    RumboButton(
                        onClick = { onEvent(StartSessionUiEvent.FinishTimer) }
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Finalizar")
                    }
                }
            }
        }

        // Session Summary Section (visible when finished or pausing)
        AnimatedVisibility(visible = uiState.isSessionFinished) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.sessionNote,
                    onValueChange = { onEvent(StartSessionUiEvent.NoteChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nota de la sesión (opcional)") },
                    placeholder = { Text("¿Qué lograste durante esta sesión?") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    maxLines = 3
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.saveProgressEntry,
                        onCheckedChange = { onEvent(StartSessionUiEvent.ToggleSaveProgress(it)) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Registrar nivel de progreso cualitativo",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (uiState.saveProgressEntry) {
                    Text(
                        text = "Nivel de Progreso",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProgressLevel.entries.forEach { level ->
                            FilterChip(
                                selected = uiState.progressLevel == level,
                                onClick = { onEvent(StartSessionUiEvent.ProgressLevelSelected(level)) },
                                label = { Text(level.label) }
                            )
                        }
                    }
                }

                RumboButton(
                    onClick = { onEvent(StartSessionUiEvent.SubmitSession) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSubmitting
                ) {
                    Text("Guardar Sesión")
                }
            }
        }
    }
}

private fun formatMillis(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}

@Preview(name = "Start Session Light", showBackground = true)
@Composable
private fun StartSessionScreenPreviewLight() {
    RumboTheme(darkTheme = false) {
        StartSessionScreen(
            uiState = StartSessionUiState(elapsedTimeMillis = 3600000L + 120000L),
            onEvent = {}
        )
    }
}
