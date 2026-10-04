package tech.nikelyh.rumbo.core.data.model

import tech.nikelyh.rumbo.core.database.model.ProcessEntity
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProcessStatus

fun ProcessEntity.asExternalModel(): Process = Process(
    id = id,
    name = name,
    description = description,
    status = runCatching { ProcessStatus.valueOf(statusName) }.getOrDefault(ProcessStatus.ACTIVE),
    createdAtEpochMillis = createdAtEpochMillis,
    finishedAtEpochMillis = finishedAtEpochMillis,
    colorOrVisualId = colorOrVisualId,
    accumulatedDirectCost = accumulatedDirectCost,
    parentProcessId = parentProcessId
)

fun Process.asEntity(): ProcessEntity = ProcessEntity(
    id = id,
    name = name,
    description = description,
    statusName = status.name,
    createdAtEpochMillis = createdAtEpochMillis,
    finishedAtEpochMillis = finishedAtEpochMillis,
    colorOrVisualId = colorOrVisualId,
    accumulatedDirectCost = accumulatedDirectCost,
    isSystemProcess = isSystem,
    parentProcessId = parentProcessId
)
