package tech.nikelyh.rumbo.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme

@Composable
fun RumboLinearProgressIndicator(
    progressFraction: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        label = "animatedLinearProgress"
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
fun RumboCircularProgressIndicator(
    progressFraction: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        label = "animatedCircularProgress"
    )

    CircularProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Preview(name = "ProgressIndicator Light", showBackground = true)
@Composable
private fun RumboProgressIndicatorPreviewLight() {
    RumboTheme(darkTheme = false) {
        RumboLinearProgressIndicator(progressFraction = 0.65f)
    }
}
