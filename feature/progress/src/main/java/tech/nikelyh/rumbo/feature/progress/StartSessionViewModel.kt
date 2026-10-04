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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.nikelyh.rumbo.core.data.repository.ProcessRepository
import tech.nikelyh.rumbo.core.data.repository.ProgressRepository
import tech.nikelyh.rumbo.core.data.repository.TaskRepository
import tech.nikelyh.rumbo.core.data.repository.WorkSessionRepository
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
        startTimer()
    }

    fun onEvent(event: StartSessionUiEvent) {
        when (event) {
            is StartSessionUiEvent.ProcessSelected -> {
                _uiState.update { it.copy(selectedProcessId = event.processId) }
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
                lastResumeEpochMillis = System.currentTimeMillis()
                _uiState.update {
                    it.copy(
                        elapsedTimeMillis = manualMillis,
                        showForgotTimerDialog = false,
                        isSessionFinished = true
                    )
                }
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
        }
    }

    private fun startTimer() {
        if (_uiState.value.isTimerRunning) return
        lastResumeEpochMillis = System.currentTimeMillis()
        _uiState.update { it.copy(isTimerRunning = true) }

        timerJob = viewModelScope.launch {
            while (_uiState.value.isTimerRunning) {
                val currentElapsed = accumulatedTimeMillis + (System.currentTimeMillis() - lastResumeEpochMillis)
                _uiState.update { it.copy(elapsedTimeMillis = currentElapsed) }
                delay(1000L)
            }
        }
    }

    private fun pauseTimer() {
        if (_uiState.value.isTimerRunning) {
            accumulatedTimeMillis += (System.currentTimeMillis() - lastResumeEpochMillis)
        }
        _uiState.update { it.copy(isTimerRunning = false, elapsedTimeMillis = accumulatedTimeMillis) }
        timerJob?.cancel()
        timerJob = null
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

            // Accumulate worked time directly to the task
            current.selectedTaskId?.let { taskId ->
                val targetTask = taskRepository.getTaskById(taskId)
                targetTask.collect { task ->
                    if (task != null) {
                        taskRepository.saveTask(task.addWorkedTime(durationMillis))
                    }
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

            _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
        }
    }
}
