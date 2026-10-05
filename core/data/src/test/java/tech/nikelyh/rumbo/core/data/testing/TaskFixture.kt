package tech.nikelyh.rumbo.core.data.testing

import tech.nikelyh.rumbo.core.model.Priority
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus

/**
 * Enterprise test data fixture builder for [Task] entities.
 */
class TaskFixtureBuilder(
    var id: String = "test-task-1",
    var processId: String = "test-process-1",
    var title: String = "Test Task",
    var description: String? = null,
    var status: TaskStatus = TaskStatus.PENDING,
    var priority: Priority = Priority.MEDIUM,
    var createdAtEpochMillis: Long = 1000L,
    var dueDateEpochMillis: Long? = 5000L,
    var cost: Double = 0.0,
    var estimatedDurationMinutes: Int? = null,
    var timeWorkedMillis: Long = 0L,
    var finishedAtEpochMillis: Long? = null
) {
    fun withId(id: String) = apply { this.id = id }
    fun withProcessId(processId: String) = apply { this.processId = processId }
    fun withTitle(title: String) = apply { this.title = title }
    fun withStatus(status: TaskStatus) = apply { this.status = status }
    fun withPriority(priority: Priority) = apply { this.priority = priority }
    fun withDueDate(dueDate: Long?) = apply { this.dueDateEpochMillis = dueDate }
    fun withCost(cost: Double) = apply { this.cost = cost }
    fun withTimeWorked(millis: Long) = apply { this.timeWorkedMillis = millis }

    fun build(): Task = Task(
        id = id,
        processId = processId,
        title = title,
        description = description,
        status = status,
        priority = priority,
        createdAtEpochMillis = createdAtEpochMillis,
        dueDateEpochMillis = dueDateEpochMillis,
        cost = cost,
        estimatedDurationMinutes = estimatedDurationMinutes,
        timeWorkedMillis = timeWorkedMillis,
        finishedAtEpochMillis = finishedAtEpochMillis
    )
}

fun aTask(block: TaskFixtureBuilder.() -> Unit = {}): Task =
    TaskFixtureBuilder().apply(block).build()
