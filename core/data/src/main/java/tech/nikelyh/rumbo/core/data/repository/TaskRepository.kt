package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import tech.nikelyh.rumbo.core.model.Task

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getTasksByProcessId(processId: String): Flow<List<Task>>
    fun getTaskById(id: String): Flow<Task?>
    suspend fun saveTask(task: Task)
    suspend fun deleteTask(id: String)
}
