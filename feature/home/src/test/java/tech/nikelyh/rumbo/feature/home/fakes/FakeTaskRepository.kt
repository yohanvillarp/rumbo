package tech.nikelyh.rumbo.feature.home.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.data.repository.TaskRepository
import tech.nikelyh.rumbo.core.model.Task

class FakeTaskRepository : TaskRepository {
    private val tasksFlow = MutableStateFlow<Map<String, Task>>(emptyMap())

    override fun getAllTasks(): Flow<List<Task>> {
        return tasksFlow.map { it.values.toList() }
    }

    override fun getTasksByProcessId(processId: String): Flow<List<Task>> {
        return tasksFlow.map { map -> map.values.filter { it.processId == processId } }
    }

    override fun getTaskById(id: String): Flow<Task?> {
        return tasksFlow.map { it[id] }
    }

    override suspend fun saveTask(task: Task) {
        tasksFlow.value = tasksFlow.value + (task.id to task)
    }

    override suspend fun deleteTask(id: String): Boolean {
        val exists = tasksFlow.value.containsKey(id)
        if (exists) {
            tasksFlow.value = tasksFlow.value - id
            return true
        }
        return false
    }
}
