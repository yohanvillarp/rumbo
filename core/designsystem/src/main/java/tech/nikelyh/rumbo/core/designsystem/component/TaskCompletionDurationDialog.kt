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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun TaskCompletionDurationDialog(
    taskTitle: String,
    onConfirm: (durationMinutes: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var minutesText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Culminar Tarea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Para culminar '$taskTitle', ingresa el tiempo real dedicado a esta tarea:",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            minutesText = input
                            isError = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Duración (minutos) *") },
                    placeholder = { Text("Ej. 30") },
                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                    singleLine = true,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(
                                text = "Ingresa una duración en minutos mayor a 0",
                                color = MaterialTheme.colorScheme.error,
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
            TextButton(
                onClick = {
                    val parsed = minutesText.trim().toLongOrNull()
                    if (parsed != null && parsed > 0L) {
                        onConfirm(parsed)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Culminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
