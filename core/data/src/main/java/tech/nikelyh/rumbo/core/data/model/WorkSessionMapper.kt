package tech.nikelyh.rumbo.core.data.model

import tech.nikelyh.rumbo.core.database.model.WorkSessionEntity
import tech.nikelyh.rumbo.core.model.WorkSession

fun WorkSessionEntity.asExternalModel(): WorkSession = WorkSession(
    id = id,
    processId = processId,
    taskId = taskId,
    startTimeEpochMillis = startTimeEpochMillis,
    endTimeEpochMillis = endTimeEpochMillis,
    durationMillis = durationMillis,
    note = note
)

fun WorkSession.asEntity(): WorkSessionEntity = WorkSessionEntity(
    id = id,
    processId = processId,
    taskId = taskId,
    startTimeEpochMillis = startTimeEpochMillis,
    endTimeEpochMillis = endTimeEpochMillis,
    durationMillis = durationMillis,
    note = note
)
