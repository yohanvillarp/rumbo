package tech.nikelyh.rumbo.core.model

data class UserProgress(
    val completedProcessesCount: Int,
    val totalProcessesCount: Int,
    val completedTasksCount: Int,
    val totalTasksCount: Int
) {
    val progressFraction: Float
        get() = if (totalTasksCount > 0) completedTasksCount.toFloat() / totalTasksCount.toFloat() else 0f
}
