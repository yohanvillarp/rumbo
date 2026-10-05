package tech.nikelyh.rumbo.feature.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
    onSessionPaused: (taskId: String?, processId: String?) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: StartSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSessionFinished()
        }
    }

    LaunchedEffect(uiState.isSessionPaused) {
        if (uiState.isSessionPaused) {
            onSessionPaused(uiState.selectedTaskId, uiState.selectedProcessId)
            viewModel.onEvent(StartSessionUiEvent.ResetPausedState)
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

    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

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
                // Header: Task and Process details (stable layout, never swaps positions)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    val process = uiState.availableProcesses.firstOrNull { it.id == uiState.selectedProcessId }
                    val processName = process?.name ?: "General"
                    val defaultSessionTitle = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_title_default)
                    val titleText = if (uiState.selectedTaskTitle.isNotBlank()) uiState.selectedTaskTitle else defaultSessionTitle

                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = processName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }

                // Center: Big, Serene Fullscreen Timer Counter with Tabular Numbers (tnum)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = formatMillis(uiState.elapsedTimeMillis),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val sessionStatusText = if (uiState.isTimerRunning) {
                            androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_status_running)
                        } else {
                            androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_status_paused)
                        }
                        Text(
                            text = sessionStatusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (uiState.isTimerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
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
                        Text(
                            text = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_action_finish),
                            style = MaterialTheme.typography.titleMedium
                        )
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
                        val toggleText = if (uiState.isTimerRunning) {
                            androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_action_pause)
                        } else {
                            androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_action_continue)
                        }
                        Text(toggleText)
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
                            Text(
                                text = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_action_forgot),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        TextButton(
                            onClick = { showCancelConfirmDialog = true }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_action_cancel),
                                color = MaterialTheme.colorScheme.error
                            )
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
                title = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_culmination_title),
                subtitle = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_culmination_subtitle)
            )

            RumboCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_total_time_recorded),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatMillis(uiState.elapsedTimeMillis),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (uiState.selectedTaskTitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_task_prefix, uiState.selectedTaskTitle),
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
                label = { Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_note_label)) },
                placeholder = { Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_note_placeholder)) },
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
                    text = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_qualitative_checkbox),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (uiState.saveProgressEntry) {
                Text(
                    text = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_qualitative_level_label),
                    style = MaterialTheme.typography.labelSmall
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProgressLevel.entries.forEach { level ->
                        val levelLabel = when (level) {
                            ProgressLevel.LOW -> androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.progress_level_low)
                            ProgressLevel.MEDIUM -> androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.progress_level_medium)
                            ProgressLevel.HIGH -> androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.progress_level_high)
                        }
                        FilterChip(
                            selected = uiState.progressLevel == level,
                            onClick = { onEvent(StartSessionUiEvent.ProgressLevelSelected(level)) },
                            label = { Text(levelLabel) }
                        )
                    }
                }
            }

            if (uiState.selectedTaskId != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEvent(StartSessionUiEvent.ToggleCompleteTask(!uiState.markTaskAsCompleted)) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Checkbox(
                            checked = uiState.markTaskAsCompleted,
                            onCheckedChange = { onEvent(StartSessionUiEvent.ToggleCompleteTask(it)) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_complete_task_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            val taskName = uiState.selectedTaskTitle.ifBlank { "Tarea" }
                            Text(
                                text = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_complete_task_subtitle, taskName),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            RumboButton(
                onClick = { onEvent(StartSessionUiEvent.SubmitSession) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting
            ) {
                val saveButtonText = if (uiState.markTaskAsCompleted) {
                    androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_save_and_complete)
                } else {
                    androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_save)
                }
                Text(saveButtonText)
            }

            TextButton(
                onClick = { showCancelConfirmDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_discard),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = { Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_cancel_dialog_title)) },
            text = { Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_cancel_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelConfirmDialog = false
                        onEvent(StartSessionUiEvent.CancelSession)
                    }
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_cancel_dialog_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmDialog = false }) {
                    Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_cancel_dialog_back))
                }
            }
        )
    }

    // Dialog for "Olvidé detenerlo"
    if (uiState.showForgotTimerDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(StartSessionUiEvent.DismissForgotTimerDialog) },
            title = { Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_adjust_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_adjust_dialog_message))
                    OutlinedTextField(
                        value = manualMinutesText,
                        onValueChange = { input -> if (input.all { it.isDigit() }) manualMinutesText = input },
                        label = { Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_adjust_minutes_label)) },
                        placeholder = { Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_adjust_minutes_placeholder)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            if (manualMinutesText.isNotBlank()) {
                                onEvent(StartSessionUiEvent.AdjustMinutesAndResume(manualMinutesText))
                            }
                        }
                    ) {
                        Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_adjust_and_continue))
                    }
                    Button(
                        onClick = {
                            if (manualMinutesText.isNotBlank()) {
                                onEvent(StartSessionUiEvent.ConfirmManualMinutes(manualMinutesText))
                            }
                        }
                    ) {
                        Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.session_adjust_and_culminate))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(StartSessionUiEvent.DismissForgotTimerDialog) }) {
                    Text(androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.action_cancel))
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
            uiState = StartSessionUiState(
                isLoading = false,
                elapsedTimeMillis = 3600000L + 120000L,
                selectedTaskTitle = "Organizar documentos del hogar"
            ),
            onEvent = {}
        )
    }
}
