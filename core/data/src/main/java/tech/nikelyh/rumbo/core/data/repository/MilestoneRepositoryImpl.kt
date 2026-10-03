package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.data.model.asEntity
import tech.nikelyh.rumbo.core.data.model.asExternalModel
import tech.nikelyh.rumbo.core.database.dao.MilestoneDao
import tech.nikelyh.rumbo.core.model.Milestone
import javax.inject.Inject

class MilestoneRepositoryImpl @Inject constructor(
    private val milestoneDao: MilestoneDao
) : MilestoneRepository {

    override fun getMilestonesByProcessId(processId: String): Flow<List<Milestone>> {
        return milestoneDao.getMilestonesByProcessId(processId).map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override fun getMilestoneById(id: String): Flow<Milestone?> {
        return milestoneDao.getMilestoneById(id).map { it?.asExternalModel() }
    }

    override suspend fun saveMilestone(milestone: Milestone) {
        milestoneDao.insertOrUpdate(milestone.asEntity())
    }

    override suspend fun deleteMilestone(id: String): Boolean {
        val rows = milestoneDao.deleteById(id)
        return rows > 0
    }
}
