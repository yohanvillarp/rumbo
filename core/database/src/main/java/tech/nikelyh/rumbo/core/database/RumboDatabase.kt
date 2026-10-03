package tech.nikelyh.rumbo.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tech.nikelyh.rumbo.core.database.dao.ProcessDao
import tech.nikelyh.rumbo.core.database.dao.TaskDao
import tech.nikelyh.rumbo.core.database.model.ProcessEntity
import tech.nikelyh.rumbo.core.database.model.TaskEntity
import tech.nikelyh.rumbo.core.database.util.Converters

@Database(
    entities = [
        ProcessEntity::class,
        TaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RumboDatabase : RoomDatabase() {
    abstract fun processDao(): ProcessDao
    abstract fun taskDao(): TaskDao
}
