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

    val isAchieved: Boolean
        get() = status == GoalStatus.ACHIEVED

    val isPending: Boolean
        get() = status == GoalStatus.PENDING || status == GoalStatus.IN_PROGRESS

    fun markAchieved(): WeeklyGoal = copy(status = GoalStatus.ACHIEVED)

    fun markCancelled(): WeeklyGoal = copy(status = GoalStatus.CANCELLED)

    fun edit(newDescription: String): WeeklyGoal {
        require(newDescription.isNotBlank()) { "Goal description cannot be blank" }
        return copy(description = newDescription.trim())
    }

    fun carryOverToWeek(newGoalId: String, nextWeekIdentifier: String): WeeklyGoal {
        return WeeklyGoal(
            id = newGoalId,
            processId = processId,
            weekIdentifier = nextWeekIdentifier,
            description = description,
            status = GoalStatus.PENDING
        )
    }

    fun updateStatus(newStatus: GoalStatus): WeeklyGoal = copy(status = newStatus)
}
