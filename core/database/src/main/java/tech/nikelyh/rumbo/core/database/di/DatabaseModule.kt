package tech.nikelyh.rumbo.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS index_processes_parentProcessId ON processes(parentProcessId)")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Version 2 to 3 migration
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE processes ADD COLUMN isStarred INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE processes ADD COLUMN dueDateEpochMillis INTEGER DEFAULT NULL")
        }
    }

    private fun insertGeneralProcess(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        db.execSQL(
            "INSERT OR REPLACE INTO processes (id, name, description, statusName, createdAtEpochMillis, finishedAtEpochMillis, colorOrVisualId, accumulatedDirectCost, isStarred, isSystemProcess, parentProcessId, dueDateEpochMillis) " +
            "VALUES ('general', 'General', 'Espacio para actividades cotidianas y tareas varias', 'ACTIVE', $now, NULL, 'system_default', 0.0, 0, 1, NULL, NULL)"
        )
    }

    @Provides
    @Singleton
    fun providesRumboDatabase(
        @ApplicationContext context: Context
    ): RumboDatabase {
        return Room.databaseBuilder(
            context,
            RumboDatabase::class.java,
            "rumbo-database"
        )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                insertGeneralProcess(db)
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                insertGeneralProcess(db)
            }
        }).build()
    }
}
