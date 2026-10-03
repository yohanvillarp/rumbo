package tech.nikelyh.rumbo.feature.processes

data class CreateProcessUiState(
    val name: String = "",
    val description: String = "",
    val colorOrVisualId: String = "teal",
    val costInput: String = "0",
    val nextAction: String = "",
    val nameError: String? = null,
    val costError: String? = null,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false
)
