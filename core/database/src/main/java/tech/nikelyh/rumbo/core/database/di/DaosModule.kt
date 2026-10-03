package tech.nikelyh.rumbo.core.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.nikelyh.rumbo.core.database.RumboDatabase
import tech.nikelyh.rumbo.core.database.dao.MilestoneDao
import tech.nikelyh.rumbo.core.database.dao.ProcessDao
import tech.nikelyh.rumbo.core.database.dao.ProgressEntryDao
import tech.nikelyh.rumbo.core.database.dao.TaskDao
import tech.nikelyh.rumbo.core.database.dao.WeeklyGoalDao
import tech.nikelyh.rumbo.core.database.dao.WorkSessionDao

@Module
@InstallIn(SingletonComponent::class)
object DaosModule {

    @Provides
    fun providesProcessDao(database: RumboDatabase): ProcessDao = database.processDao()

    @Provides
    fun providesTaskDao(database: RumboDatabase): TaskDao = database.taskDao()

    @Provides
    fun providesMilestoneDao(database: RumboDatabase): MilestoneDao = database.milestoneDao()

    @Provides
    fun providesWorkSessionDao(database: RumboDatabase): WorkSessionDao = database.workSessionDao()

    @Provides
    fun providesProgressEntryDao(database: RumboDatabase): ProgressEntryDao = database.progressEntryDao()

    @Provides
    fun providesWeeklyGoalDao(database: RumboDatabase): WeeklyGoalDao = database.weeklyGoalDao()
}
