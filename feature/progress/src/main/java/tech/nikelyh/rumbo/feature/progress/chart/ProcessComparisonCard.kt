package tech.nikelyh.rumbo.feature.progress.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tech.nikelyh.rumbo.core.designsystem.component.RumboCard
import tech.nikelyh.rumbo.core.designsystem.theme.ProcessColors
import tech.nikelyh.rumbo.feature.progress.ProcessProgressComparison
import java.util.Locale

@Composable
fun ProcessComparisonCard(
    comparison: ProcessProgressComparison,
    modifier: Modifier = Modifier
) {
    val processColor = ProcessColors.getColor(comparison.colorOrVisualId)

    // Map declared progress label to level 1..4
    val level = when (comparison.declaredProgressLevelLabel.lowercase()) {
        "sin avance", "inicio", "bajo" -> 1
        "moderado", "medio" -> 2
        "alto", "avanzado", "significativo" -> 3
        "completado", "consolidado", "máximo" -> 4
        else -> 2
    }

    val levelNames = listOf(
        androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.progress_level_initial),
        androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.progress_level_medium),
        androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.progress_level_high),
        androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.progress_level_consolidated)
    )

    RumboCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with process indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(processColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = comparison.processName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Hours Tag Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(processColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = processColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", comparison.timeInvestedHours)}h",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = processColor
                    )
                }
            }

            // Qualitative Progress Visual Level Meter (4 Segments)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = androidx.compose.ui.res.stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.metric_qualitative_declared),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Text(
                        text = comparison.declaredProgressLevelLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // 4-segmented visual bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..4) {
                        val isFilled = i <= level
                        val isTarget = i == level
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isTarget) processColor
                                    else if (isFilled) processColor.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                // Labels below segments
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    levelNames.forEachIndexed { idx, name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (idx + 1 == level) processColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontWeight = if (idx + 1 == level) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
