package tech.nikelyh.rumbo.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikelyh.rumbo.core.designsystem.component.RumboCard
import tech.nikelyh.rumbo.core.designsystem.component.RumboEmptyState
import tech.nikelyh.rumbo.core.designsystem.component.RumboLoadingWheel
import tech.nikelyh.rumbo.core.model.TaskStatus

@Composable
fun TasksRoute(
    onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TasksScreen(
        uiState = uiState,
        onEvent = { event ->
            if (event is TasksUiEvent.TaskSelected) {
                onTaskClick(event.taskId)
            } else {
                viewModel.onEvent(event)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun TasksScreen(
    uiState: TasksUiState,
    onEvent: (TasksUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Tareas",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            TasksUiState.Loading -> RumboLoadingWheel()
            is TasksUiState.Error -> RumboEmptyState(message = uiState.message)
            is TasksUiState.Success -> {
                if (uiState.tasks.isEmpty()) {
                    RumboEmptyState(
                        message = "No hay tareas disponibles",
                        icon = Icons.Default.Assignment
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.tasks, key = { it.id }) { task ->
                            RumboCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onEvent(TasksUiEvent.TaskSelected(task.id)) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = task.status == TaskStatus.COMPLETED,
                                        onCheckedChange = { onEvent(TasksUiEvent.ToggleTaskStatus(task)) }
                                    )
                                    Column(modifier = Modifier.padding(start = 8.dp)) {
                                        Text(text = task.title, style = MaterialTheme.typography.titleLarge)
                                        task.description?.let {
                                            Text(text = it, style = MaterialTheme.typography.bodyLarge)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
