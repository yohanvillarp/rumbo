package tech.nikelyh.rumbo.feature.tasks

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
import tech.nikelyh.rumbo.core.data.repository.TaskRepository
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
    processRepository: ProcessRepository
) : ViewModel() {

    private val initialProcessId = savedStateHandle.get<String>("processId") ?: Process.GENERAL_PROCESS_ID

    private val _uiState = MutableStateFlow(CreateTaskUiState(selectedProcessId = initialProcessId))
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            processRepository.getProcesses().collect { processes ->
                val baseActive = processes.filter { !it.isFinished }
                val hasGeneral = baseActive.any { it.id == Process.GENERAL_PROCESS_ID }
                val withGeneral = if (hasGeneral) baseActive else listOf(Process.createGeneralProcess()) + baseActive
                val sortedProcesses = withGeneral.sortedWith(
                    compareByDescending<Process> { it.isSystem }.thenBy { it.name }
                )
                _uiState.update { current ->
                    val resolvedProcessId = if (current.selectedProcessId.isBlank()) {
                        initialProcessId.ifBlank { Process.GENERAL_PROCESS_ID }
                    } else {
                        current.selectedProcessId
                    }
                    current.copy(
                        availableProcesses = sortedProcesses,
                        selectedProcessId = resolvedProcessId
                    )
                }
            }
        }
    }

    fun onEvent(event: CreateTaskUiEvent) {
        when (event) {
            is CreateTaskUiEvent.TitleChanged -> {
                _uiState.update { current ->
                    current.copy(
                        title = event.title,
                        titleError = if (current.titleError != null) validateTitle(event.title) else null
                    )
                }
            }
            is CreateTaskUiEvent.DescriptionChanged -> {
                _uiState.update { it.copy(description = event.description) }
            }
            is CreateTaskUiEvent.ProcessSelected -> {
                _uiState.update { current ->
                    val selectedProc = current.availableProcesses.firstOrNull { it.id == event.processId }
                    val dueDateError = validateDueDate(current.dueDateEpochMillis, selectedProc)
                    current.copy(
                        selectedProcessId = event.processId,
                        processError = null,
                        dueDateError = dueDateError
                    )
                }
            }
            is CreateTaskUiEvent.PriorityChanged -> {
                _uiState.update { it.copy(priority = event.priority) }
            }
            is CreateTaskUiEvent.DurationChanged -> {
                _uiState.update { it.copy(estimatedDurationMinutesInput = event.minutes) }
            }
            is CreateTaskUiEvent.CostChanged -> {
                _uiState.update { current ->
                    current.copy(
                        costInput = event.cost,
                        costError = if (current.costError != null) validateCost(event.cost) else null
                    )
                }
            }
            is CreateTaskUiEvent.DueDateChanged -> {
                _uiState.update { current ->
                    val selectedProc = current.availableProcesses.firstOrNull { it.id == current.selectedProcessId }
                    val dueDateError = if (event.millis != null) {
                        validateDueDate(event.millis, selectedProc)
                    } else null
                    current.copy(
                        dueDateEpochMillis = event.millis,
                        dueDateError = dueDateError
                    )
                }
            }
            CreateTaskUiEvent.SubmitTask -> {
                val current = _uiState.value
                val titleErr = validateTitle(current.title)
                val costErr = validateCost(current.costInput)
                val selectedProc = current.availableProcesses.firstOrNull { it.id == current.selectedProcessId }
                val dueDateErr = validateDueDate(current.dueDateEpochMillis, selectedProc)

                val processErr = if (selectedProc != null && selectedProc.isFinished) {
                    "No se pueden agregar tareas a un proceso culminado"
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

                val newTask = Task(
                    id = UUID.randomUUID().toString(),
                    processId = current.selectedProcessId.ifBlank { Process.GENERAL_PROCESS_ID },
                    title = trimmedTitle,
                    description = trimmedDesc,
                    status = TaskStatus.PENDING,
                    priority = current.priority,
                    createdAtEpochMillis = System.currentTimeMillis(),
                    dueDateEpochMillis = current.dueDateEpochMillis,
                    cost = costDouble,
                    estimatedDurationMinutes = durationInt
                )

                viewModelScope.launch {
                    _uiState.update { it.copy(isSubmitting = true) }
                    taskRepository.saveTask(newTask)
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

    private fun validateDueDate(taskDueDate: Long?, process: Process?): String? {
        if (taskDueDate == null) {
            return "La fecha límite es obligatoria"
        }
        val procDue = process?.dueDateEpochMillis
        if (procDue != null && taskDueDate > procDue) {
            val instant = java.time.Instant.ofEpochMilli(procDue)
            val zone = java.time.ZoneId.systemDefault()
            val procDateStr = instant.atZone(zone).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy, hh:mm a"))
            return "La fecha y hora límite no puede superar la del proceso ($procDateStr)"
        }
        return null
    }
}
