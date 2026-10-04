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
            is CreateProcessUiEvent.CostChanged -> {
                _uiState.update { current ->
                    current.copy(
                        costInput = event.cost,
                        costError = if (current.costError != null) validateCost(event.cost) else null
                    )
                }
            }
            is CreateProcessUiEvent.NextActionChanged -> {
                _uiState.update { it.copy(nextAction = event.nextAction) }
            }
            is CreateProcessUiEvent.ParentProcessSelected -> {
                _uiState.update { it.copy(parentProcessId = event.parentId) }
            }
            CreateProcessUiEvent.SubmitProcess -> {
                val current = _uiState.value
                val nameErr = validateName(current.name)
                val costErr = validateCost(current.costInput)

                if (nameErr != null || costErr != null) {
                    _uiState.update { it.copy(nameError = nameErr, costError = costErr) }
                    return
                }

                val trimmedName = current.name.trim()
                val trimmedDesc = current.description.trim().ifBlank { null }
                val trimmedNextAction = current.nextAction.trim().ifBlank { null }
                val costDouble = current.costInput.trim().toDoubleOrNull() ?: 0.0

                val newProcess = Process(
                    id = UUID.randomUUID().toString(),
                    name = trimmedName,
                    description = trimmedDesc,
                    createdAtEpochMillis = System.currentTimeMillis(),
                    colorOrVisualId = current.colorOrVisualId,
                    accumulatedDirectCost = costDouble,
                    nextAction = trimmedNextAction,
                    parentProcessId = current.parentProcessId
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

    private fun validateCost(input: String): String? {
        if (input.isBlank()) return null
        val parsed = input.trim().toDoubleOrNull()
        return when {
            parsed == null -> "El costo debe ser un valor numérico"
            parsed < 0 -> "El costo inicial no puede ser negativo"
            else -> null
        }
    }
}
