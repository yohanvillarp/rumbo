package tech.nikelyh.rumbo.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "work_sessions",
    indices = [
        Index(value = ["processId"]),
        Index(value = ["taskId"])
    ]
)
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
