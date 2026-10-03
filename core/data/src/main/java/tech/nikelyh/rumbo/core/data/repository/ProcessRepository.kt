package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import tech.nikelyh.rumbo.core.model.Process

interface ProcessRepository {
    fun getProcesses(): Flow<List<Process>>
    fun getProcessById(id: String): Flow<Process?>
    suspend fun saveProcess(process: Process)
    suspend fun deleteProcess(id: String): Boolean
    suspend fun archiveProcess(id: String): Boolean
    suspend fun ensureGeneralProcessExists()
}
