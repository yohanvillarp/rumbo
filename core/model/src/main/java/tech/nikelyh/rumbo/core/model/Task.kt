package tech.nikelyh.rumbo.core.model

data class Task(
    val id: String,
    val processId: String,
    val title: String,
    val description: String,
    val status: TaskStatus = TaskStatus.PENDING,
    val priority: Priority = Priority.MEDIUM,
    val dueDateEpochMillis: Long? = null
)
