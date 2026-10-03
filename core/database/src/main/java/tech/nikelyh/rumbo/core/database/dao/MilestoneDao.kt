package tech.nikelyh.rumbo.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.nikelyh.rumbo.core.database.model.MilestoneEntity

@Dao
interface MilestoneDao {

    @Query("SELECT * FROM milestones WHERE processId = :processId")
    fun getMilestonesByProcessId(processId: String): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones WHERE id = :id")
    fun getMilestoneById(id: String): Flow<MilestoneEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(milestone: MilestoneEntity)

    @Query("DELETE FROM milestones WHERE id = :id")
    suspend fun deleteById(id: String): Int
}
