package tech.nikelyh.rumbo.feature.processes

import tech.nikelyh.rumbo.core.model.Process

data class CreateProcessUiState(
    val name: String = "",
    val description: String = "",
    val colorOrVisualId: String = "teal",
    val parentProcessId: String? = null,
    val availableParents: List<Process> = emptyList(),
    val nameError: String? = null,
    val dueDateEpochMillis: Long? = null,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false
)
