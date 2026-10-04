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
import tech.nikelyh.rumbo.core.database.RumboDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesRumboDatabase(
        @ApplicationContext context: Context
    ): RumboDatabase {
        return Room.databaseBuilder(
            context,
            RumboDatabase::class.java,
            "rumbo-database"
        ).fallbackToDestructiveMigration(dropAllTables = true)
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val now = System.currentTimeMillis()
                db.execSQL(
                    "INSERT OR REPLACE INTO processes (id, name, description, statusName, createdAtEpochMillis, finishedAtEpochMillis, colorOrVisualId, accumulatedDirectCost, isSystemProcess, parentProcessId) " +
                    "VALUES ('general', 'General', 'Espacio para actividades cotidianas y tareas varias', 'ACTIVE', $now, NULL, 'system_default', 0.0, 1, NULL)"
                )
            }
        }).build()
    }
}
