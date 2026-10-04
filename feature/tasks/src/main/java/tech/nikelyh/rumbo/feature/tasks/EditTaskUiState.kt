package tech.nikelyh.rumbo.feature.tasks

import tech.nikelyh.rumbo.core.model.Priority
import tech.nikelyh.rumbo.core.model.Process

data class EditTaskUiState(
    val taskId: String = "",
    val title: String = "",
    val description: String = "",
    val selectedProcessId: String = "general",
    val availableProcesses: List<Process> = emptyList(),
    val priority: Priority = Priority.MEDIUM,
    val estimatedDurationMinutesInput: String = "",
    val costInput: String = "0",
    val dueDateEpochMillis: Long? = null,
    val titleError: String? = null,
    val costError: String? = null,
    val dueDateError: String? = null,
    val processError: String? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false
)
