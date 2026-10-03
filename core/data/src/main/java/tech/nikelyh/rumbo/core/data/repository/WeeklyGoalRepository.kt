package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import tech.nikelyh.rumbo.core.model.WeeklyGoal

interface WeeklyGoalRepository {
    fun getWeeklyGoalsByProcessId(processId: String): Flow<List<WeeklyGoal>>
    fun getWeeklyGoalsByWeek(weekIdentifier: String): Flow<List<WeeklyGoal>>
    suspend fun saveWeeklyGoal(goal: WeeklyGoal)
    suspend fun deleteWeeklyGoal(id: String): Boolean
}
