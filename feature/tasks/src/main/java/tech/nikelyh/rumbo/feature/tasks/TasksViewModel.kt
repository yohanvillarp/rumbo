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

import tech.nikelyh.rumbo.core.model.Priority
import tech.nikelyh.rumbo.core.model.TaskSortOrder

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val workSessionRepository: WorkSessionRepository,
    processRepository: ProcessRepository
) : ViewModel() {

    private val selectedFilter = MutableStateFlow(TaskFilter.PENDING)
    private val selectedProcessId = MutableStateFlow<String?>(null)
    private val selectedPriority = MutableStateFlow<Priority?>(null)
    private val taskSortOrder = MutableStateFlow(TaskSortOrder.DUE_DATE)
    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<TasksUiState> = combine(
        taskRepository.getAllTasks(),
        processRepository.getProcesses(),
        selectedFilter,
        selectedProcessId,
        selectedPriority,
        taskSortOrder,
        searchQuery
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val tasks = flows[0] as List<Task>
        @Suppress("UNCHECKED_CAST")
        val processes = flows[1] as List<tech.nikelyh.rumbo.core.model.Process>
        val filter = flows[2] as TaskFilter
        val processId = flows[3] as String?
        val priority = flows[4] as Priority?
        val sortOrder = flows[5] as TaskSortOrder
        val query = flows[6] as String

        var filtered = tasks

        // Filter by process
        if (processId != null) {
            filtered = filtered.filter { it.processId == processId }
        }

        // Filter by priority
        if (priority != null) {
            filtered = filtered.filter { it.priority == priority }
        }

        // Filter by tab
        val now = System.currentTimeMillis()
        val today = java.time.LocalDate.now()
        filtered = when (filter) {
            TaskFilter.ALL -> filtered
            TaskFilter.PENDING -> filtered.filter { !it.isCompleted }
            TaskFilter.TODAY -> filtered.filter { task ->
                !task.isCompleted && task.dueDateEpochMillis?.let { millis ->
                    val taskDate = java.time.Instant.ofEpochMilli(millis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    taskDate == today
                } ?: false
            }
            TaskFilter.OVERDUE -> filtered.filter { task ->
                val due = task.dueDateEpochMillis
                !task.isCompleted && due != null && due < now
            }
            TaskFilter.COMPLETED -> filtered.filter { it.isCompleted }
        }

        // Filter by search query
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                    (it.description?.contains(query, ignoreCase = true) == true)
            }
        }

        // Sort tasks
        val sorted = when (sortOrder) {
            TaskSortOrder.DUE_DATE -> filtered.sortedWith(
                compareBy<Task> { it.dueDateEpochMillis == null }
                    .thenBy { it.dueDateEpochMillis ?: Long.MAX_VALUE }
                    .thenByDescending { it.priority.ordinal }
            )
            TaskSortOrder.RECENT -> filtered.sortedByDescending { it.createdAtEpochMillis }
            TaskSortOrder.PRIORITY -> filtered.sortedWith(
                compareByDescending<Task> { it.priority.ordinal }
                    .thenBy { it.dueDateEpochMillis ?: Long.MAX_VALUE }
            )
        }

        if (tasks.isEmpty()) {
            TasksUiState.Empty
        } else {
            TasksUiState.Content(
                tasks = sorted,
                availableProcesses = processes,
                selectedFilter = filter,
                selectedProcessId = processId,
                selectedPriority = priority,
                taskSortOrder = sortOrder,
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
            is TasksUiEvent.PriorityFilterChanged -> selectedPriority.value = event.priority
            is TasksUiEvent.SortOrderChanged -> taskSortOrder.value = event.sortOrder
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
            val targetTotalMillis = durationMinutes * 60 * 1000L
            val additionalMillis = maxOf(0L, targetTotalMillis - task.timeWorkedMillis)
            val now = System.currentTimeMillis()
            if (additionalMillis > 0L) {
                val session = WorkSession(
                    id = UUID.randomUUID().toString(),
                    processId = task.processId,
                    taskId = task.id,
                    startTimeEpochMillis = now - additionalMillis,
                    endTimeEpochMillis = now,
                    durationMillis = additionalMillis,
                    note = "Duración registrada al culminar tarea"
                )
                workSessionRepository.saveWorkSession(session)
            }
            val updatedTask = task.copy(timeWorkedMillis = maxOf(task.timeWorkedMillis, targetTotalMillis))
                .updateStatus(TaskStatus.COMPLETED, finishedAt = now)
            taskRepository.saveTask(updatedTask)
        }
    }
}
