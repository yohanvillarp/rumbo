package tech.nikelyh.rumbo.feature.progress

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
class ProgressViewModel @Inject constructor(
    processRepository: ProcessRepository,
    taskRepository: TaskRepository
) : ViewModel() {

    val uiState: StateFlow<ProgressUiState> = combine(
        processRepository.getProcesses(),
        taskRepository.getAllTasks()
    ) { processes, tasks ->
        val completedTasks = tasks.count { it.status == TaskStatus.COMPLETED }
        val userProgress = UserProgress(
            completedProcessesCount = 0,
            totalProcessesCount = processes.size,
            completedTasksCount = completedTasks,
            totalTasksCount = tasks.size
        )
        ProgressUiState.Success(userProgress = userProgress)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressUiState.Loading
    )

    fun onEvent(event: ProgressUiEvent) {
        when (event) {
            ProgressUiEvent.Refresh -> { /* Refresh trigger */ }
        }
    }
}
