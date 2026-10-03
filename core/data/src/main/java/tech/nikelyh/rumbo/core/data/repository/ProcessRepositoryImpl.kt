package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.data.model.asEntity
import tech.nikelyh.rumbo.core.data.model.asExternalModel
import tech.nikelyh.rumbo.core.database.dao.ProcessDao
import tech.nikelyh.rumbo.core.model.Process
import javax.inject.Inject

class ProcessRepositoryImpl @Inject constructor(
    private val processDao: ProcessDao
) : ProcessRepository {

    override fun getProcesses(): Flow<List<Process>> {
        return processDao.getProcesses().map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override fun getProcessById(id: String): Flow<Process?> {
        return processDao.getProcessById(id).map { it?.asExternalModel() }
    }

    override suspend fun saveProcess(process: Process) {
        processDao.insertOrUpdate(process.asEntity())
    }

    override suspend fun deleteProcess(id: String) {
        processDao.deleteById(id)
    }
}
