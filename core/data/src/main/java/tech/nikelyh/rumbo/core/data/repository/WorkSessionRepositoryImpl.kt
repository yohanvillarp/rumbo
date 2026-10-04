package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.data.datasource.RumboPreferencesDataSource
import tech.nikelyh.rumbo.core.data.model.asEntity
import tech.nikelyh.rumbo.core.data.model.asExternalModel
import tech.nikelyh.rumbo.core.database.dao.WorkSessionDao
import tech.nikelyh.rumbo.core.model.ActiveSessionState
import tech.nikelyh.rumbo.core.model.WorkSession
import javax.inject.Inject

class WorkSessionRepositoryImpl @Inject constructor(
    private val workSessionDao: WorkSessionDao,
    private val preferencesDataSource: RumboPreferencesDataSource
) : WorkSessionRepository {

    override val activeSessionState: Flow<ActiveSessionState> = preferencesDataSource.activeSessionState

    override fun getAllWorkSessions(): Flow<List<WorkSession>> {
        return workSessionDao.getAllWorkSessions().map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override fun getWorkSessionsByProcessId(processId: String): Flow<List<WorkSession>> {
        return workSessionDao.getWorkSessionsByProcessId(processId).map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override fun getWorkSessionsByTaskId(taskId: String): Flow<List<WorkSession>> {
        return workSessionDao.getWorkSessionsByTaskId(taskId).map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override fun getWorkSessionById(id: String): Flow<WorkSession?> {
        return workSessionDao.getWorkSessionById(id).map { it?.asExternalModel() }
    }

    override suspend fun saveWorkSession(session: WorkSession) {
        workSessionDao.insertOrUpdate(session.asEntity())
    }

    override suspend fun deleteWorkSession(id: String): Boolean {
        val rows = workSessionDao.deleteById(id)
        return rows > 0
    }

    override suspend fun saveActiveSessionState(state: ActiveSessionState) {
        preferencesDataSource.saveActiveSessionState(state)
    }

    override suspend fun clearActiveSessionState() {
        preferencesDataSource.clearActiveSessionState()
    }
}
