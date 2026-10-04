package tech.nikelyh.rumbo.feature.processes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.nikelyh.rumbo.core.data.repository.ProcessRepository
import tech.nikelyh.rumbo.core.model.Process
import javax.inject.Inject

@HiltViewModel
class EditProcessViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val processRepository: ProcessRepository
) : ViewModel() {

    val processId: String = savedStateHandle.get<String>("processId") ?: ""

    private val _uiState = MutableStateFlow(EditProcessUiState(processId = processId))
    val uiState: StateFlow<EditProcessUiState> = _uiState.asStateFlow()

    private var originalProcess: Process? = null

    init {
        viewModelScope.launch {
            val process = processRepository.getProcessById(processId).firstOrNull()
            if (process != null) {
                originalProcess = process
                _uiState.update { current ->
                    current.copy(
                        name = process.name,
                        description = process.description ?: "",
                        colorOrVisualId = process.colorOrVisualId,
                        costInput = process.accumulatedDirectCost.toString(),
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onEvent(event: EditProcessUiEvent) {
        when (event) {
            is EditProcessUiEvent.NameChanged -> {
                _uiState.update { current ->
                    current.copy(
                        name = event.name,
                        nameError = if (current.nameError != null) validateName(event.name) else null
                    )
                }
            }
            is EditProcessUiEvent.DescriptionChanged -> {
                _uiState.update { it.copy(description = event.description) }
            }
            is EditProcessUiEvent.ColorChanged -> {
                _uiState.update { it.copy(colorOrVisualId = event.colorOrVisualId) }
            }
            is EditProcessUiEvent.CostChanged -> {
                _uiState.update { current ->
                    current.copy(
                        costInput = event.cost,
                        costError = if (current.costError != null) validateCost(event.cost) else null
                    )
                }
            }
            EditProcessUiEvent.SubmitProcess -> {
                val current = _uiState.value
                val nameErr = validateName(current.name)
                val costErr = validateCost(current.costInput)

                if (nameErr != null || costErr != null) {
                    _uiState.update { it.copy(nameError = nameErr, costError = costErr) }
                    return
                }

                val target = originalProcess ?: return
                if (target.isSystem || target.id == Process.GENERAL_PROCESS_ID) {
                    return
                }
                val trimmedName = current.name.trim()
                val trimmedDesc = current.description.trim().ifBlank { null }
                val costDouble = current.costInput.trim().toDoubleOrNull() ?: 0.0

                val updatedProcess = target.copy(
                    name = trimmedName,
                    description = trimmedDesc,
                    colorOrVisualId = current.colorOrVisualId,
                    accumulatedDirectCost = costDouble
                )

                viewModelScope.launch {
                    _uiState.update { it.copy(isSubmitting = true) }
                    processRepository.saveProcess(updatedProcess)
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
            parsed < 0 -> "El costo no puede ser negativo"
            else -> null
        }
    }
}
