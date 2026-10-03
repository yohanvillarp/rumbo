package tech.nikelyh.rumbo.core.model

data class Process(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val createdAtEpochMillis: Long,
    val totalTasksCount: Int = 0,
    val completedTasksCount: Int = 0
)
