package tech.nikelyh.rumbo.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tech.nikelyh.rumbo.core.database.dao.MilestoneDao
import tech.nikelyh.rumbo.core.database.dao.ProcessDao
import tech.nikelyh.rumbo.core.database.dao.ProgressEntryDao
import tech.nikelyh.rumbo.core.database.dao.TaskDao
import tech.nikelyh.rumbo.core.database.dao.WeeklyGoalDao
import tech.nikelyh.rumbo.core.database.dao.WorkSessionDao
import tech.nikelyh.rumbo.core.database.model.MilestoneEntity
import tech.nikelyh.rumbo.core.database.model.ProcessEntity
import tech.nikelyh.rumbo.core.database.model.ProgressEntryEntity
import tech.nikelyh.rumbo.core.database.model.TaskEntity
import tech.nikelyh.rumbo.core.database.model.WeeklyGoalEntity
import tech.nikelyh.rumbo.core.database.model.WorkSessionEntity
import tech.nikelyh.rumbo.core.database.util.Converters

@Database(
    entities = [
        ProcessEntity::class,
        TaskEntity::class,
        MilestoneEntity::class,
        WorkSessionEntity::class,
        ProgressEntryEntity::class,
        WeeklyGoalEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RumboDatabase : RoomDatabase() {
    abstract fun processDao(): ProcessDao
    abstract fun taskDao(): TaskDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun workSessionDao(): WorkSessionDao
    abstract fun progressEntryDao(): ProgressEntryDao
    abstract fun weeklyGoalDao(): WeeklyGoalDao
}
