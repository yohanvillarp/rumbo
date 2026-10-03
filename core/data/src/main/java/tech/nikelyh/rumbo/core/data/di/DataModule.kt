package tech.nikelyh.rumbo.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.nikelyh.rumbo.core.data.repository.ProcessRepository
import tech.nikelyh.rumbo.core.data.repository.ProcessRepositoryImpl
import tech.nikelyh.rumbo.core.data.repository.SettingsRepository
import tech.nikelyh.rumbo.core.data.repository.SettingsRepositoryImpl
import tech.nikelyh.rumbo.core.data.repository.TaskRepository
import tech.nikelyh.rumbo.core.data.repository.TaskRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindProcessRepository(
        impl: ProcessRepositoryImpl
    ): ProcessRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
