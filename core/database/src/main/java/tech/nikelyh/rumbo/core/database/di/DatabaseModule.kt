package tech.nikelyh.rumbo.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.nikelyh.rumbo.core.database.RumboDatabase
import tech.nikelyh.rumbo.core.database.model.ProcessEntity
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesRumboDatabase(
        @ApplicationContext context: Context,
        databaseProvider: Provider<RumboDatabase>
    ): RumboDatabase {
        return Room.databaseBuilder(
            context,
            RumboDatabase::class.java,
            "rumbo-database"
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val generalProcess = ProcessEntity(
                        id = "general",
                        name = "General",
                        description = "Proceso del sistema para tareas generales",
                        statusName = "ACTIVE",
                        createdAtEpochMillis = System.currentTimeMillis(),
                        finishedAtEpochMillis = null,
                        colorOrVisualId = "system_default",
                        accumulatedDirectCost = 0.0,
                        nextAction = null,
                        isSystemProcess = true
                    )
                    databaseProvider.get().processDao().insertOrUpdate(generalProcess)
                }
            }
        }).build()
    }
}
