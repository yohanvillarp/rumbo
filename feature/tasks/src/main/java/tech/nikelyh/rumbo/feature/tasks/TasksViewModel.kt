package tech.nikelyh.rumbo.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.nikelyh.rumbo.core.data.repository.TaskRepository
import tech.nikelyh.rumbo.core.model.TaskStatus
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val selectedFilter = MutableStateFlow<TaskStatus?>(null)

    val uiState: StateFlow<TasksUiState> = combine(
        taskRepository.getAllTasks(),
        selectedFilter
    ) { tasks, filter ->
        val filtered = if (filter == null) tasks else tasks.filter { it.status == filter }
        TasksUiState.Success(
            tasks = filtered,
            filterStatus = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TasksUiState.Loading
    )

    fun onEvent(event: TasksUiEvent) {
        when (event) {
            is TasksUiEvent.FilterByStatus -> selectedFilter.value = event.status
            is TasksUiEvent.ToggleTaskStatus -> {
                viewModelScope.launch {
                    val nextStatus = if (event.task.status == TaskStatus.COMPLETED) {
                        TaskStatus.PENDING
                    } else {
                        TaskStatus.COMPLETED
                    }
                    taskRepository.saveTask(event.task.copy(status = nextStatus))
                }
            }
            is TasksUiEvent.DeleteTask -> {
                viewModelScope.launch {
                    taskRepository.deleteTask(event.taskId)
                }
            }
            is TasksUiEvent.TaskSelected -> { /* Handle task click */ }
        }
    }
}
