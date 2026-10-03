package tech.nikelyh.rumbo.core.data.model

import tech.nikelyh.rumbo.core.database.model.ProcessEntity
import tech.nikelyh.rumbo.core.model.Process

fun ProcessEntity.asExternalModel(): Process = Process(
    id = id,
    title = title,
    description = description,
    category = category,
    createdAtEpochMillis = createdAtEpochMillis
)

fun Process.asEntity(): ProcessEntity = ProcessEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    createdAtEpochMillis = createdAtEpochMillis
)
