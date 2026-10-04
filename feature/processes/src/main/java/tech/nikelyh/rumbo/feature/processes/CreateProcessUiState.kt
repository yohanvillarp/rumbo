package tech.nikelyh.rumbo.feature.processes

import tech.nikelyh.rumbo.core.model.Process

data class CreateProcessUiState(
    val name: String = "",
    val description: String = "",
    val colorOrVisualId: String = "teal",
    val costInput: String = "0",
    val parentProcessId: String? = null,
    val availableParents: List<Process> = emptyList(),
    val nameError: String? = null,
    val costError: String? = null,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false
)
