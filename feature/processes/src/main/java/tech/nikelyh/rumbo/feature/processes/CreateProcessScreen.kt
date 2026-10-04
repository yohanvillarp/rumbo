package tech.nikelyh.rumbo.feature.processes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikelyh.rumbo.core.designsystem.component.RumboButton
import tech.nikelyh.rumbo.core.designsystem.component.RumboSectionHeader
import tech.nikelyh.rumbo.core.designsystem.theme.ProcessColors
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.Process

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateProcessScreen(
    uiState: CreateProcessUiState,
    onEvent: (CreateProcessUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var parentDropdownExpanded by remember { mutableStateOf(false) }
    val selectedParent = uiState.availableParents.firstOrNull { it.id == uiState.parentProcessId }
    val selectedParentLabel = selectedParent?.name ?: "Ninguno (Proceso Principal)"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Name
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { onEvent(CreateProcessUiEvent.NameChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre del proceso *") },
            placeholder = { Text("Ej. Renovar el hogar o Plan de estudio") },
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
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            )
        )

        // Description
        OutlinedTextField(
            value = uiState.description,
            onValueChange = { onEvent(CreateProcessUiEvent.DescriptionChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descripción (opcional)") },
            placeholder = { Text("¿En qué consiste este proyecto y qué esperas lograr?") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            maxLines = 3,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            )
        )

        // Proceso Padre (opcional para jerarquía de subprocesos)
        ExposedDropdownMenuBox(
            expanded = parentDropdownExpanded,
            onExpandedChange = { parentDropdownExpanded = !parentDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedParentLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("¿Forma parte de otro proceso? (opcional)") },
                leadingIcon = { Icon(Icons.Default.AccountTree, contentDescription = null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentDropdownExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = parentDropdownExpanded,
                onDismissRequest = { parentDropdownExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Ninguno (Es un proceso principal)") },
                    onClick = {
                        onEvent(CreateProcessUiEvent.ParentProcessSelected(null))
                        parentDropdownExpanded = false
                    }
                )
                uiState.availableParents.forEach { parent ->
                    DropdownMenuItem(
                        text = { Text(parent.name) },
                        onClick = {
                            onEvent(CreateProcessUiEvent.ParentProcessSelected(parent.id))
                            parentDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Circular Color Picker (No English text labels!)
        Text(
            text = "Color representativo",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProcessColors.options.forEach { (colorKey, colorValue) ->
                val isSelected = uiState.colorOrVisualId == colorKey
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colorValue)
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onEvent(CreateProcessUiEvent.ColorChanged(colorKey)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Color seleccionado",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

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
