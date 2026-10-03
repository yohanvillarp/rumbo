package tech.nikelyh.rumbo.feature.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.nikelyh.rumbo.core.data.repository.ProcessRepository
import tech.nikelyh.rumbo.core.data.repository.ProgressRepository
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProgressEntry
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LogProgressViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val progressRepository: ProgressRepository,
    processRepository: ProcessRepository
) : ViewModel() {

    private val initialProcessId = savedStateHandle.get<String>("processId") ?: Process.GENERAL_PROCESS_ID

    private val _uiState = MutableStateFlow(LogProgressUiState(selectedProcessId = initialProcessId))
    val uiState: StateFlow<LogProgressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            processRepository.getProcesses().collect { processes ->
                _uiState.update { it.copy(availableProcesses = processes) }
            }
        }
    }

    fun onEvent(event: LogProgressUiEvent) {
        when (event) {
            is LogProgressUiEvent.ProcessSelected -> {
                _uiState.update { it.copy(selectedProcessId = event.processId) }
            }
            is LogProgressUiEvent.LevelSelected -> {
                _uiState.update { it.copy(progressLevel = event.level) }
            }
            is LogProgressUiEvent.NoteChanged -> {
                _uiState.update { it.copy(note = event.note) }
            }
            LogProgressUiEvent.SubmitProgress -> {
                val current = _uiState.value
                val entry = ProgressEntry(
                    id = UUID.randomUUID().toString(),
                    processId = current.selectedProcessId.ifBlank { Process.GENERAL_PROCESS_ID },
                    dateEpochMillis = System.currentTimeMillis(),
                    progressLevel = current.progressLevel.numericValue,
                    note = current.note.trim().ifBlank { null }
                )

                viewModelScope.launch {
                    _uiState.update { it.copy(isSubmitting = true) }
                    progressRepository.saveProgressEntry(entry)
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                }
            }
        }
    }
}
