package tech.nikelyh.rumbo.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.nikelyh.rumbo.core.database.model.ProcessEntity

@Dao
interface ProcessDao {

    @Query("SELECT * FROM processes")
    fun getProcesses(): Flow<List<ProcessEntity>>

    @Query("SELECT * FROM processes WHERE id = :id")
    fun getProcessById(id: String): Flow<ProcessEntity?>

    @Query("SELECT * FROM processes WHERE id = :id")
    suspend fun getProcessByIdSync(id: String): ProcessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(process: ProcessEntity)

    @Query("DELETE FROM processes WHERE id = :id AND isSystemProcess = 0")
    suspend fun deleteById(id: String): Int
}
