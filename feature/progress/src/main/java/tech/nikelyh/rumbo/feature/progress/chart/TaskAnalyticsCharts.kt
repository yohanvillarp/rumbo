package tech.nikelyh.rumbo.feature.progress.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tech.nikelyh.rumbo.core.designsystem.component.RumboCard
import tech.nikelyh.rumbo.feature.progress.TaskAnalyticsData

@Composable
fun TaskCompletionGauge(
    data: TaskAnalyticsData,
    modifier: Modifier = Modifier
) {
    val total = data.totalTasksCount.coerceAtLeast(1)
    val completionPercentage = ((data.completedTasksCount.toFloat() / total.toFloat()) * 100f).coerceIn(0f, 100f)

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(completionPercentage) {
        animProgress.animateTo(
            targetValue = completionPercentage,
            animationSpec = tween(durationMillis = 900)
        )
    }

    val completedColor = Color(0xFF2E7D32)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(190.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(170.dp)) {
                val strokeWidth = 20.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val topLeft = Offset(
                    (size.width - radius * 2) / 2,
                    (size.height - radius * 2) / 2
                )
                val arcSize = Size(radius * 2, radius * 2)

                // Background track
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )

                // Animated progress arc
                val sweep = (animProgress.value / 100f) * 360f
                if (sweep > 0f) {
                    drawArc(
                        color = completedColor,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // Center metric text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${animProgress.value.toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.metric_completed_tasks),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun TaskDistributionBar(
    data: TaskAnalyticsData,
    modifier: Modifier = Modifier
) {
    val total = data.totalTasksCount.coerceAtLeast(1)
    val completedRatio = data.completedTasksCount.toFloat() / total.toFloat()
    val onTimePendingCount = (data.pendingTasksCount - data.overdueTasksCount).coerceAtLeast(0)
    val onTimePendingRatio = onTimePendingCount.toFloat() / total.toFloat()
    val overdueRatio = data.overdueTasksCount.toFloat() / total.toFloat()

    val completedColor = Color(0xFF2E7D32)
    val pendingColor = Color(0xFF0F5257)
    val overdueColor = Color(0xFFC62828)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Multi-segment horizontal bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                if (completedRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(completedRatio)
                            .background(completedColor)
                    )
                }
                if (onTimePendingRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(onTimePendingRatio)
                            .background(pendingColor)
                    )
                }
                if (overdueRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(overdueRatio)
                            .background(overdueColor)
                    )
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendIndicator(
                color = completedColor,
                label = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.metric_completed_count, data.completedTasksCount)
            )
            val pendingLabel = if (data.overdueTasksCount > 0) {
                androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.metric_on_time_tasks, onTimePendingCount)
            } else {
                androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.metric_pending_count, data.pendingTasksCount)
            }
            LegendIndicator(
                color = pendingColor,
                label = pendingLabel
            )
            if (data.overdueTasksCount > 0) {
                LegendIndicator(
                    color = overdueColor,
                    label = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.metric_overdue_count, data.overdueTasksCount)
                )
            }
        }
    }
}

@Composable
private fun LegendIndicator(
    color: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TaskMetricTiles(
    data: TaskAnalyticsData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.metric_completed_tasks),
                value = data.completedTasksCount.toString(),
                icon = Icons.Default.CheckCircle,
                iconTint = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.metric_pending_tasks),
                value = data.pendingTasksCount.toString(),
                icon = Icons.Default.Schedule,
                iconTint = Color(0xFF0F5257),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.metric_overdue_tasks),
                value = data.overdueTasksCount.toString(),
                icon = Icons.Default.Warning,
                iconTint = if (data.overdueTasksCount > 0) Color(0xFFC62828) else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.metric_total_tasks),
                value = data.totalTasksCount.toString(),
                icon = Icons.AutoMirrored.Filled.Assignment,
                iconTint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    RumboCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
