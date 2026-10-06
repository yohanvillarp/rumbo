package tech.nikelyh.rumbo.feature.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.nikelyh.rumbo.core.data.repository.ProcessRepository
import tech.nikelyh.rumbo.core.data.repository.ProgressRepository
import tech.nikelyh.rumbo.core.data.repository.TaskRepository
import tech.nikelyh.rumbo.core.data.repository.WorkSessionRepository
import tech.nikelyh.rumbo.core.model.ActiveSessionState
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProgressEntry
import tech.nikelyh.rumbo.core.model.WorkSession
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class StartSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workSessionRepository: WorkSessionRepository,
    private val progressRepository: ProgressRepository,
    private val taskRepository: TaskRepository,
    processRepository: ProcessRepository
) : ViewModel() {

    private val initialProcessId = savedStateHandle.get<String>("processId") ?: Process.GENERAL_PROCESS_ID
    private val initialTaskId = savedStateHandle.get<String>("taskId")

    private val _uiState = MutableStateFlow(
        StartSessionUiState(
            selectedProcessId = initialProcessId,
            selectedTaskId = initialTaskId
        )
    )
    val uiState: StateFlow<StartSessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartTimeEpochMillis: Long = System.currentTimeMillis()
    private var lastResumeEpochMillis: Long = System.currentTimeMillis()
    private var accumulatedTimeMillis: Long = 0L

    init {
        viewModelScope.launch {
            val activeState = workSessionRepository.activeSessionState.firstOrNull() ?: ActiveSessionState()
            val processes = processRepository.getProcesses().firstOrNull() ?: emptyList()
            val tasks = taskRepository.getAllTasks().firstOrNull() ?: emptyList()
            val now = System.currentTimeMillis()

            val isRestoring = activeState.hasActiveSession && (initialTaskId == null || initialTaskId == activeState.taskId)

            val resolvedProcessId = if (isRestoring) (activeState.processId ?: initialProcessId) else initialProcessId
            val resolvedTaskId = if (isRestoring) (activeState.taskId ?: initialTaskId) else initialTaskId
            val resolvedTask = tasks.firstOrNull { it.id == resolvedTaskId }

            val resolvedElapsed = if (isRestoring) {
                if (activeState.isRunning) {
                    activeState.accumulatedTimeMillis + (now - activeState.lastResumeEpochMillis).coerceAtLeast(0L)
                } else {
                    activeState.accumulatedTimeMillis
                }
            } else 0L

            val isRunning = if (isRestoring) activeState.isRunning else false

            sessionStartTimeEpochMillis = if (isRestoring) activeState.startTimeEpochMillis else 0L
            lastResumeEpochMillis = now
            accumulatedTimeMillis = if (isRestoring) {
                if (activeState.isRunning) resolvedElapsed else activeState.accumulatedTimeMillis
            } else 0L

            _uiState.update {
                it.copy(
                    selectedProcessId = resolvedProcessId,
                    selectedTaskId = resolvedTaskId,
                    selectedTaskTitle = resolvedTask?.title ?: "",
                    availableProcesses = processes,
                    availableTasks = tasks,
                    elapsedTimeMillis = resolvedElapsed,
                    isTimerRunning = isRunning,
                    isLoading = false
                )
            }

            if (isRestoring) {
                saveActiveState(isRunning = isRunning)
            }

            if (isRunning) {
                resumeTimerLoop()
            }

            launch {
                processRepository.getProcesses().collect { procList ->
                    _uiState.update { it.copy(availableProcesses = procList) }
                }
            }
            launch {
                taskRepository.getAllTasks().collect { taskList ->
                    _uiState.update { current ->
                        val task = taskList.firstOrNull { it.id == current.selectedTaskId }
                        current.copy(
                            availableTasks = taskList,
                            selectedTaskTitle = task?.title ?: current.selectedTaskTitle
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: StartSessionUiEvent) {
        when (event) {
            is StartSessionUiEvent.ProcessSelected -> {
                _uiState.update { it.copy(selectedProcessId = event.processId) }
                saveActiveState(isRunning = _uiState.value.isTimerRunning)
            }
            is StartSessionUiEvent.TaskSelected -> {
                _uiState.update { current ->
                    val task = current.availableTasks.firstOrNull { t -> t.id == event.taskId }
                    current.copy(
                        selectedTaskId = event.taskId,
                        selectedTaskTitle = task?.title ?: "",
                        selectedProcessId = task?.processId ?: current.selectedProcessId
                    )
                }
                saveActiveState(isRunning = _uiState.value.isTimerRunning)
            }
            StartSessionUiEvent.ToggleTimer -> {
                if (_uiState.value.isTimerRunning) {
                    pauseTimer()
                } else {
                    startTimer()
                }
            }
            StartSessionUiEvent.ResetPausedState -> {
                _uiState.update { it.copy(isSessionPaused = false) }
            }
            StartSessionUiEvent.FinishTimer -> {
                pauseTimer()
                _uiState.update { it.copy(isSessionFinished = true) }
            }
            StartSessionUiEvent.ForgotTimerClicked -> {
                pauseTimer()
                _uiState.update { it.copy(showForgotTimerDialog = true) }
            }
            StartSessionUiEvent.DismissForgotTimerDialog -> {
                _uiState.update { it.copy(showForgotTimerDialog = false) }
            }
            is StartSessionUiEvent.ConfirmManualMinutes -> {
                val parsedMinutes = event.minutesInput.trim().toLongOrNull() ?: 0L
                val manualMillis = parsedMinutes * 60 * 1000L
                accumulatedTimeMillis = manualMillis
                _uiState.update {
                    it.copy(
                        elapsedTimeMillis = manualMillis,
                        showForgotTimerDialog = false,
                        isSessionFinished = true,
                        isTimerRunning = false,
                        markTaskAsCompleted = it.selectedTaskId != null
                    )
                }
                saveActiveState(isRunning = false)
            }
            is StartSessionUiEvent.AdjustMinutesAndResume -> {
                val parsedMinutes = event.minutesInput.trim().toLongOrNull() ?: 0L
                val manualMillis = parsedMinutes * 60 * 1000L
                accumulatedTimeMillis = manualMillis
                lastResumeEpochMillis = System.currentTimeMillis()
                _uiState.update {
                    it.copy(
                        elapsedTimeMillis = manualMillis,
                        showForgotTimerDialog = false,
                        isSessionFinished = false,
                        isTimerRunning = true
                    )
                }
                saveActiveState(isRunning = true)
                resumeTimerLoop()
            }
            is StartSessionUiEvent.ToggleCompleteTask -> {
                _uiState.update { it.copy(markTaskAsCompleted = event.complete) }
            }
            is StartSessionUiEvent.NoteChanged -> {
                _uiState.update { it.copy(sessionNote = event.note) }
            }
            is StartSessionUiEvent.ToggleSaveProgress -> {
                _uiState.update { it.copy(saveProgressEntry = event.save) }
            }
            is StartSessionUiEvent.ProgressLevelSelected -> {
                _uiState.update { it.copy(progressLevel = event.level) }
            }
            StartSessionUiEvent.SubmitSession -> {
                submitSession()
            }
            StartSessionUiEvent.CancelSession -> {
                cancelSession()
            }
        }
    }

    private fun cancelSession() {
        timerJob?.cancel()
        timerJob = null
        viewModelScope.launch {
            workSessionRepository.clearActiveSessionState()
            _uiState.update {
                it.copy(
                    isTimerRunning = false,
                    isSessionFinished = true,
                    isSuccess = true
                )
            }
        }
    }

    private fun resumeTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isTimerRunning) {
                val currentElapsed = accumulatedTimeMillis + (System.currentTimeMillis() - lastResumeEpochMillis)
                _uiState.update { it.copy(elapsedTimeMillis = currentElapsed) }
                delay(250L)
            }
        }
    }

    private fun startTimer() {
        if (_uiState.value.isTimerRunning) return
        val now = System.currentTimeMillis()
        if (sessionStartTimeEpochMillis == 0L) {
            sessionStartTimeEpochMillis = now
        }
        lastResumeEpochMillis = now
        _uiState.update { it.copy(isTimerRunning = true) }
        saveActiveState(isRunning = true)
        resumeTimerLoop()
    }

    private fun pauseTimer() {
        if (_uiState.value.isTimerRunning) {
            accumulatedTimeMillis += (System.currentTimeMillis() - lastResumeEpochMillis).coerceAtLeast(0L)
        }
        _uiState.update {
            it.copy(
                isTimerRunning = false,
                elapsedTimeMillis = accumulatedTimeMillis,
                isSessionPaused = false
            )
        }
        timerJob?.cancel()
        timerJob = null
        saveActiveState(isRunning = false)
    }

    private fun saveActiveState(isRunning: Boolean) {
        viewModelScope.launch {
            val current = _uiState.value
            workSessionRepository.saveActiveSessionState(
                ActiveSessionState(
                    processId = current.selectedProcessId,
                    taskId = current.selectedTaskId,
                    startTimeEpochMillis = sessionStartTimeEpochMillis,
                    lastResumeEpochMillis = lastResumeEpochMillis,
                    accumulatedTimeMillis = accumulatedTimeMillis,
                    isRunning = isRunning
                )
            )
        }
    }

    private fun submitSession() {
        val current = _uiState.value
        val endTimeMillis = System.currentTimeMillis()
        val durationMillis = current.elapsedTimeMillis
        val effectiveStartMillis = if (sessionStartTimeEpochMillis > 0L) sessionStartTimeEpochMillis else (endTimeMillis - durationMillis)

        val session = WorkSession(
            id = UUID.randomUUID().toString(),
            processId = current.selectedProcessId.ifBlank { Process.GENERAL_PROCESS_ID },
            taskId = current.selectedTaskId,
            startTimeEpochMillis = effectiveStartMillis,
            endTimeEpochMillis = endTimeMillis,
            durationMillis = durationMillis,
            note = current.sessionNote.trim().ifBlank { null }
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            workSessionRepository.saveWorkSession(session)

            // Accumulate worked time directly to the task using firstOrNull to avoid loop
            current.selectedTaskId?.let { taskId ->
                val task = taskRepository.getTaskById(taskId).firstOrNull()
                if (task != null) {
                    val updatedTask = if (current.markTaskAsCompleted) {
                        task.addWorkedTime(durationMillis).complete(endTimeMillis)
                    } else {
                        task.addWorkedTime(durationMillis)
                    }
                    taskRepository.saveTask(updatedTask)
                }
            }

            if (current.saveProgressEntry) {
                val progressEntry = ProgressEntry(
                    id = UUID.randomUUID().toString(),
                    processId = current.selectedProcessId.ifBlank { Process.GENERAL_PROCESS_ID },
                    dateEpochMillis = endTimeMillis,
                    progressLevel = current.progressLevel.numericValue,
                    note = current.sessionNote.trim().ifBlank { null }
                )
                progressRepository.saveProgressEntry(progressEntry)
            }

            workSessionRepository.clearActiveSessionState()
            _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        timerJob = null
    }
}
