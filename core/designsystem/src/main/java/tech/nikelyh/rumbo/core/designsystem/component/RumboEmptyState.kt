package tech.nikelyh.rumbo.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme

@Composable
fun RumboEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    mascotState: MascotState = MascotState.RESTING,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
        } else {
            RumboMascot(state = mascotState)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        AnimatedVisibility(visible = actionLabel != null) {
            actionLabel?.let { label ->
                Spacer(modifier = Modifier.height(16.dp))
                RumboButton(onClick = onActionClick) {
                    Text(text = label)
                }
            }
        }
    }
}

@Preview(name = "EmptyState Light", showBackground = true)
@Composable
private fun RumboEmptyStatePreviewLight() {
    RumboTheme(darkTheme = false) {
        RumboEmptyState(
            message = "No hay procesos activos",
            subtitle = "Crea tu primer proceso para comenzar el seguimiento de tus metas.",
            actionLabel = "Crear Proceso"
        )
    }
}
