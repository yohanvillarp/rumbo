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
import tech.nikelyh.rumbo.core.data.repository.ProcessRepository
import tech.nikelyh.rumbo.core.data.repository.TaskRepository
import tech.nikelyh.rumbo.core.data.repository.WorkSessionRepository
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus
import tech.nikelyh.rumbo.core.model.WorkSession
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val workSessionRepository: WorkSessionRepository,
    processRepository: ProcessRepository
) : ViewModel() {

    private val selectedFilter = MutableStateFlow(TaskFilter.PENDING)
    private val selectedProcessId = MutableStateFlow<String?>(null)
    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<TasksUiState> = combine(
        taskRepository.getAllTasks(),
        processRepository.getProcesses(),
        selectedFilter,
        selectedProcessId,
        searchQuery
    ) { tasks, processes, filter, processId, query ->
        var filtered = tasks

        // Filter by process
        if (processId != null) {
            filtered = filtered.filter { it.processId == processId }
        }

        // Filter by tab
        filtered = when (filter) {
            TaskFilter.PENDING -> filtered.filter { !it.isCompleted }
            TaskFilter.TODAY -> filtered.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> filtered.filter { it.isCompleted }
        }

        // Filter by search query
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                    (it.description?.contains(query, ignoreCase = true) == true)
            }
        }

        if (tasks.isEmpty()) {
            TasksUiState.Empty
        } else {
            TasksUiState.Content(
                tasks = filtered,
                availableProcesses = processes,
                selectedFilter = filter,
                selectedProcessId = processId,
                searchQuery = query
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TasksUiState.Loading
    )

    fun onEvent(event: TasksUiEvent) {
        when (event) {
            is TasksUiEvent.FilterChanged -> selectedFilter.value = event.filter
            is TasksUiEvent.ProcessFilterChanged -> selectedProcessId.value = event.processId
            is TasksUiEvent.SearchQueryChanged -> searchQuery.value = event.query
            is TasksUiEvent.ToggleTaskStatus -> toggleTaskStatus(event.task)
            is TasksUiEvent.CompleteTaskWithDuration -> completeTaskWithDuration(event.task, event.durationMinutes)
            else -> { /* Navigation handled at Screen route level */ }
        }
    }

    private fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            val newStatus = if (task.isCompleted) TaskStatus.PENDING else TaskStatus.COMPLETED
            val updatedTask = task.updateStatus(newStatus)
            taskRepository.saveTask(updatedTask)
        }
    }

    private fun completeTaskWithDuration(task: Task, durationMinutes: Long) {
        viewModelScope.launch {
            val durationMillis = durationMinutes * 60 * 1000L
            val now = System.currentTimeMillis()
            val session = WorkSession(
                id = UUID.randomUUID().toString(),
                processId = task.processId,
                taskId = task.id,
                startTimeEpochMillis = now - durationMillis,
                endTimeEpochMillis = now,
                durationMillis = durationMillis,
                note = "Duración registrada al culminar tarea"
            )
            workSessionRepository.saveWorkSession(session)
            val updatedTask = task.addWorkedTime(durationMillis).updateStatus(TaskStatus.COMPLETED, finishedAt = now)
            taskRepository.saveTask(updatedTask)
        }
    }
}
