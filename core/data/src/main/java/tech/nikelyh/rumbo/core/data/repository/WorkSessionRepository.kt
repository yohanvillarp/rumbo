package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import tech.nikelyh.rumbo.core.model.WorkSession

interface WorkSessionRepository {
    fun getWorkSessionsByProcessId(processId: String): Flow<List<WorkSession>>
    fun getWorkSessionsByTaskId(taskId: String): Flow<List<WorkSession>>
    fun getWorkSessionById(id: String): Flow<WorkSession?>
    suspend fun saveWorkSession(session: WorkSession)
    suspend fun deleteWorkSession(id: String): Boolean
}
