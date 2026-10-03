package tech.nikelyh.rumbo.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import tech.nikelyh.rumbo.core.data.repository.ProcessRepository
import tech.nikelyh.rumbo.core.data.repository.TaskRepository
import tech.nikelyh.rumbo.core.model.TaskStatus
import tech.nikelyh.rumbo.core.model.UserProgress
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    processRepository: ProcessRepository,
    taskRepository: TaskRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        processRepository.getProcesses(),
        taskRepository.getAllTasks()
    ) { processes, tasks ->
        val pendingTasks = tasks.filter { it.status == TaskStatus.PENDING || it.status == TaskStatus.IN_PROGRESS }
        val completedTasks = tasks.filter { it.status == TaskStatus.COMPLETED }
        val progress = UserProgress(
            completedProcessesCount = 0,
            totalProcessesCount = processes.size,
            completedTasksCount = completedTasks.size,
            totalTasksCount = tasks.size
        )
        HomeUiState.Success(
            recentProcesses = processes.take(5),
            pendingTasks = pendingTasks.take(5),
            progress = progress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading
    )

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.Refresh -> { /* Refresh trigger if necessary */ }
            is HomeUiEvent.OnProcessClick -> { /* Handle process selection */ }
            is HomeUiEvent.OnTaskClick -> { /* Handle task selection */ }
        }
    }
}
