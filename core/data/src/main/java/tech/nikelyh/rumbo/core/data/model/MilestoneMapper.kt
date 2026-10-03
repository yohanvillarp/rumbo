package tech.nikelyh.rumbo.core.data.model

import tech.nikelyh.rumbo.core.database.model.MilestoneEntity
import tech.nikelyh.rumbo.core.model.Milestone

fun MilestoneEntity.asExternalModel(): Milestone = Milestone(
    id = id,
    processId = processId,
    title = title,
    description = description,
    isCompleted = isCompleted,
    targetDateEpochMillis = targetDateEpochMillis,
    completedAtEpochMillis = completedAtEpochMillis
)

fun Milestone.asEntity(): MilestoneEntity = MilestoneEntity(
    id = id,
    processId = processId,
    title = title,
    description = description,
    isCompleted = isCompleted,
    targetDateEpochMillis = targetDateEpochMillis,
    completedAtEpochMillis = completedAtEpochMillis
)
