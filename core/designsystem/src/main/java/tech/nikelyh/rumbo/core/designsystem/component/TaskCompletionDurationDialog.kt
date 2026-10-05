package tech.nikelyh.rumbo.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import tech.nikelyh.rumbo.core.designsystem.R

@Composable
fun TaskCompletionDurationDialog(
    taskTitle: String,
    initialMinutes: Long = 0L,
    minMinutes: Long = 1L,
    onConfirm: (durationMinutes: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val effectiveMin = maxOf(1L, minMinutes)
    val defaultText = if (initialMinutes > 0L) {
        initialMinutes.toString()
    } else if (minMinutes > 1L) {
        minMinutes.toString()
    } else {
        ""
    }
    var minutesText by remember { mutableStateOf(defaultText) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val minErrorMsg = stringResource(R.string.task_duration_dialog_min_error, effectiveMin)
    val zeroErrorMsg = stringResource(R.string.task_duration_dialog_zero_error)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.task_duration_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (effectiveMin > 1L) {
                    Text(
                        text = stringResource(R.string.task_duration_dialog_sessions_msg, effectiveMin),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = stringResource(R.string.task_duration_dialog_manual_msg, taskTitle),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            minutesText = input
                            val parsed = input.trim().toLongOrNull()
                            if (parsed != null && parsed < effectiveMin) {
                                errorMessage = if (effectiveMin > 1L) minErrorMsg else zeroErrorMsg
                            } else {
                                errorMessage = null
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.task_duration_dialog_label)) },
                    placeholder = {
                        Text(if (effectiveMin > 1L) effectiveMin.toString() else stringResource(R.string.task_duration_dialog_placeholder_default))
                    },
                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall
                            )
                        } else if (effectiveMin > 1L) {
                            Text(
                                text = stringResource(R.string.task_duration_dialog_min_registered, effectiveMin),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
        },
        confirmButton = {
            val currentParsed = minutesText.trim().toLongOrNull()
            val isValid = currentParsed != null && currentParsed >= effectiveMin

            TextButton(
                onClick = {
                    if (currentParsed != null && currentParsed >= effectiveMin) {
                        onConfirm(currentParsed)
                    } else {
                        errorMessage = if (effectiveMin > 1L) minErrorMsg else zeroErrorMsg
                    }
                },
                enabled = isValid
            ) {
                Text(stringResource(R.string.task_duration_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
