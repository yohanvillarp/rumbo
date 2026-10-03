package tech.nikelyh.rumbo.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey
    val id: String,
    val processId: String,
    val title: String,
    val description: String?,
    val isCompleted: Boolean,
    val targetDateEpochMillis: Long?,
    val completedAtEpochMillis: Long?
)
