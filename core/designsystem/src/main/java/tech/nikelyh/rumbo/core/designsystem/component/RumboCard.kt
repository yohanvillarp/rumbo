package tech.nikelyh.rumbo.core.designsystem.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProcessStatus

@Composable
fun RumboCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.animateContentSize(),
            shape = MaterialTheme.shapes.medium,
            colors = cardColors,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    } else {
        Card(
            modifier = modifier.animateContentSize(),
            shape = MaterialTheme.shapes.medium,
            colors = cardColors,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
fun RumboProcessCard(
    process: Process,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RumboCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Text(
            text = process.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        process.description?.let { desc ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Estado: ${process.status.name}  •  Costo: $${process.accumulatedDirectCost}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        process.nextAction?.let { action ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Siguiente: $action",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Preview(name = "ProcessCard Light", showBackground = true)
@Composable
private fun RumboProcessCardPreviewLight() {
    RumboTheme(darkTheme = false) {
        RumboProcessCard(
            process = Process(
                id = "p1",
                name = "Aprender Arquitectura Modular",
                description = "Diseñar e implementar capas claras y desacopladas.",
                status = ProcessStatus.ACTIVE,
                createdAtEpochMillis = 1000L,
                colorOrVisualId = "teal",
                accumulatedDirectCost = 150.0,
                nextAction = "Escribir pruebas unitarias"
            ),
            onClick = {}
        )
    }
}
