package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.data.model.asEntity
import tech.nikelyh.rumbo.core.data.model.asExternalModel
import tech.nikelyh.rumbo.core.database.dao.ProcessDao
import tech.nikelyh.rumbo.core.database.dao.TaskDao
import tech.nikelyh.rumbo.core.database.model.ProcessEntity
import tech.nikelyh.rumbo.core.database.model.TaskEntity
import tech.nikelyh.rumbo.core.model.Process
import java.util.ArrayDeque
import javax.inject.Inject

class ProcessRepositoryImpl @Inject constructor(
    private val processDao: ProcessDao,
    private val taskDao: TaskDao
) : ProcessRepository {

    override fun getProcesses(): Flow<List<Process>> {
        return combine(processDao.getProcesses(), taskDao.getAllTasks()) { processEntities, taskEntities ->
            calculateProcessesWithAccumulatedCost(processEntities, taskEntities)
        }
    }

    override fun getProcessById(id: String): Flow<Process?> {
        return combine(
            processDao.getProcessById(id),
            processDao.getProcesses(),
            taskDao.getAllTasks()
        ) { targetEntity, allProcessEntities, taskEntities ->
            if (targetEntity == null) return@combine null
            val processesWithCost = calculateProcessesWithAccumulatedCost(allProcessEntities, taskEntities)
            processesWithCost.firstOrNull { it.id == id } ?: run {
                val directCost = taskEntities.filter { it.processId == id }.sumOf { it.cost }
                targetEntity.asExternalModel().copy(accumulatedDirectCost = directCost)
            }
        }
    }

    private fun calculateProcessesWithAccumulatedCost(
        processEntities: List<ProcessEntity>,
        taskEntities: List<TaskEntity>
    ): List<Process> {
        val childrenMap = mutableMapOf<String, MutableList<String>>()
        for (entity in processEntities) {
            val parentId = entity.parentProcessId
            if (parentId != null) {
                childrenMap.getOrPut(parentId) { mutableListOf() }.add(entity.id)
            }
        }

        val directCostByProcessId = taskEntities
            .groupBy { it.processId }
            .mapValues { (_, tasks) -> tasks.sumOf { it.cost } }

        fun getDescendantIds(rootId: String): Set<String> {
            val descendants = mutableSetOf<String>()
            val queue = ArrayDeque<String>()
            queue.add(rootId)
            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                val children = childrenMap[current] ?: continue
                for (child in children) {
                    if (descendants.add(child)) {
                        queue.add(child)
                    }
                }
            }
            return descendants
        }

        return processEntities.map { entity ->
            val domain = entity.asExternalModel()
            val allTargetIds = getDescendantIds(entity.id) + entity.id
            val totalCost = allTargetIds.sumOf { directCostByProcessId[it] ?: 0.0 }
            domain.copy(accumulatedDirectCost = totalCost)
        }
    }

    override suspend fun saveProcess(process: Process) {
        if (process.id == Process.GENERAL_PROCESS_ID) {
            val existing = processDao.getProcessByIdSync(Process.GENERAL_PROCESS_ID)
            if (existing != null) {
                return // General process cannot be edited
            }
        }
        processDao.insertOrUpdate(process.asEntity())
    }

    override suspend fun deleteProcess(id: String): Boolean {
        if (id == Process.GENERAL_PROCESS_ID) {
            return false // General process cannot be deleted
        }
        val rows = processDao.deleteById(id)
        return rows > 0
    }

    override suspend fun archiveProcess(id: String): Boolean {
        if (id == Process.GENERAL_PROCESS_ID) {
            return false // General process cannot be archived
        }
        val currentEntity = processDao.getProcessByIdSync(id) ?: return false
        val domain = currentEntity.asExternalModel()
        val archived = domain.archive()
        processDao.insertOrUpdate(archived.asEntity())
        return true
    }

    override suspend fun toggleProcessStarred(id: String): StarProcessResult {
        val currentEntity = processDao.getProcessByIdSync(id) ?: return StarProcessResult.ProcessNotFound
        if (currentEntity.isStarred) {
            processDao.insertOrUpdate(currentEntity.copy(isStarred = false))
            return StarProcessResult.Success
        }
        val starredCount = processDao.getStarredCount()
        if (starredCount >= 3) {
            return StarProcessResult.MaxLimitReached
        }
        processDao.insertOrUpdate(currentEntity.copy(isStarred = true))
        return StarProcessResult.Success
    }

    override suspend fun ensureGeneralProcessExists() {
        val existing = processDao.getProcessByIdSync(Process.GENERAL_PROCESS_ID)
        if (existing == null) {
            val general = Process.createGeneralProcess(System.currentTimeMillis())
            processDao.insertOrUpdate(general.asEntity())
        }
    }
}
