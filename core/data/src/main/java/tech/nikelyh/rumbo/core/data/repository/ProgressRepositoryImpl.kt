package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.data.model.asEntity
import tech.nikelyh.rumbo.core.data.model.asExternalModel
import tech.nikelyh.rumbo.core.database.dao.ProgressEntryDao
import tech.nikelyh.rumbo.core.model.ProgressEntry
import javax.inject.Inject

class ProgressRepositoryImpl @Inject constructor(
    private val progressEntryDao: ProgressEntryDao
) : ProgressRepository {

    override fun getAllProgressEntries(): Flow<List<ProgressEntry>> {
        return progressEntryDao.getAllProgressEntries().map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override fun getProgressEntriesByProcessId(processId: String): Flow<List<ProgressEntry>> {
        return progressEntryDao.getProgressEntriesByProcessId(processId).map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override suspend fun saveProgressEntry(entry: ProgressEntry) {
        progressEntryDao.insertOrUpdate(entry.asEntity())
    }

    override suspend fun deleteProgressEntry(id: String): Boolean {
        val rows = progressEntryDao.deleteById(id)
        return rows > 0
    }
}
