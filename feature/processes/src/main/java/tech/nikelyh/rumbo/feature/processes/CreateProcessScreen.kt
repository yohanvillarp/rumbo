package tech.nikelyh.rumbo.feature.processes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikelyh.rumbo.core.designsystem.component.RumboButton
import tech.nikelyh.rumbo.core.designsystem.component.RumboSectionHeader
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme

@Composable
fun CreateProcessRoute(
    onProcessCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateProcessViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onProcessCreated()
        }
    }

    CreateProcessScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
internal fun CreateProcessScreen(
    uiState: CreateProcessUiState,
    onEvent: (CreateProcessUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RumboSectionHeader(
            title = "Nuevo Proceso",
            subtitle = "Define el nombre, costos e hito inicial de tu proceso."
        )

        // Name
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { onEvent(CreateProcessUiEvent.NameChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre del proceso *") },
            placeholder = { Text("Ej. Aprender Jetpack Compose") },
            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
            isError = uiState.nameError != null,
            supportingText = {
                uiState.nameError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            singleLine = true
        )

        // Description
        OutlinedTextField(
            value = uiState.description,
            onValueChange = { onEvent(CreateProcessUiEvent.DescriptionChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descripción (opcional)") },
            placeholder = { Text("Objetivos y contexto general del proceso") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            maxLines = 3
        )

        // Color Identifier
        Text(
            text = "Identificador Visual / Color",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val colorOptions = listOf("teal", "blue", "purple", "amber", "emerald")
            colorOptions.forEach { color ->
                FilterChip(
                    selected = uiState.colorOrVisualId == color,
                    onClick = { onEvent(CreateProcessUiEvent.ColorChanged(color)) },
                    label = { Text(color.replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        // Initial Cost
        OutlinedTextField(
            value = uiState.costInput,
            onValueChange = { onEvent(CreateProcessUiEvent.CostChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Costo Inicial Directo ($)") },
            placeholder = { Text("0.0") },
            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
            isError = uiState.costError != null,
            supportingText = {
                uiState.costError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            singleLine = true
        )

        // Next Action
        OutlinedTextField(
            value = uiState.nextAction,
            onValueChange = { onEvent(CreateProcessUiEvent.NextActionChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Siguiente Acción (opcional)") },
            placeholder = { Text("Ej. Comprar libro de referencia") },
            leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        RumboButton(
            onClick = { onEvent(CreateProcessUiEvent.SubmitProcess) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSubmitting
        ) {
            Text("Guardar Proceso")
        }
    }
}

@Preview(name = "Create Process Light", showBackground = true)
@Composable
private fun CreateProcessScreenPreviewLight() {
    RumboTheme(darkTheme = false) {
        CreateProcessScreen(
            uiState = CreateProcessUiState(),
            onEvent = {}
        )
    }
}
