package tech.nikelyh.rumbo.feature.processes.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.data.repository.WeeklyGoalRepository
import tech.nikelyh.rumbo.core.model.WeeklyGoal

class FakeWeeklyGoalRepository : WeeklyGoalRepository {
    private val flow = MutableStateFlow<Map<String, WeeklyGoal>>(emptyMap())

    override fun getWeeklyGoalsByProcessId(processId: String): Flow<List<WeeklyGoal>> {
        return flow.map { map -> map.values.filter { it.processId == processId } }
    }

    override fun getWeeklyGoalsByWeek(weekIdentifier: String): Flow<List<WeeklyGoal>> {
        return flow.map { map -> map.values.filter { it.weekIdentifier == weekIdentifier } }
    }

    override suspend fun saveWeeklyGoal(goal: WeeklyGoal) {
        flow.value = flow.value + (goal.id to goal)
    }

    override suspend fun deleteWeeklyGoal(id: String): Boolean {
        val exists = flow.value.containsKey(id)
        if (exists) {
            flow.value = flow.value - id
            return true
        }
        return false
    }
}
