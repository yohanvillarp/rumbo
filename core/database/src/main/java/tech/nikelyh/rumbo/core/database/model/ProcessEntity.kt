package tech.nikelyh.rumbo.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processes")
data class ProcessEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val createdAtEpochMillis: Long
)
