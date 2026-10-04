package tech.nikelyh.rumbo.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Brand Emblem & Animated Mascot for Rumbo.
 * Embodies the celestial guiding compass (Estrella del Rumbo & Astrolabio)
 * with fluid micro-animations reflecting the user's focus and state.
 */
@Composable
fun RumboMascot(
    modifier: Modifier = Modifier,
    state: MascotState = MascotState.DEFAULT,
    size: Dp = 96.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface

    val mainColor by animateColorAsState(
        targetValue = when (state) {
            MascotState.DEFAULT -> primaryColor
            MascotState.FOCUSED -> primaryColor
            MascotState.RESTING -> secondaryColor
            MascotState.PROGRESS -> tertiaryColor
            MascotState.SUCCESS -> primaryColor
        },
        animationSpec = tween(600),
        label = "mascotMainColor"
    )

    val shadowColor = mainColor.copy(alpha = 0.55f)
    val ringColor = mainColor.copy(alpha = 0.28f)

    val infiniteTransition = rememberInfiniteTransition(label = "rumboEmblemAnimations")

    // 1. Serene breathing / float scale
    val breathScale by infiniteTransition.animateFloat(
        initialValue = if (state == MascotState.RESTING) 0.94f else 0.97f,
        targetValue = if (state == MascotState.RESTING) 1.01f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == MascotState.RESTING) 3200 else 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    // 2. Subtle directional compass needle sway (oscillates smoothly like magnetic compass settling)
    val needleAngle by infiniteTransition.animateFloat(
        initialValue = when (state) {
            MascotState.FOCUSED -> 0f
            MascotState.PROGRESS -> 0f
            else -> -6f
        },
        targetValue = when (state) {
            MascotState.FOCUSED -> 0f
            MascotState.PROGRESS -> 360f
            else -> 6f
        },
        animationSpec = if (state == MascotState.PROGRESS) {
            infiniteRepeatable(
                animation = tween(durationMillis = 6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        },
        label = "needleAngle"
    )

    // 3. Radial pulse ring expansion for active focus / progress
    val pulseRatio by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRatio"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = size.toPx()
        val h = size.toPx()
        val cx = w / 2f
        val cy = h / 2f
        val radius = w * 0.42f
        val innerRadius = radius * 0.30f
        val secRadius = radius * 0.58f

        scale(breathScale, pivot = Offset(cx, cy)) {
            // A. Radial soft aura glow in background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        mainColor.copy(alpha = if (state == MascotState.FOCUSED) 0.20f else 0.10f),
                        mainColor.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = radius * 1.5f
                ),
                center = Offset(cx, cy),
                radius = radius * 1.5f
            )

            // B. Expanding pulse ripple for FOCUSED or PROGRESS states
            if (state == MascotState.FOCUSED || state == MascotState.PROGRESS) {
                val rippleRadius = radius * (1.1f + 0.4f * pulseRatio)
                val rippleAlpha = (1f - pulseRatio) * 0.22f
                drawCircle(
                    color = mainColor.copy(alpha = rippleAlpha),
                    radius = rippleRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // C. Outer Navigational Ring with Cardinal Ticks
            val outerRingRadius = radius * 1.08f
            drawCircle(
                color = ringColor,
                radius = outerRingRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Subtle dashed inner ring
            drawCircle(
                color = ringColor.copy(alpha = 0.15f),
                radius = outerRingRadius * 0.85f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.dp.toPx())
            )

            // Cardinal tick marks (N, S, E, W)
            val tickLength = radius * 0.12f
            drawLine(
                color = mainColor.copy(alpha = 0.6f),
                start = Offset(cx, cy - outerRingRadius - tickLength),
                end = Offset(cx, cy - outerRingRadius + 2.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = mainColor.copy(alpha = 0.4f),
                start = Offset(cx, cy + outerRingRadius - 2.dp.toPx()),
                end = Offset(cx, cy + outerRingRadius + tickLength),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = mainColor.copy(alpha = 0.4f),
                start = Offset(cx - outerRingRadius - tickLength, cy),
                end = Offset(cx - outerRingRadius + 2.dp.toPx(), cy),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = mainColor.copy(alpha = 0.4f),
                start = Offset(cx + outerRingRadius - 2.dp.toPx(), cy),
                end = Offset(cx + outerRingRadius + tickLength, cy),
                strokeWidth = 1.5.dp.toPx()
            )

            // D. Rotatable Faceted Compass Star (North Star of Rumbo)
            rotate(degrees = needleAngle, pivot = Offset(cx, cy)) {
                // Secondary 4 diagonal points (NE, NW, SE, SW)
                for (i in 0 until 4) {
                    val angle = 45.0 + i * 90.0
                    val rad = Math.toRadians(angle)
                    val radPrev = Math.toRadians(angle - 22.5)
                    val radNext = Math.toRadians(angle + 22.5)

                    val tip = Offset((cx + cos(rad) * secRadius).toFloat(), (cy + sin(rad) * secRadius).toFloat())
                    val p1 = Offset((cx + cos(radPrev) * innerRadius * 0.85f).toFloat(), (cy + sin(radPrev) * innerRadius * 0.85f).toFloat())
                    val p2 = Offset((cx + cos(radNext) * innerRadius * 0.85f).toFloat(), (cy + sin(radNext) * innerRadius * 0.85f).toFloat())

                    val facet1 = Path().apply {
                        moveTo(cx, cy)
                        lineTo(p1.x, p1.y)
                        lineTo(tip.x, tip.y)
                        close()
                    }
                    val facet2 = Path().apply {
                        moveTo(cx, cy)
                        lineTo(tip.x, tip.y)
                        lineTo(p2.x, p2.y)
                        close()
                    }
                    drawPath(path = facet1, color = mainColor.copy(alpha = 0.35f))
                    drawPath(path = facet2, color = shadowColor.copy(alpha = 0.22f))
                }

                // Primary 4 points (N, S, E, W) - Faceted 3D Origami Appearance
                val northTip = Offset(cx, cy - radius)
                val eastTip = Offset(cx + radius * 0.82f, cy)
                val southTip = Offset(cx, cy + radius * 0.72f)
                val westTip = Offset(cx - radius * 0.82f, cy)

                // Valleys
                val neValley = Offset(cx + innerRadius * 0.7f, cy - innerRadius * 0.7f)
                val seValley = Offset(cx + innerRadius * 0.7f, cy + innerRadius * 0.7f)
                val swValley = Offset(cx - innerRadius * 0.7f, cy + innerRadius * 0.7f)
                val nwValley = Offset(cx - innerRadius * 0.7f, cy - innerRadius * 0.7f)

                // North Facets
                val northLight = Path().apply {
                    moveTo(cx, cy)
                    lineTo(nwValley.x, nwValley.y)
                    lineTo(northTip.x, northTip.y)
                    close()
                }
                val northDark = Path().apply {
                    moveTo(cx, cy)
                    lineTo(northTip.x, northTip.y)
                    lineTo(neValley.x, neValley.y)
                    close()
                }
                drawPath(northLight, color = mainColor)
                drawPath(northDark, color = shadowColor)

                // East Facets
                val eastLight = Path().apply {
                    moveTo(cx, cy)
                    lineTo(neValley.x, neValley.y)
                    lineTo(eastTip.x, eastTip.y)
                    close()
                }
                val eastDark = Path().apply {
                    moveTo(cx, cy)
                    lineTo(eastTip.x, eastTip.y)
                    lineTo(seValley.x, seValley.y)
                    close()
                }
                drawPath(eastLight, color = mainColor.copy(alpha = 0.85f))
                drawPath(eastDark, color = shadowColor.copy(alpha = 0.85f))

                // South Facets
                val southLight = Path().apply {
                    moveTo(cx, cy)
                    lineTo(seValley.x, seValley.y)
                    lineTo(southTip.x, southTip.y)
                    close()
                }
                val southDark = Path().apply {
                    moveTo(cx, cy)
                    lineTo(southTip.x, southTip.y)
                    lineTo(swValley.x, swValley.y)
                    close()
                }
                drawPath(southLight, color = mainColor.copy(alpha = 0.75f))
                drawPath(southDark, color = shadowColor.copy(alpha = 0.75f))

                // West Facets
                val westLight = Path().apply {
                    moveTo(cx, cy)
                    lineTo(swValley.x, swValley.y)
                    lineTo(westTip.x, westTip.y)
                    close()
                }
                val westDark = Path().apply {
                    moveTo(cx, cy)
                    lineTo(westTip.x, westTip.y)
                    lineTo(nwValley.x, nwValley.y)
                    close()
                }
                drawPath(westLight, color = mainColor.copy(alpha = 0.85f))
                drawPath(westDark, color = shadowColor.copy(alpha = 0.85f))

                // E. Center Luminous Core Pivot
                drawCircle(
                    color = surfaceColor,
                    radius = innerRadius * 0.45f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = mainColor,
                    radius = innerRadius * 0.30f,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}

@Preview(name = "Mascot Default Light", showBackground = true)
@Composable
private fun RumboMascotPreviewLight() {
    RumboTheme(darkTheme = false) {
        RumboMascot(state = MascotState.DEFAULT)
    }
}

@Preview(name = "Mascot Focused Dark", showBackground = true)
@Composable
private fun RumboMascotPreviewDark() {
    RumboTheme(darkTheme = true) {
        RumboMascot(state = MascotState.FOCUSED)
    }
}
