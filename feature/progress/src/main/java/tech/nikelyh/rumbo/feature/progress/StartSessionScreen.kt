package tech.nikelyh.rumbo.feature.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var manualMinutesText by remember { mutableStateOf("") }
    var showCancelConfirmDialog by remember { mutableStateOf(false) }

    if (!uiState.isSessionFinished) {
        // Immersive Fullscreen Timer Mode
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header: Task and Process details (calm and focused)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    val process = uiState.availableProcesses.firstOrNull { it.id == uiState.selectedProcessId }
                    val processName = process?.name ?: "General"

                    if (uiState.selectedTaskTitle.isNotBlank()) {
                        Text(
                            text = uiState.selectedTaskTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = processName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        Text(
                            text = processName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sesión de Trabajo",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Center: Big, Serene Fullscreen Timer Counter
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = formatMillis(uiState.elapsedTimeMillis),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 68.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1.5).sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (uiState.isTimerRunning) "Sesión en curso" else "Sesión en pausa",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.isTimerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }

                // Bottom: Action Controls
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    RumboButton(
                        onClick = { onEvent(StartSessionUiEvent.FinishTimer) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Finalizar Sesión", style = MaterialTheme.typography.titleMedium)
                    }

                    RumboOutlinedButton(
                        onClick = { onEvent(StartSessionUiEvent.ToggleTimer) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (uiState.isTimerRunning) "Pausar" else "Continuar")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onEvent(StartSessionUiEvent.ForgotTimerClicked) }
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Olvidé detenerlo", color = MaterialTheme.colorScheme.secondary)
                        }

                        TextButton(
                            onClick = { showCancelConfirmDialog = true }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cancelar Sesión", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    } else {
        // Session Culmination Screen
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RumboSectionHeader(
                title = "Sesión Finalizada",
                subtitle = "Revisa el tiempo invertido y guarda tu sesión."
            )

            RumboCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tiempo total registrado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatMillis(uiState.elapsedTimeMillis),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (uiState.selectedTaskTitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tarea: ${uiState.selectedTaskTitle}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

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

            TextButton(
                onClick = { showCancelConfirmDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Descartar Sesión", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = { Text("¿Cancelar sesión?") },
            text = { Text("Se descartará el tiempo transcurrido en esta sesión y no se guardará ningún registro.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelConfirmDialog = false
                        onEvent(StartSessionUiEvent.CancelSession)
                    }
                ) {
                    Text("Sí, cancelar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmDialog = false }) {
                    Text("Volver")
                }
            }
        )
    }

    // Dialog for "Olvidé detenerlo"
    if (uiState.showForgotTimerDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(StartSessionUiEvent.DismissForgotTimerDialog) },
            title = { Text("Ajustar Tiempo Invertido") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Introduce la cantidad de minutos reales que trabajaste en esta sesión:")
                    OutlinedTextField(
                        value = manualMinutesText,
                        onValueChange = { input -> if (input.all { it.isDigit() }) manualMinutesText = input },
                        label = { Text("Minutos estimados") },
                        placeholder = { Text("Ej. 45") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (manualMinutesText.isNotBlank()) {
                            onEvent(StartSessionUiEvent.ConfirmManualMinutes(manualMinutesText))
                        }
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(StartSessionUiEvent.DismissForgotTimerDialog) }) {
                    Text("Cancelar")
                }
            }
        )
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
