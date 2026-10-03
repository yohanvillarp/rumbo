package tech.nikelyh.rumbo.core.model

data class Milestone(
    val id: String,
    val processId: String,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val targetDateEpochMillis: Long? = null,
    val completedAtEpochMillis: Long? = null
) {
    init {
        require(processId.isNotBlank()) { "Milestone must belong to a valid processId" }
        require(title.isNotBlank()) { "Milestone title cannot be blank" }
    }

    fun complete(completedAt: Long): Milestone {
        return copy(
            isCompleted = true,
            completedAtEpochMillis = completedAt
        )
    }

    fun reopen(): Milestone {
        return copy(
            isCompleted = false,
            completedAtEpochMillis = null
        )
    }
}
