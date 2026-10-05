package tech.nikelyh.rumbo.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class ProcessCelebrationVariation(
    val title: String,
    val message: String,
    val buttonText: String
)

/**
 * Enterprise UX celebratory cinematic dialog presented upon process completion.
 * Features an expansive motivating panel with 3 dynamic inspirational variations,
 * the Rumbo brand logo in the center, and a concordant action button.
 */
@Composable
fun CelebrationCinematicDialog(
    processName: String,
    title: String? = null,
    subtitle: String? = null,
    totalTimeFormatted: String? = null,
    totalCostFormatted: String? = null,
    completedTasksCount: Int? = null,
    onDismiss: () -> Unit
) {
    var contentVisible by remember { mutableStateOf(false) }

    val variations = remember(processName) {
        listOf(
            ProcessCelebrationVariation(
                title = "¡Has completado \"$processName\"!",
                message = "¡Imparable! Cada proceso culminado consolida tu disciplina y te acerca a tus metas más ambiciosas.",
                buttonText = "¡Continuar imparable!"
            ),
            ProcessCelebrationVariation(
                title = "¡Misión Cumplida en \"$processName\"!",
                message = "¡Extraordinario trabajo! Has demostrado enfoque y constancia de inicio a fin. ¡Celebra tu conquista!",
                buttonText = "¡A por el siguiente reto!"
            ),
            ProcessCelebrationVariation(
                title = "¡Excelente logro con \"$processName\"!",
                message = "¡Objetivo alcanzado con maestría! Tu constancia transforma planes en realidades tangibles.",
                buttonText = "¡Seguir creciendo!"
            )
        )
    }

    val selectedVariation = remember(processName) {
        val hash = kotlin.math.abs(processName.hashCode())
        variations[hash % variations.size]
    }

    val effectiveTitle = title ?: selectedVariation.title
    val effectiveSubtitle = subtitle ?: selectedVariation.message
    val effectiveButtonText = selectedVariation.buttonText

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            // Background Confetti Particle Burst
            ConfettiCelebration(
                particleCount = 100,
                durationMillis = 3400
            )

            AnimatedVisibility(
                visible = contentVisible,
                enter = scaleIn(
                    initialScale = 0.72f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Trophy / Crown badge
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFFFFB300).copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(1.dp, Color(0xFFFFB300).copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Brand Logo in the middle
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        RumboLogo(
                            size = 96.dp,
                            state = MascotState.SUCCESS,
                            showSubtitle = false
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Motivating Title
                    Text(
                        text = effectiveTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Motivating Subtitle
                    Text(
                        text = effectiveSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Metric Recap Grid (Time, Investment, Tasks)
                    val hasMetrics = totalTimeFormatted != null || totalCostFormatted != null || completedTasksCount != null
                    if (hasMetrics) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (totalTimeFormatted != null) {
                                MetricRecapTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Schedule,
                                    label = "Tiempo",
                                    value = totalTimeFormatted,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (totalCostFormatted != null) {
                                MetricRecapTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.AttachMoney,
                                    label = "Inversión",
                                    value = totalCostFormatted,
                                    tint = Color(0xFF2E7D32)
                                )
                            }
                            if (completedTasksCount != null) {
                                MetricRecapTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.CheckCircle,
                                    label = "Tareas",
                                    value = "$completedTasksCount",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))
                    }

                    // Celebratory Confirmation Button concordant with the motivation text
                    RumboButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(effectiveButtonText)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRecapTile(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
