package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import tech.nikelyh.rumbo.core.model.ProgressEntry

interface ProgressRepository {
    fun getProgressEntriesByProcessId(processId: String): Flow<List<ProgressEntry>>
    suspend fun saveProgressEntry(entry: ProgressEntry)
    suspend fun deleteProgressEntry(id: String): Boolean
}
