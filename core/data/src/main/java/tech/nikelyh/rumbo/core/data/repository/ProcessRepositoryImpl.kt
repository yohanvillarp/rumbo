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
        if (process.id == Process.GENERAL_PROCESS_ID) {
            val existing = processDao.getProcessByIdSync(Process.GENERAL_PROCESS_ID)
            if (existing != null) {
                return // General process cannot be edited
            }
        }
        processDao.insertOrUpdate(process.asEntity())
    }

    override suspend fun deleteProcess(id: String): Boolean {
        if (id == Process.GENERAL_PROCESS_ID) {
            return false // General process cannot be deleted
        }
        val rows = processDao.deleteById(id)
        return rows > 0
    }

    override suspend fun archiveProcess(id: String): Boolean {
        if (id == Process.GENERAL_PROCESS_ID) {
            return false // General process cannot be archived
        }
        val currentEntity = processDao.getProcessByIdSync(id) ?: return false
        val domain = currentEntity.asExternalModel()
        val archived = domain.archive()
        processDao.insertOrUpdate(archived.asEntity())
        return true
    }

    override suspend fun toggleProcessStarred(id: String): StarProcessResult {
        val currentEntity = processDao.getProcessByIdSync(id) ?: return StarProcessResult.ProcessNotFound
        if (currentEntity.isStarred) {
            processDao.insertOrUpdate(currentEntity.copy(isStarred = false))
            return StarProcessResult.Success
        }
        val starredCount = processDao.getStarredCount()
        if (starredCount >= 3) {
            return StarProcessResult.MaxLimitReached
        }
        processDao.insertOrUpdate(currentEntity.copy(isStarred = true))
        return StarProcessResult.Success
    }

    override suspend fun ensureGeneralProcessExists() {
        val existing = processDao.getProcessByIdSync(Process.GENERAL_PROCESS_ID)
        if (existing == null) {
            val general = Process.createGeneralProcess(System.currentTimeMillis())
            processDao.insertOrUpdate(general.asEntity())
        }
    }
}
