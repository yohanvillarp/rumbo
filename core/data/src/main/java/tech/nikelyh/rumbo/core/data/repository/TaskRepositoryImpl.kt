package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.data.model.asEntity
import tech.nikelyh.rumbo.core.data.model.asExternalModel
import tech.nikelyh.rumbo.core.database.dao.TaskDao
import tech.nikelyh.rumbo.core.model.Task
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override fun getTasksByProcessId(processId: String): Flow<List<Task>> {
        return taskDao.getTasksByProcessId(processId).map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override fun getTaskById(id: String): Flow<Task?> {
        return taskDao.getTaskById(id).map { it?.asExternalModel() }
    }

    override suspend fun saveTask(task: Task) {
        taskDao.insertOrUpdate(task.asEntity())
    }

    override suspend fun deleteTask(id: String): Boolean {
        val rows = taskDao.deleteById(id)
        return rows > 0
    }
}
