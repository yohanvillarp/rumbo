package tech.nikelyh.rumbo.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RumboTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    navigationIconContentDescription: String? = null,
    onNavigationClick: () -> Unit = {},
    actionIcon: ImageVector? = null,
    actionIconContentDescription: String? = null,
    onActionClick: () -> Unit = {}
) {
    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    val onContainerColor = MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor = MaterialTheme.colorScheme.primary

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val w = size.width
                val h = size.height

                // Draw base background color that responds to theme
                drawRect(color = containerColor)

                // Traced artistic drawings (dibujos trazados - navigational compass and geometric lines)
                val strokeColor = accentColor.copy(alpha = 0.18f)
                val lightLineColor = onContainerColor.copy(alpha = 0.12f)

                // 1. Concentric navigational arcs originating from right quadrant
                val center = Offset(w * 0.90f, h * 0.35f)
                drawCircle(
                    color = strokeColor,
                    radius = h * 0.60f,
                    center = center,
                    style = Stroke(width = 1.2.dp.toPx())
                )
                drawCircle(
                    color = lightLineColor,
                    radius = h * 1.10f,
                    center = center,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                )
                drawCircle(
                    color = strokeColor,
                    radius = h * 1.60f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // 2. Radiating directional bearing lines
                val angles = listOf(145.0, 170.0, 195.0, 220.0)
                angles.forEach { deg ->
                    val rad = Math.toRadians(deg)
                    val startDist = h * 0.30f
                    val endDist = h * 1.70f
                    val p1 = Offset(
                        (center.x + cos(rad) * startDist).toFloat(),
                        (center.y + sin(rad) * startDist).toFloat()
                    )
                    val p2 = Offset(
                        (center.x + cos(rad) * endDist).toFloat(),
                        (center.y + sin(rad) * endDist).toFloat()
                    )
                    drawLine(
                        color = lightLineColor,
                        start = p1,
                        end = p2,
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 3. Subtle course nodes & dashed connecting line
                val node1 = Offset(w * 0.65f, h * 0.65f)
                val node2 = Offset(w * 0.50f, h * 0.38f)
                val node3 = Offset(w * 0.38f, h * 0.70f)

                drawCircle(color = accentColor.copy(alpha = 0.30f), radius = 2.5.dp.toPx(), center = node1)
                drawCircle(color = accentColor.copy(alpha = 0.25f), radius = 2.dp.toPx(), center = node2)
                drawCircle(color = accentColor.copy(alpha = 0.22f), radius = 2.dp.toPx(), center = node3)

                val coursePath = Path().apply {
                    moveTo(node3.x, node3.y)
                    lineTo(node2.x, node2.y)
                    lineTo(node1.x, node1.y)
                }
                drawPath(
                    path = coursePath,
                    color = lightLineColor,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)
                    )
                )

                // 4. Clean bottom divider line in a gentle tone
                drawLine(
                    color = onContainerColor.copy(alpha = 0.08f),
                    start = Offset(0f, h),
                    end = Offset(w, h),
                    strokeWidth = 1.dp.toPx()
                )
            },
        navigationIcon = {
            if (navigationIcon != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = navigationIconContentDescription,
                        tint = onContainerColor
                    )
                }
            }
        },
        actions = {
            if (actionIcon != null) {
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = actionIconContentDescription,
                        tint = onContainerColor
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = onContainerColor,
            navigationIconContentColor = onContainerColor,
            actionIconContentColor = onContainerColor
        )
    )
}

@Preview(name = "TopBar Light", showBackground = true)
@Composable
private fun RumboTopBarPreviewLight() {
    RumboTheme(darkTheme = false) {
        RumboTopBar(
            title = "Procesos",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            actionIcon = Icons.Default.Settings
        )
    }
}

@Preview(name = "TopBar Dark", showBackground = true)
@Composable
private fun RumboTopBarPreviewDark() {
    RumboTheme(darkTheme = true) {
        RumboTopBar(
            title = "Procesos",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            actionIcon = Icons.Default.Settings
        )
    }
}
