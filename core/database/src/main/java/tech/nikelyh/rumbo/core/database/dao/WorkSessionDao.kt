package tech.nikelyh.rumbo.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.nikelyh.rumbo.core.database.model.WorkSessionEntity

@Dao
interface WorkSessionDao {

    @Query("SELECT * FROM work_sessions WHERE processId = :processId")
    fun getWorkSessionsByProcessId(processId: String): Flow<List<WorkSessionEntity>>

    @Query("SELECT * FROM work_sessions WHERE taskId = :taskId")
    fun getWorkSessionsByTaskId(taskId: String): Flow<List<WorkSessionEntity>>

    @Query("SELECT * FROM work_sessions WHERE id = :id")
    fun getWorkSessionById(id: String): Flow<WorkSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(session: WorkSessionEntity)

    @Query("DELETE FROM work_sessions WHERE id = :id")
    suspend fun deleteById(id: String): Int
}
