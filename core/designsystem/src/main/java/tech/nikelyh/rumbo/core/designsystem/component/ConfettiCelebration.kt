package tech.nikelyh.rumbo.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Shape variants for festive confetti particles.
 */
enum class ConfettiShape {
    RECTANGLE,
    CIRCLE,
    RIBBON,
    STAR
}

private data class ConfettiParticle(
    val initialXRatio: Float,
    val initialYRatio: Float,
    val initialVx: Float,
    val initialVy: Float,
    val gravity: Float,
    val color: Color,
    val shape: ConfettiShape,
    val size: Float,
    val rotationSpeed: Float,
    val windFactor: Float
)

/**
 * High-performance Compose Canvas confetti particle celebration.
 * Emits physics-driven festive particles with gravity, flutter, rotation, and alpha fade.
 */
@Composable
fun ConfettiCelebration(
    modifier: Modifier = Modifier,
    particleCount: Int = 80,
    durationMillis: Int = 2600,
    onFinished: (() -> Unit)? = null
) {
    val progress = remember { Animatable(0f) }

    val celebrationPalette = remember {
        listOf(
            Color(0xFFFFB300), // Amber Gold
            Color(0xFF00B4D8), // Vibrant Cyan
            Color(0xFF7209B7), // Royal Violet
            Color(0xFFF72585), // Neon Rose
            Color(0xFF4CC9F0), // Sky Blue
            Color(0xFF10B981), // Emerald
            Color(0xFFFF7A00)  // Deep Orange
        )
    }

    val particles = remember(particleCount) {
        val random = Random(42)
        List(particleCount) {
            val startFromSide = random.nextBoolean()
            val initialX = if (startFromSide) {
                if (random.nextBoolean()) random.nextFloat() * 0.25f else 0.75f + random.nextFloat() * 0.25f
            } else {
                random.nextFloat()
            }
            val initialY = random.nextFloat() * 0.2f

            ConfettiParticle(
                initialXRatio = initialX,
                initialYRatio = initialY,
                initialVx = (random.nextFloat() - 0.5f) * 650f,
                initialVy = random.nextFloat() * 250f + 150f,
                gravity = random.nextFloat() * 550f + 350f,
                color = celebrationPalette[random.nextInt(celebrationPalette.size)],
                shape = ConfettiShape.entries[random.nextInt(ConfettiShape.entries.size)],
                size = random.nextFloat() * 12f + 8f,
                rotationSpeed = (random.nextFloat() - 0.5f) * 720f,
                windFactor = random.nextFloat() * 2.5f + 1f
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing)
        )
        onFinished?.invoke()
    }

    val currentProgress = progress.value

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val t = currentProgress

        // Alpha fades smoothly toward the end of celebration
        val alpha = if (t > 0.7f) {
            ((1f - t) / 0.3f).coerceIn(0f, 1f)
        } else {
            1f
        }

        particles.forEach { p ->
            val startX = p.initialXRatio * width
            val startY = p.initialYRatio * height

            val timeSec = t * (durationMillis / 1000f)
            val currentX = startX + p.initialVx * timeSec + (sin((timeSec * p.windFactor).toDouble()).toFloat() * 40f)
            val currentY = startY + (p.initialVy * timeSec) + (0.5f * p.gravity * timeSec * timeSec)
            val currentRotation = p.rotationSpeed * timeSec

            if (currentY <= height + 40f && currentX in -50f..(width + 50f)) {
                val particleColor = p.color.copy(alpha = alpha)

                rotate(degrees = currentRotation, pivot = Offset(currentX, currentY)) {
                    when (p.shape) {
                        ConfettiShape.CIRCLE -> {
                            drawCircle(
                                color = particleColor,
                                radius = p.size / 2f,
                                center = Offset(currentX, currentY)
                            )
                        }
                        ConfettiShape.RECTANGLE -> {
                            drawRect(
                                color = particleColor,
                                topLeft = Offset(currentX - p.size / 2f, currentY - p.size / 4f),
                                size = Size(p.size, p.size / 2f)
                            )
                        }
                        ConfettiShape.RIBBON -> {
                            drawRect(
                                color = particleColor,
                                topLeft = Offset(currentX - p.size / 4f, currentY - p.size),
                                size = Size(p.size / 2f, p.size * 1.6f)
                            )
                        }
                        ConfettiShape.STAR -> {
                            val starPath = Path().apply {
                                val points = 5
                                val outerRadius = p.size / 1.5f
                                val innerRadius = outerRadius / 2f
                                val cx = currentX
                                val cy = currentY

                                for (i in 0 until points * 2) {
                                    val r = if (i % 2 == 0) outerRadius else innerRadius
                                    val angle = (i * PI / points) - (PI / 2)
                                    val px = cx + (r * cos(angle)).toFloat()
                                    val py = cy + (r * sin(angle)).toFloat()
                                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                                }
                                close()
                            }
                            drawPath(path = starPath, color = particleColor)
                        }
                    }
                }
            }
        }
    }
}
