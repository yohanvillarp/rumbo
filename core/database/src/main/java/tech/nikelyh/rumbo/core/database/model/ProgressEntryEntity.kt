package tech.nikelyh.rumbo.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "progress_entries",
    indices = [
        Index(value = ["processId"])
    ]
)
data class ProgressEntryEntity(
    @PrimaryKey
    val id: String,
    val processId: String,
    val dateEpochMillis: Long,
    val progressLevel: Int,
    val note: String?
)
