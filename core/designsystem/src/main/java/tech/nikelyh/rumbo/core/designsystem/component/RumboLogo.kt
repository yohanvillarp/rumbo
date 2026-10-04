package tech.nikelyh.rumbo.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme

/**
 * Official Brand Logo for Rumbo.
 * Combines the animated compass star emblem with crafted brand typography.
 */
@Composable
fun RumboLogo(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    state: MascotState = MascotState.FOCUSED,
    showSubtitle: Boolean = true,
    horizontal: Boolean = false
) {
    if (horizontal) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RumboMascot(
                state = state,
                size = size
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Rumbo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                if (showSubtitle) {
                    Text(
                        text = "Dirección y Enfoque",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            RumboMascot(
                state = state,
                size = size
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Rumbo",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp
            )
            if (showSubtitle) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Orientación Serena para tus Metas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Preview(name = "Rumbo Logo Vertical Light", showBackground = true)
@Composable
private fun RumboLogoVerticalPreview() {
    RumboTheme(darkTheme = false) {
        RumboLogo()
    }
}

@Preview(name = "Rumbo Logo Horizontal Dark", showBackground = true)
@Composable
private fun RumboLogoHorizontalPreview() {
    RumboTheme(darkTheme = true) {
        RumboLogo(horizontal = true)
    }
}
