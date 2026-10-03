package tech.nikelyh.rumbo.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_sessions")
data class WorkSessionEntity(
    @PrimaryKey
    val id: String,
    val processId: String,
    val taskId: String?,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long?,
    val durationMillis: Long,
    val note: String?
)
