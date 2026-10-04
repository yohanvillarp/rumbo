package tech.nikelyh.rumbo.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val processId: String,
    val title: String,
    val description: String?,
    val statusName: String,
    val priorityName: String,
    val createdAtEpochMillis: Long,
    val dueDateEpochMillis: Long?,
    val estimatedDurationMinutes: Int?,
    val cost: Double,
    val finishedAtEpochMillis: Long?,
    val timeWorkedMillis: Long = 0L
)
