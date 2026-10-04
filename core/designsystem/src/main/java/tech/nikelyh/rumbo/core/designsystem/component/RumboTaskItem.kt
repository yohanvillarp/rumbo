package tech.nikelyh.rumbo.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import tech.nikelyh.rumbo.core.designsystem.theme.ProcessColors
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.Priority
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus
import java.time.format.DateTimeFormatter

private val TASK_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, hh:mm a")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RumboTaskItem(
    task: Task,
    onToggleStatus: (Task) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    processColorOrVisualId: String? = null,
    processName: String? = null,
    onStartSession: ((Task) -> Unit)? = null
) {
    val isGeneral = task.processId == "general" ||
        processColorOrVisualId == "system_default" ||
        processColorOrVisualId == null
    val processColor: Color? = if (!isGeneral) ProcessColors.getColor(processColorOrVisualId) else null

    val prioritySpanish = when (task.priority) {
        Priority.LOW -> "Baja"
        Priority.MEDIUM -> "Media"
        Priority.HIGH -> "Alta"
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (processColor != null) {
                        // 0. Left Process Color Accent Strip
                        drawRect(
                            color = processColor,
                            topLeft = Offset(0f, 0f),
                            size = Size(5.dp.toPx(), size.height)
                        )

                        // 1. Soft radial gradient wash in top-right corner
                        val centerPoint = Offset(size.width * 0.95f, size.height * 0.25f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    processColor.copy(alpha = 0.22f),
                                    processColor.copy(alpha = 0.06f),
                                    Color.Transparent
                                ),
                                center = centerPoint,
                                radius = size.width * 0.55f
                            ),
                            center = centerPoint,
                            radius = size.width * 0.55f
                        )

                        // 2. Artistic semitransparent geometric shapes (concentric rings and circular badge)
                        val geomCenter = Offset(size.width - 24.dp.toPx(), 26.dp.toPx())
                        drawCircle(
                            color = processColor.copy(alpha = 0.20f),
                            radius = 50.dp.toPx(),
                            center = geomCenter,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(
                            color = processColor.copy(alpha = 0.12f),
                            radius = 74.dp.toPx(),
                            center = geomCenter,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        drawCircle(
                            color = processColor.copy(alpha = 0.14f),
                            radius = 26.dp.toPx(),
                            center = geomCenter
                        )
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (processColor != null) 18.dp else 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (processName != null && !isGeneral) {
                                    Surface(
                                        color = (processColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = processName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = processColor ?: MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                .widthIn(max = 140.dp)
                                        )
                                    }
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Prioridad: $prioritySpanish",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (processColor != null) processColor.copy(alpha = 0.9f) else MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                val dueEpoch = task.dueDateEpochMillis
                                if (dueEpoch != null) {
                                    val dateText = remember(dueEpoch) {
                                        val instant = java.time.Instant.ofEpochMilli(dueEpoch)
                                        val zone = java.time.ZoneId.systemDefault()
                                        val zonedDateTime = instant.atZone(zone)
                                        zonedDateTime.format(TASK_DATE_FORMATTER)
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Event,
                                                contentDescription = null,
                                                modifier = Modifier.size(11.dp),
                                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Límite: $dateText",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }
                                if (task.timeWorkedMillis > 0L) {
                                    val mins = task.timeWorkedMillis / (1000 * 60)
                                    Text(
                                        text = "•  ${mins}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (task.cost > 0.0) {
                            Text(
                                text = "$${task.cost}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }

                        if (!task.isCompleted && onStartSession != null) {
                            val isTaskStarted = task.timeWorkedMillis > 0L || task.status == TaskStatus.IN_PROGRESS
                            IconButton(
                                onClick = { onStartSession(task) }
                            ) {
                                Icon(
                                    imageVector = if (isTaskStarted) Icons.Default.PlayCircle else Icons.Default.PlayArrow,
                                    contentDescription = if (isTaskStarted) "Continuar Sesión" else "Iniciar Sesión",
                                    tint = processColor ?: MaterialTheme.colorScheme.primary
                                )
                            }
                        }
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
    }
}

@Preview(name = "TaskItem General Light", showBackground = true)
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

@Preview(name = "TaskItem Process Color Light", showBackground = true)
@Composable
private fun RumboTaskItemProcessColorPreview() {
    RumboTheme(darkTheme = false) {
        RumboTaskItem(
            task = Task(
                id = "t2",
                processId = "proc-arch",
                title = "Planificar menú semanal",
                description = "Definir los ingredientes y recetas para los almuerzos de la semana.",
                status = TaskStatus.PENDING,
                priority = Priority.HIGH,
                createdAtEpochMillis = 1000L,
                cost = 50.0
            ),
            processColorOrVisualId = "teal",
            processName = "Hogar",
            onToggleStatus = {},
            onClick = {}
        )
    }
}
