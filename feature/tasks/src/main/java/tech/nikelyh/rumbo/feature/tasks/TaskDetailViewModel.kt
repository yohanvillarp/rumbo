package tech.nikelyh.rumbo.feature.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.nikelyh.rumbo.core.data.repository.ProcessRepository
import tech.nikelyh.rumbo.core.data.repository.TaskRepository
import tech.nikelyh.rumbo.core.model.TaskStatus
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
    processRepository: ProcessRepository
) : ViewModel() {

    val taskId: String = savedStateHandle.get<String>("taskId") ?: ""

    val uiState: StateFlow<TaskDetailUiState> = combine(
        taskRepository.getTaskById(taskId),
        processRepository.getProcesses()
    ) { task, processes ->
        if (task == null) {
            TaskDetailUiState.Error("Tarea no encontrada")
        } else {
            val process = processes.firstOrNull { it.id == task.processId }
            val processName = process?.name ?: "General"
            TaskDetailUiState.Content(
                task = task,
                processName = processName
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskDetailUiState.Loading
    )

    fun onEvent(event: TaskDetailUiEvent) {
        val currentState = uiState.value as? TaskDetailUiState.Content ?: return
        val currentTask = currentState.task

        when (event) {
            TaskDetailUiEvent.ToggleStatus -> {
                viewModelScope.launch {
                    val newStatus = if (currentTask.isCompleted) TaskStatus.PENDING else TaskStatus.COMPLETED
                    taskRepository.saveTask(currentTask.updateStatus(newStatus))
                }
            }
            TaskDetailUiEvent.DeleteTask -> {
                viewModelScope.launch {
                    taskRepository.deleteTask(currentTask.id)
                }
            }
        }
    }
}
