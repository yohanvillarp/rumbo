package tech.nikelyh.rumbo.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tech.nikelyh.rumbo.core.database.RumboDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesRumboDatabase(
        @ApplicationContext context: Context
    ): RumboDatabase = Room.databaseBuilder(
        context,
        RumboDatabase::class.java,
        "rumbo-database"
    ).build()
}
