package tech.nikelyh.rumbo.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.nikelyh.rumbo.core.database.model.WeeklyGoalEntity

@Dao
interface WeeklyGoalDao {

    @Query("SELECT * FROM weekly_goals WHERE processId = :processId")
    fun getWeeklyGoalsByProcessId(processId: String): Flow<List<WeeklyGoalEntity>>

    @Query("SELECT * FROM weekly_goals WHERE weekIdentifier = :weekIdentifier")
    fun getWeeklyGoalsByWeek(weekIdentifier: String): Flow<List<WeeklyGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(goal: WeeklyGoalEntity)

    @Query("DELETE FROM weekly_goals WHERE id = :id")
    suspend fun deleteById(id: String): Int
}
