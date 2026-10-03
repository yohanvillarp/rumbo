package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.data.model.asEntity
import tech.nikelyh.rumbo.core.data.model.asExternalModel
import tech.nikelyh.rumbo.core.database.dao.WeeklyGoalDao
import tech.nikelyh.rumbo.core.model.WeeklyGoal
import javax.inject.Inject

class WeeklyGoalRepositoryImpl @Inject constructor(
    private val weeklyGoalDao: WeeklyGoalDao
) : WeeklyGoalRepository {

    override fun getWeeklyGoalsByProcessId(processId: String): Flow<List<WeeklyGoal>> {
        return weeklyGoalDao.getWeeklyGoalsByProcessId(processId).map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override fun getWeeklyGoalsByWeek(weekIdentifier: String): Flow<List<WeeklyGoal>> {
        return weeklyGoalDao.getWeeklyGoalsByWeek(weekIdentifier).map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override suspend fun saveWeeklyGoal(goal: WeeklyGoal) {
        weeklyGoalDao.insertOrUpdate(goal.asEntity())
    }

    override suspend fun deleteWeeklyGoal(id: String): Boolean {
        val rows = weeklyGoalDao.deleteById(id)
        return rows > 0
    }
}
