package tech.nikelyh.rumbo.feature.processes

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
import tech.nikelyh.rumbo.core.model.Process
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateProcessViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val processRepository: ProcessRepository
) : ViewModel() {

    private val initialParentId: String? = savedStateHandle.get<String>("parentProcessId")

    private val _uiState = MutableStateFlow(CreateProcessUiState(parentProcessId = initialParentId))
    val uiState: StateFlow<CreateProcessUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            processRepository.getProcesses().collect { processes ->
                val activeParents = processes.filter { !it.isFinished && it.id != Process.GENERAL_PROCESS_ID }
                _uiState.update { it.copy(availableParents = activeParents) }
            }
        }
    }

    fun onEvent(event: CreateProcessUiEvent) {
        when (event) {
            is CreateProcessUiEvent.NameChanged -> {
                _uiState.update { current ->
                    current.copy(
                        name = event.name,
                        nameError = if (current.nameError != null) validateName(event.name) else null
                    )
                }
            }
            is CreateProcessUiEvent.DescriptionChanged -> {
                _uiState.update { it.copy(description = event.description) }
            }
            is CreateProcessUiEvent.ColorChanged -> {
                _uiState.update { it.copy(colorOrVisualId = event.colorOrVisualId) }
            }
            is CreateProcessUiEvent.ParentProcessSelected -> {
                _uiState.update { it.copy(parentProcessId = event.parentId) }
            }
            is CreateProcessUiEvent.DueDateChanged -> {
                _uiState.update { it.copy(dueDateEpochMillis = event.millis) }
            }
            CreateProcessUiEvent.SubmitProcess -> {
                val current = _uiState.value
                val nameErr = validateName(current.name)

                if (nameErr != null) {
                    _uiState.update { it.copy(nameError = nameErr) }
                    return
                }

                val trimmedName = current.name.trim()
                val trimmedDesc = current.description.trim().ifBlank { null }

                val newProcess = Process(
                    id = UUID.randomUUID().toString(),
                    name = trimmedName,
                    description = trimmedDesc,
                    createdAtEpochMillis = System.currentTimeMillis(),
                    colorOrVisualId = current.colorOrVisualId,
                    accumulatedDirectCost = 0.0,
                    parentProcessId = current.parentProcessId,
                    dueDateEpochMillis = current.dueDateEpochMillis
                )

                viewModelScope.launch {
                    _uiState.update { it.copy(isSubmitting = true) }
                    processRepository.saveProcess(newProcess)
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                }
            }
        }
    }

    private fun validateName(input: String): String? {
        val trimmed = input.trim()
        return when {
            trimmed.isBlank() -> "El nombre del proceso es obligatorio"
            trimmed.length > 50 -> "El nombre no puede superar los 50 caracteres"
            else -> null
        }
    }
}
