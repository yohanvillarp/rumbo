package tech.nikelyh.rumbo.feature.tasks

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
import tech.nikelyh.rumbo.core.data.repository.TaskRepository
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.Task
import javax.inject.Inject

@HiltViewModel
class EditTaskViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
    private val processRepository: ProcessRepository
) : ViewModel() {

    val taskId: String = savedStateHandle.get<String>("taskId") ?: ""

    private val _uiState = MutableStateFlow(EditTaskUiState(taskId = taskId))
    val uiState: StateFlow<EditTaskUiState> = _uiState.asStateFlow()

    private var originalTask: Task? = null

    init {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId).firstOrNull()
            if (task != null) {
                originalTask = task
                _uiState.update { current ->
                    current.copy(
                        title = task.title,
                        description = task.description ?: "",
                        selectedProcessId = task.processId,
                        priority = task.priority,
                        estimatedDurationMinutesInput = task.estimatedDurationMinutes?.toString() ?: "",
                        costInput = if (task.cost == 0.0) "0" else task.cost.toString(),
                        dueDateEpochMillis = task.dueDateEpochMillis,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        viewModelScope.launch {
            processRepository.getProcesses().collect { processes ->
                val selectable = processes.filter { !it.isFinished }
                val hasGeneral = selectable.any { it.id == Process.GENERAL_PROCESS_ID }
                val withGeneral = if (hasGeneral) selectable else listOf(Process.createGeneralProcess()) + selectable
                val sorted = withGeneral.sortedWith(
                    compareByDescending<Process> { it.isSystem }.thenBy { it.name }
                )
                _uiState.update { it.copy(availableProcesses = sorted) }
            }
        }
    }

    fun onEvent(event: EditTaskUiEvent) {
        when (event) {
            is EditTaskUiEvent.TitleChanged -> {
                _uiState.update { current ->
                    current.copy(
                        title = event.title,
                        titleError = if (current.titleError != null) validateTitle(event.title) else null
                    )
                }
            }
            is EditTaskUiEvent.DescriptionChanged -> {
                _uiState.update { it.copy(description = event.description) }
            }
            is EditTaskUiEvent.ProcessSelected -> {
                _uiState.update { it.copy(selectedProcessId = event.processId, processError = null) }
            }
            is EditTaskUiEvent.PriorityChanged -> {
                _uiState.update { it.copy(priority = event.priority) }
            }
            is EditTaskUiEvent.DurationChanged -> {
                _uiState.update { it.copy(estimatedDurationMinutesInput = event.minutes) }
            }
            is EditTaskUiEvent.CostChanged -> {
                _uiState.update { current ->
                    current.copy(
                        costInput = event.cost,
                        costError = if (current.costError != null) validateCost(event.cost) else null
                    )
                }
            }
            is EditTaskUiEvent.DueDateChanged -> {
                _uiState.update { it.copy(dueDateEpochMillis = event.millis, dueDateError = null) }
            }
            EditTaskUiEvent.SubmitTask -> {
                val current = _uiState.value
                val titleErr = validateTitle(current.title)
                val costErr = validateCost(current.costInput)
                val dueDateErr = if (current.dueDateEpochMillis == null) {
                    "La fecha límite es obligatoria"
                } else null

                val target = originalTask ?: return
                val selectedProc = current.availableProcesses.firstOrNull { it.id == current.selectedProcessId }
                val processErr = if (selectedProc != null && selectedProc.isFinished) {
                    "No se pueden asignar tareas a un proceso culminado"
                } else null

                if (titleErr != null || costErr != null || dueDateErr != null || processErr != null) {
                    _uiState.update {
                        it.copy(
                            titleError = titleErr,
                            costError = costErr,
                            dueDateError = dueDateErr,
                            processError = processErr
                        )
                    }
                    return
                }

                val trimmedTitle = current.title.trim()
                val trimmedDesc = current.description.trim().ifBlank { null }
                val durationInt = current.estimatedDurationMinutesInput.trim().toIntOrNull()
                val costDouble = current.costInput.trim().toDoubleOrNull() ?: 0.0

                val updatedTask = target.copy(
                    processId = current.selectedProcessId.ifBlank { Process.GENERAL_PROCESS_ID },
                    title = trimmedTitle,
                    description = trimmedDesc,
                    priority = current.priority,
                    dueDateEpochMillis = current.dueDateEpochMillis,
                    cost = costDouble,
                    estimatedDurationMinutes = durationInt
                )

                viewModelScope.launch {
                    _uiState.update { it.copy(isSubmitting = true) }
                    taskRepository.saveTask(updatedTask)
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                }
            }
        }
    }

    private fun validateTitle(input: String): String? {
        val trimmed = input.trim()
        return when {
            trimmed.isBlank() -> "El título de la tarea es obligatorio"
            trimmed.length > 100 -> "El título no puede superar los 100 caracteres"
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
