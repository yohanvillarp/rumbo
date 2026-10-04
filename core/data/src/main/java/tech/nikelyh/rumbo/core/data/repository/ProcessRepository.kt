package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import tech.nikelyh.rumbo.core.model.Process

/**
 * Result of attempting to star/unstar a process.
 */
sealed interface StarProcessResult {
    data object Success : StarProcessResult
    data object MaxLimitReached : StarProcessResult
    data object ProcessNotFound : StarProcessResult
}

interface ProcessRepository {
    fun getProcesses(): Flow<List<Process>>
    fun getProcessById(id: String): Flow<Process?>
    suspend fun saveProcess(process: Process)
    suspend fun deleteProcess(id: String): Boolean
    suspend fun archiveProcess(id: String): Boolean
    suspend fun toggleProcessStarred(id: String): StarProcessResult
    suspend fun ensureGeneralProcessExists()
}
