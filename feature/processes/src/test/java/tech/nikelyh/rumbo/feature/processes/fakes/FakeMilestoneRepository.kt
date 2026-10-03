package tech.nikelyh.rumbo.feature.processes.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.data.repository.MilestoneRepository
import tech.nikelyh.rumbo.core.model.Milestone

class FakeMilestoneRepository : MilestoneRepository {
    private val flow = MutableStateFlow<Map<String, Milestone>>(emptyMap())

    override fun getMilestonesByProcessId(processId: String): Flow<List<Milestone>> {
        return flow.map { map -> map.values.filter { it.processId == processId } }
    }

    override fun getMilestoneById(id: String): Flow<Milestone?> {
        return flow.map { it[id] }
    }

    override suspend fun saveMilestone(milestone: Milestone) {
        flow.value = flow.value + (milestone.id to milestone)
    }

    override suspend fun deleteMilestone(id: String): Boolean {
        val exists = flow.value.containsKey(id)
        if (exists) {
            flow.value = flow.value - id
            return true
        }
        return false
    }
}
