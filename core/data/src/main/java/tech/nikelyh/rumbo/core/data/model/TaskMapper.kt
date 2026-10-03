package tech.nikelyh.rumbo.core.data.model

import tech.nikelyh.rumbo.core.database.model.TaskEntity
import tech.nikelyh.rumbo.core.model.Priority
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus

fun TaskEntity.asExternalModel(): Task = Task(
    id = id,
    processId = processId,
    title = title,
    description = description,
    status = runCatching { TaskStatus.valueOf(statusName) }.getOrDefault(TaskStatus.PENDING),
    priority = runCatching { Priority.valueOf(priorityName) }.getOrDefault(Priority.MEDIUM),
    createdAtEpochMillis = createdAtEpochMillis,
    dueDateEpochMillis = dueDateEpochMillis,
    estimatedDurationMinutes = estimatedDurationMinutes,
    cost = cost,
    finishedAtEpochMillis = finishedAtEpochMillis
)

fun Task.asEntity(): TaskEntity = TaskEntity(
    id = id,
    processId = processId,
    title = title,
    description = description,
    statusName = status.name,
    priorityName = priority.name,
    createdAtEpochMillis = createdAtEpochMillis,
    dueDateEpochMillis = dueDateEpochMillis,
    estimatedDurationMinutes = estimatedDurationMinutes,
    cost = cost,
    finishedAtEpochMillis = finishedAtEpochMillis
)
