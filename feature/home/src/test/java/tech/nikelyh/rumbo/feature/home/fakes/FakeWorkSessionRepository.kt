package tech.nikelyh.rumbo.feature.home.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.data.repository.WorkSessionRepository
import tech.nikelyh.rumbo.core.model.WorkSession

class FakeWorkSessionRepository : WorkSessionRepository {
    private val flow = MutableStateFlow<Map<String, WorkSession>>(emptyMap())

    override fun getAllWorkSessions(): Flow<List<WorkSession>> {
        return flow.map { it.values.toList() }
    }

    override fun getWorkSessionsByProcessId(processId: String): Flow<List<WorkSession>> {
        return flow.map { map -> map.values.filter { it.processId == processId } }
    }

    override fun getWorkSessionsByTaskId(taskId: String): Flow<List<WorkSession>> {
        return flow.map { map -> map.values.filter { it.taskId == taskId } }
    }

    override fun getWorkSessionById(id: String): Flow<WorkSession?> {
        return flow.map { it[id] }
    }

    override suspend fun saveWorkSession(session: WorkSession) {
        flow.value = flow.value + (session.id to session)
    }

    override suspend fun deleteWorkSession(id: String): Boolean {
        val exists = flow.value.containsKey(id)
        if (exists) {
            flow.value = flow.value - id
            return true
        }
        return false
    }
}
