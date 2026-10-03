package tech.nikelyh.rumbo.feature.home

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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
fun HomeRoute(
    onNavigateToProcess: (String) -> Unit,
    onNavigateToTask: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is HomeUiEvent.OnProcessClick -> onNavigateToProcess(event.processId)
                is HomeUiEvent.OnTaskClick -> onNavigateToTask(event.taskId)
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Inicio",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            HomeUiState.Loading -> RumboLoadingWheel()
            is HomeUiState.Error -> RumboEmptyState(message = uiState.message)
            is HomeUiState.Success -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        RumboCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Progreso General",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { uiState.progress.progressFraction },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${uiState.progress.completedTasksCount} de ${uiState.progress.totalTasksCount} tareas completadas",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Procesos Recientes",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    if (uiState.recentProcesses.isEmpty()) {
                        item {
                            RumboEmptyState(
                                message = "No hay procesos recientes",
                                icon = Icons.Default.ListAlt,
                                modifier = Modifier.height(160.dp)
                            )
                        }
                    } else {
                        items(uiState.recentProcesses, key = { it.id }) { process ->
                            RumboCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onEvent(HomeUiEvent.OnProcessClick(process.id)) }
                            ) {
                                Text(text = process.title, style = MaterialTheme.typography.titleLarge)
                                Text(text = process.description, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tareas Pendientes",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    if (uiState.pendingTasks.isEmpty()) {
                        item {
                            RumboEmptyState(
                                message = "No hay tareas pendientes",
                                icon = Icons.Default.Assignment,
                                modifier = Modifier.height(160.dp)
                            )
                        }
                    } else {
                        items(uiState.pendingTasks, key = { it.id }) { task ->
                            RumboCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onEvent(HomeUiEvent.OnTaskClick(task.id)) }
                            ) {
                                Text(text = task.title, style = MaterialTheme.typography.titleLarge)
                                Text(text = task.description, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
