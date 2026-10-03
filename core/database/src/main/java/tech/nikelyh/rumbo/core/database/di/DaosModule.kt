package tech.nikelyh.rumbo.core.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.nikelyh.rumbo.core.database.RumboDatabase
import tech.nikelyh.rumbo.core.database.dao.ProcessDao
import tech.nikelyh.rumbo.core.database.dao.TaskDao

@Module
@InstallIn(SingletonComponent::class)
object DaosModule {

    @Provides
    fun providesProcessDao(database: RumboDatabase): ProcessDao = database.processDao()

    @Provides
    fun providesTaskDao(database: RumboDatabase): TaskDao = database.taskDao()
}
