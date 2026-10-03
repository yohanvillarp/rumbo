package tech.nikelyh.rumbo.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processes")
data class ProcessEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val statusName: String,
    val createdAtEpochMillis: Long,
    val finishedAtEpochMillis: Long?,
    val colorOrVisualId: String,
    val accumulatedDirectCost: Double,
    val nextAction: String?
)
