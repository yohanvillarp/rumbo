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
            processRepository.getProcesses().collect { processes ->
                _uiState.update { it.copy(availableProcesses = processes) }
            }
        }
        viewModelScope.launch {
            taskRepository.getAllTasks().collect { tasks ->
                _uiState.update { current ->
                    val selectedTask = tasks.firstOrNull { it.id == current.selectedTaskId }
                    current.copy(
                        availableTasks = tasks,
                        selectedTaskTitle = selectedTask?.title ?: ""
                    )
                }
            }
        }
        initializeSession()
    }

    private fun initializeSession() {
        viewModelScope.launch {
            val activeState = workSessionRepository.activeSessionState.firstOrNull() ?: ActiveSessionState()
            val now = System.currentTimeMillis()

            val isRestoring = activeState.hasActiveSession && (initialTaskId == null || initialTaskId == activeState.taskId)

            if (isRestoring) {
                val restoredProcessId = activeState.processId ?: initialProcessId
                val restoredTaskId = activeState.taskId ?: initialTaskId
                val restoredElapsed = if (activeState.isRunning) {
                    activeState.accumulatedTimeMillis + (now - activeState.lastResumeEpochMillis).coerceAtLeast(0L)
                } else {
                    activeState.accumulatedTimeMillis
                }
                sessionStartTimeEpochMillis = activeState.startTimeEpochMillis
                lastResumeEpochMillis = now
                accumulatedTimeMillis = if (activeState.isRunning) restoredElapsed else activeState.accumulatedTimeMillis

                _uiState.update { current ->
                    current.copy(
                        selectedProcessId = restoredProcessId,
                        selectedTaskId = restoredTaskId,
                        elapsedTimeMillis = restoredElapsed,
                        isTimerRunning = activeState.isRunning
                    )
                }
                saveActiveState(isRunning = activeState.isRunning)
                if (activeState.isRunning) {
                    resumeTimerLoop()
                }
            } else {
                sessionStartTimeEpochMillis = now
                lastResumeEpochMillis = now
                accumulatedTimeMillis = 0L
                _uiState.update { current ->
                    current.copy(
                        selectedProcessId = initialProcessId,
                        selectedTaskId = initialTaskId,
                        elapsedTimeMillis = 0L,
                        isTimerRunning = true
                    )
                }
                saveActiveState(isRunning = true)
                resumeTimerLoop()
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
                        isTimerRunning = false
                    )
                }
                saveActiveState(isRunning = false)
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
                delay(1000L)
            }
        }
    }

    private fun startTimer() {
        if (_uiState.value.isTimerRunning) return
        lastResumeEpochMillis = System.currentTimeMillis()
        _uiState.update { it.copy(isTimerRunning = true) }
        saveActiveState(isRunning = true)
        resumeTimerLoop()
    }

    private fun pauseTimer() {
        if (_uiState.value.isTimerRunning) {
            accumulatedTimeMillis += (System.currentTimeMillis() - lastResumeEpochMillis).coerceAtLeast(0L)
        }
        _uiState.update { it.copy(isTimerRunning = false, elapsedTimeMillis = accumulatedTimeMillis) }
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

        val session = WorkSession(
            id = UUID.randomUUID().toString(),
            processId = current.selectedProcessId.ifBlank { Process.GENERAL_PROCESS_ID },
            taskId = current.selectedTaskId,
            startTimeEpochMillis = sessionStartTimeEpochMillis,
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
                    taskRepository.saveTask(task.addWorkedTime(durationMillis))
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
