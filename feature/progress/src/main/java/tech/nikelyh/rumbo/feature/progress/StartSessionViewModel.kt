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
    processRepository: ProcessRepository,
    taskRepository: TaskRepository
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

    init {
        viewModelScope.launch {
            processRepository.getProcesses().collect { processes ->
                _uiState.update { it.copy(availableProcesses = processes) }
            }
        }
        viewModelScope.launch {
            taskRepository.getAllTasks().collect { tasks ->
                _uiState.update { it.copy(availableTasks = tasks) }
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
                _uiState.update { it.copy(selectedTaskId = event.taskId) }
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
        _uiState.update { it.copy(isTimerRunning = true) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.isTimerRunning) {
                delay(1000L)
                _uiState.update { it.copy(elapsedTimeMillis = it.elapsedTimeMillis + 1000L) }
            }
        }
    }

    private fun pauseTimer() {
        _uiState.update { it.copy(isTimerRunning = false) }
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
