package tech.nikelyh.rumbo.core.data.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.database.dao.TaskDao
import tech.nikelyh.rumbo.core.database.model.TaskEntity

class FakeTaskDao : TaskDao {
    private val tasksFlow = MutableStateFlow<Map<String, TaskEntity>>(emptyMap())

    override fun getAllTasks(): Flow<List<TaskEntity>> {
        return tasksFlow.map { it.values.toList() }
    }

    override fun getTasksByProcessId(processId: String): Flow<List<TaskEntity>> {
        return tasksFlow.map { map ->
            map.values.filter { it.processId == processId }
        }
    }

    override fun getTaskById(id: String): Flow<TaskEntity?> {
        return tasksFlow.map { it[id] }
    }

    override suspend fun insertOrUpdate(task: TaskEntity) {
        tasksFlow.value = tasksFlow.value + (task.id to task)
    }

    override suspend fun deleteById(id: String): Int {
        val exists = tasksFlow.value.containsKey(id)
        if (exists) {
            tasksFlow.value = tasksFlow.value - id
            return 1
        }
        return 0
    }
}
