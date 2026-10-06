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
import tech.nikelyh.rumbo.core.data.repository.WorkSessionRepository
import tech.nikelyh.rumbo.core.model.TaskStatus
import tech.nikelyh.rumbo.core.model.WorkSession
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
    private val workSessionRepository: WorkSessionRepository,
    processRepository: ProcessRepository
) : ViewModel() {

    val taskId: String = savedStateHandle.get<String>("taskId") ?: ""

    val uiState: StateFlow<TaskDetailUiState> = combine(
        taskRepository.getTaskById(taskId),
        processRepository.getProcesses(),
        workSessionRepository.activeSessionState,
        workSessionRepository.getWorkSessionsByTaskId(taskId)
    ) { task, processes, activeSession, sessions ->
        if (task == null) {
            TaskDetailUiState.Error("Tarea no encontrada")
        } else {
            val process = processes.firstOrNull { it.id == task.processId }
            val processName = process?.name ?: "General"
            val isSessionActive = activeSession.hasActiveSession && activeSession.taskId == task.id
            val hasStarted = isSessionActive || task.timeWorkedMillis > 0L || task.status == TaskStatus.IN_PROGRESS || sessions.isNotEmpty()
            val accumulatedMillis = if (isSessionActive) {
                if (activeSession.isRunning) {
                    activeSession.accumulatedTimeMillis + (System.currentTimeMillis() - activeSession.lastResumeEpochMillis).coerceAtLeast(0L)
                } else {
                    activeSession.accumulatedTimeMillis
                }
            } else 0L

            TaskDetailUiState.Content(
                task = task,
                processName = processName,
                sessions = sessions,
                hasStartedSession = hasStarted,
                activeSessionAccumulatedMillis = accumulatedMillis,
                isActiveSessionRunning = isSessionActive && activeSession.isRunning,
                hasActiveSession = isSessionActive
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
                    val finishedAt = if (newStatus == TaskStatus.COMPLETED) System.currentTimeMillis() else null
                    taskRepository.saveTask(currentTask.updateStatus(newStatus, finishedAt = finishedAt))
                }
            }
            is TaskDetailUiEvent.CompleteWithDuration -> {
                viewModelScope.launch {
                    val targetTotalMillis = event.durationMinutes * 60 * 1000L
                    val additionalMillis = maxOf(0L, targetTotalMillis - currentTask.timeWorkedMillis)
                    val now = System.currentTimeMillis()
                    if (additionalMillis > 0L) {
                        val session = WorkSession(
                            id = UUID.randomUUID().toString(),
                            processId = currentTask.processId,
                            taskId = currentTask.id,
                            startTimeEpochMillis = now - additionalMillis,
                            endTimeEpochMillis = now,
                            durationMillis = additionalMillis,
                            note = "Duración registrada al culminar tarea"
                        )
                        workSessionRepository.saveWorkSession(session)
                    }
                    val updatedTask = currentTask.copy(timeWorkedMillis = maxOf(currentTask.timeWorkedMillis, targetTotalMillis))
                        .updateStatus(TaskStatus.COMPLETED, finishedAt = now)
                    taskRepository.saveTask(updatedTask)
                }
            }
            is TaskDetailUiEvent.DeleteSession -> {
                viewModelScope.launch {
                    val sessionToDelete = currentState.sessions.firstOrNull { it.id == event.sessionId }
                    workSessionRepository.deleteWorkSession(event.sessionId)
                    if (sessionToDelete != null) {
                        val newTimeWorked = maxOf(0L, currentTask.timeWorkedMillis - sessionToDelete.durationMillis)
                        taskRepository.saveTask(currentTask.copy(timeWorkedMillis = newTimeWorked))
                    }
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
