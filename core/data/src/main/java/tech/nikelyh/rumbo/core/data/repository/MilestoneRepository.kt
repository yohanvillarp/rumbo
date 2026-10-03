package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import tech.nikelyh.rumbo.core.model.Milestone

interface MilestoneRepository {
    fun getMilestonesByProcessId(processId: String): Flow<List<Milestone>>
    fun getMilestoneById(id: String): Flow<Milestone?>
    suspend fun saveMilestone(milestone: Milestone)
    suspend fun deleteMilestone(id: String): Boolean
}
