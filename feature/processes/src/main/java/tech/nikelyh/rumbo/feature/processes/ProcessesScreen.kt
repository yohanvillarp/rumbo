package tech.nikelyh.rumbo.feature.processes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikelyh.rumbo.core.designsystem.component.RumboCard
import tech.nikelyh.rumbo.core.designsystem.component.RumboEmptyState
import tech.nikelyh.rumbo.core.designsystem.component.RumboLoadingWheel

@Composable
fun ProcessesRoute(
    onProcessClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProcessesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProcessesScreen(
        uiState = uiState,
        onEvent = { event ->
            if (event is ProcessesUiEvent.ProcessSelected) {
                onProcessClick(event.processId)
            } else {
                viewModel.onEvent(event)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun ProcessesScreen(
    uiState: ProcessesUiState,
    onEvent: (ProcessesUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Procesos",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            ProcessesUiState.Loading -> RumboLoadingWheel()
            is ProcessesUiState.Error -> RumboEmptyState(message = uiState.message)
            is ProcessesUiState.Success -> {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { onEvent(ProcessesUiEvent.SearchQueryChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar procesos...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.processes.isEmpty()) {
                    RumboEmptyState(
                        message = "No hay procesos creados",
                        icon = Icons.Default.ListAlt
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.processes, key = { it.id }) { process ->
                            RumboCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onEvent(ProcessesUiEvent.ProcessSelected(process.id)) }
                            ) {
                                Text(text = process.title, style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = process.description, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
