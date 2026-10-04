package tech.nikelyh.rumbo.feature.home.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.data.repository.ProcessRepository
import tech.nikelyh.rumbo.core.model.Process

class FakeProcessRepository : ProcessRepository {
    private val processesFlow = MutableStateFlow<Map<String, Process>>(emptyMap())

    override fun getProcesses(): Flow<List<Process>> {
        return processesFlow.map { it.values.toList() }
    }

    override fun getProcessById(id: String): Flow<Process?> {
        return processesFlow.map { it[id] }
    }

    override suspend fun saveProcess(process: Process) {
        processesFlow.value = processesFlow.value + (process.id to process)
    }

    override suspend fun deleteProcess(id: String): Boolean {
        if (id == Process.GENERAL_PROCESS_ID) return false
        val exists = processesFlow.value.containsKey(id)
        if (exists) {
            processesFlow.value = processesFlow.value - id
            return true
        }
        return false
    }

    override suspend fun archiveProcess(id: String): Boolean {
        if (id == Process.GENERAL_PROCESS_ID) return false
        val process = processesFlow.value[id] ?: return false
        processesFlow.value = processesFlow.value + (id to process.archive())
        return true
    }

    override suspend fun toggleProcessStarred(id: String): tech.nikelyh.rumbo.core.data.repository.StarProcessResult {
        val current = processesFlow.value[id] ?: return tech.nikelyh.rumbo.core.data.repository.StarProcessResult.ProcessNotFound
        if (current.isStarred) {
            processesFlow.value = processesFlow.value + (id to current.copy(isStarred = false))
            return tech.nikelyh.rumbo.core.data.repository.StarProcessResult.Success
        }
        val count = processesFlow.value.values.count { it.isStarred }
        if (count >= 3) {
            return tech.nikelyh.rumbo.core.data.repository.StarProcessResult.MaxLimitReached
        }
        processesFlow.value = processesFlow.value + (id to current.copy(isStarred = true))
        return tech.nikelyh.rumbo.core.data.repository.StarProcessResult.Success
    }

    override suspend fun ensureGeneralProcessExists() {
        if (!processesFlow.value.containsKey(Process.GENERAL_PROCESS_ID)) {
            val general = Process.createGeneralProcess(1000L)
            processesFlow.value = processesFlow.value + (Process.GENERAL_PROCESS_ID to general)
        }
    }
}
