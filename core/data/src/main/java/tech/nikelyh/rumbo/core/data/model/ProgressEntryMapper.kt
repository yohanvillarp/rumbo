package tech.nikelyh.rumbo.core.data.model

import tech.nikelyh.rumbo.core.database.model.ProgressEntryEntity
import tech.nikelyh.rumbo.core.model.ProgressEntry

fun ProgressEntryEntity.asExternalModel(): ProgressEntry = ProgressEntry(
    id = id,
    processId = processId,
    dateEpochMillis = dateEpochMillis,
    progressLevel = progressLevel,
    note = note
)

fun ProgressEntry.asEntity(): ProgressEntryEntity = ProgressEntryEntity(
    id = id,
    processId = processId,
    dateEpochMillis = dateEpochMillis,
    progressLevel = progressLevel,
    note = note
)
