package tech.nikelyh.rumbo.core.data.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.database.dao.ProcessDao
import tech.nikelyh.rumbo.core.database.model.ProcessEntity

class FakeProcessDao : ProcessDao {
    private val processesFlow = MutableStateFlow<Map<String, ProcessEntity>>(emptyMap())

    override fun getProcesses(): Flow<List<ProcessEntity>> {
        return processesFlow.map { it.values.toList() }
    }

    override fun getProcessById(id: String): Flow<ProcessEntity?> {
        return processesFlow.map { it[id] }
    }

    override suspend fun getProcessByIdSync(id: String): ProcessEntity? {
        return processesFlow.value[id]
    }

    override suspend fun insertOrUpdate(process: ProcessEntity) {
        processesFlow.value = processesFlow.value + (process.id to process)
    }

    override suspend fun deleteById(id: String): Int {
        val current = processesFlow.value[id]
        if (current != null && !current.isSystemProcess) {
            processesFlow.value = processesFlow.value - id
            return 1
        }
        return 0
    }

    override suspend fun getStarredCount(): Int {
        return processesFlow.value.values.count { it.isStarred }
    }
}
