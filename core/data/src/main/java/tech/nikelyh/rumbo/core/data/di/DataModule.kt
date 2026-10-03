package tech.nikelyh.rumbo.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.nikelyh.rumbo.core.data.repository.MilestoneRepository
import tech.nikelyh.rumbo.core.data.repository.MilestoneRepositoryImpl
import tech.nikelyh.rumbo.core.data.repository.ProcessRepository
import tech.nikelyh.rumbo.core.data.repository.ProcessRepositoryImpl
import tech.nikelyh.rumbo.core.data.repository.ProgressRepository
import tech.nikelyh.rumbo.core.data.repository.ProgressRepositoryImpl
import tech.nikelyh.rumbo.core.data.repository.SettingsRepository
import tech.nikelyh.rumbo.core.data.repository.SettingsRepositoryImpl
import tech.nikelyh.rumbo.core.data.repository.TaskRepository
import tech.nikelyh.rumbo.core.data.repository.TaskRepositoryImpl
import tech.nikelyh.rumbo.core.data.repository.WeeklyGoalRepository
import tech.nikelyh.rumbo.core.data.repository.WeeklyGoalRepositoryImpl
import tech.nikelyh.rumbo.core.data.repository.WorkSessionRepository
import tech.nikelyh.rumbo.core.data.repository.WorkSessionRepositoryImpl
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
    abstract fun bindMilestoneRepository(
        impl: MilestoneRepositoryImpl
    ): MilestoneRepository

    @Binds
    @Singleton
    abstract fun bindWorkSessionRepository(
        impl: WorkSessionRepositoryImpl
    ): WorkSessionRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(
        impl: ProgressRepositoryImpl
    ): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindWeeklyGoalRepository(
        impl: WeeklyGoalRepositoryImpl
    ): WeeklyGoalRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
