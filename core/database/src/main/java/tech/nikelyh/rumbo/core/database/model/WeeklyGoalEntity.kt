package tech.nikelyh.rumbo.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weekly_goals",
    indices = [
        Index(value = ["processId"]),
        Index(value = ["weekIdentifier"])
    ]
)
data class WeeklyGoalEntity(
    @PrimaryKey
    val id: String,
    val processId: String,
    val weekIdentifier: String,
    val description: String,
    val statusName: String
)
