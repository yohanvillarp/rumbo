package tech.nikelyh.rumbo.feature.processes.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.data.repository.ProgressRepository
import tech.nikelyh.rumbo.core.model.ProgressEntry

class FakeProgressRepository : ProgressRepository {
    private val flow = MutableStateFlow<Map<String, ProgressEntry>>(emptyMap())

    override fun getAllProgressEntries(): Flow<List<ProgressEntry>> {
        return flow.map { it.values.toList() }
    }

    override fun getProgressEntriesByProcessId(processId: String): Flow<List<ProgressEntry>> {
        return flow.map { map -> map.values.filter { it.processId == processId } }
    }

    override suspend fun saveProgressEntry(entry: ProgressEntry) {
        flow.value = flow.value + (entry.id to entry)
    }

    override suspend fun deleteProgressEntry(id: String): Boolean {
        val exists = flow.value.containsKey(id)
        if (exists) {
            flow.value = flow.value - id
            return true
        }
        return false
    }
}
