package tech.nikelyh.rumbo.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.Priority
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus

@Composable
fun RumboTaskItem(
    task: Task,
    onToggleStatus: (Task) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prioritySpanish = when (task.priority) {
        Priority.LOW -> "Baja"
        Priority.MEDIUM -> "Media"
        Priority.HIGH -> "Alta"
    }

    RumboCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleStatus(task) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Prioridad: $prioritySpanish",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            if (task.cost > 0.0) {
                Text(
                    text = "$${task.cost}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        AnimatedVisibility(visible = !task.description.isNullOrBlank()) {
            task.description?.let { desc ->
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Preview(name = "TaskItem Light", showBackground = true)
@Composable
private fun RumboTaskItemPreviewLight() {
    RumboTheme(darkTheme = false) {
        RumboTaskItem(
            task = Task(
                id = "t1",
                processId = "general",
                title = "Configurar componentes del Design System",
                description = "Crear botones, tarjetas, topbars e indicadores de progreso.",
                status = TaskStatus.PENDING,
                priority = Priority.HIGH,
                createdAtEpochMillis = 1000L,
                cost = 0.0
            ),
            onToggleStatus = {},
            onClick = {}
        )
    }
}
