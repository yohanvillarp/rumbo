package tech.nikelyh.rumbo.feature.processes

data class EditProcessUiState(
    val processId: String = "",
    val name: String = "",
    val description: String = "",
    val colorOrVisualId: String = "teal",
    val nameError: String? = null,
    val dueDateEpochMillis: Long? = null,
    val dueDateError: String? = null,
    val maxTaskDueDateEpochMillis: Long? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false
)
