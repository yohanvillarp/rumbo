package tech.nikelyh.rumbo.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme

@Composable
fun RumboMascot(
    modifier: Modifier = Modifier,
    state: MascotState = MascotState.DEFAULT,
    size: Dp = 96.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val accentColor by animateColorAsState(
        targetValue = when (state) {
            MascotState.DEFAULT -> primaryColor
            MascotState.FOCUSED -> MaterialTheme.colorScheme.tertiary
            MascotState.RESTING -> secondaryColor
            MascotState.PROGRESS -> primaryColor
            MascotState.SUCCESS -> MaterialTheme.colorScheme.primary
        },
        label = "mascotAccentColor"
    )

    Canvas(modifier = modifier.size(size)) {
        val width = size.toPx()
        val height = size.toPx()
        val cx = width / 2f
        val cy = height / 2f
        val strokeWidth = width * 0.035f

        // Minimalist Wolf Head Path (Clean geometric polygon)
        val headPath = Path().apply {
            // Left Ear Tip
            moveTo(cx - width * 0.32f, cy - height * 0.38f)
            // Left Temple
            lineTo(cx - width * 0.16f, cy - height * 0.12f)
            // Snout Tip (Muzzle)
            lineTo(cx, cy + height * 0.35f)
            // Right Temple
            lineTo(cx + width * 0.16f, cy - height * 0.12f)
            // Right Ear Tip
            lineTo(cx + width * 0.32f, cy - height * 0.38f)
            // Crown / Forehead
            lineTo(cx, cy - height * 0.22f)
            close()
        }

        drawPath(
            path = headPath,
            color = accentColor,
            style = Stroke(width = strokeWidth)
        )

        // Serene Eyes (Attentive horizontal/slanted lines)
        val eyeWidth = width * 0.08f
        val eyeY = cy - height * 0.04f

        // Left Eye
        drawLine(
            color = accentColor,
            start = Offset(cx - width * 0.18f, eyeY),
            end = Offset(cx - width * 0.18f + eyeWidth, eyeY),
            strokeWidth = strokeWidth
        )

        // Right Eye
        drawLine(
            color = accentColor,
            start = Offset(cx + width * 0.18f - eyeWidth, eyeY),
            end = Offset(cx + width * 0.18f, eyeY),
            strokeWidth = strokeWidth
        )

        // Explorer Compass Emblem / Nose Point
        drawCircle(
            color = accentColor,
            radius = width * 0.035f,
            center = Offset(cx, cy + height * 0.24f)
        )
    }
}

@Preview(name = "Mascot Light", showBackground = true)
@Composable
private fun RumboMascotPreviewLight() {
    RumboTheme(darkTheme = false) {
        RumboMascot(state = MascotState.DEFAULT)
    }
}

@Preview(name = "Mascot Dark", showBackground = true)
@Composable
private fun RumboMascotPreviewDark() {
    RumboTheme(darkTheme = true) {
        RumboMascot(state = MascotState.FOCUSED)
    }
}
