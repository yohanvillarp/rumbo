package tech.nikelyh.rumbo.core.model

data class Task(
    val id: String,
    val processId: String = Process.GENERAL_PROCESS_ID,
    val title: String,
    val description: String? = null,
    val status: TaskStatus = TaskStatus.PENDING,
    val priority: Priority = Priority.MEDIUM,
    val createdAtEpochMillis: Long,
    val dueDateEpochMillis: Long? = null,
    val estimatedDurationMinutes: Int? = null,
    val cost: Double = 0.0,
    val finishedAtEpochMillis: Long? = null
) {
    init {
        require(cost >= 0.0) { "Task cost cannot be negative" }
        require(processId.isNotBlank()) { "Task must belong to a valid processId" }
    }

    val isCompleted: Boolean
        get() = status == TaskStatus.COMPLETED

    fun complete(finishedAt: Long): Task {
        require(finishedAt >= createdAtEpochMillis) { "Finish timestamp cannot be before creation timestamp" }
        return copy(
            status = TaskStatus.COMPLETED,
            finishedAtEpochMillis = finishedAt
        )
    }

    fun updateStatus(newStatus: TaskStatus, finishedAt: Long? = null): Task {
        return if (newStatus == TaskStatus.COMPLETED) {
            complete(finishedAt ?: System.currentTimeMillis())
        } else {
            copy(status = newStatus, finishedAtEpochMillis = null)
        }
    }
}
