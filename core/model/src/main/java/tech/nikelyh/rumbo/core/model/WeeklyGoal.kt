package tech.nikelyh.rumbo.core.model

data class WeeklyGoal(
    val id: String,
    val processId: String,
    val weekIdentifier: String,
    val description: String,
    val status: GoalStatus = GoalStatus.PENDING
) {
    init {
        require(processId.isNotBlank()) { "WeeklyGoal must belong to a valid processId" }
        require(description.isNotBlank()) { "WeeklyGoal description cannot be blank" }
    }

    fun markAchieved(): WeeklyGoal = copy(status = GoalStatus.ACHIEVED)

    fun markCancelled(): WeeklyGoal = copy(status = GoalStatus.CANCELLED)

    fun updateStatus(newStatus: GoalStatus): WeeklyGoal = copy(status = newStatus)
}
