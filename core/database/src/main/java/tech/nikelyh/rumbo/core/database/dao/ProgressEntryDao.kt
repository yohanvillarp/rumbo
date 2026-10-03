package tech.nikelyh.rumbo.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.nikelyh.rumbo.core.database.model.ProgressEntryEntity

@Dao
interface ProgressEntryDao {

    @Query("SELECT * FROM progress_entries WHERE processId = :processId ORDER BY dateEpochMillis DESC")
    fun getProgressEntriesByProcessId(processId: String): Flow<List<ProgressEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: ProgressEntryEntity)

    @Query("DELETE FROM progress_entries WHERE id = :id")
    suspend fun deleteById(id: String): Int
}
