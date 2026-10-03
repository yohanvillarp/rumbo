package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.nikelyh.rumbo.core.data.fakes.FakeTaskDao
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.Task

class TaskRepositoryTest {

    private lateinit var taskDao: FakeTaskDao
    private lateinit var repository: TaskRepositoryImpl

    @Before
    fun setUp() {
        taskDao = FakeTaskDao()
        repository = TaskRepositoryImpl(taskDao)
    }

    @Test
    fun `task defaults to General processId if not specified`() = runBlocking {
        val task = Task(
            id = "t1",
            title = "Tarea General",
            createdAtEpochMillis = 1000L
        )

        assertEquals(Process.GENERAL_PROCESS_ID, task.processId)
        assertEquals(0.0, task.cost, 0.001)

        repository.saveTask(task)

        val generalTasks = repository.getTasksByProcessId(Process.GENERAL_PROCESS_ID).first()
        assertEquals(1, generalTasks.size)
        assertEquals("Tarea General", generalTasks.first().title)
    }

    @Test
    fun `task belongs to specified processId and can be queried`() = runBlocking {
        val task = Task(
            id = "t2",
            processId = "process_abc",
            title = "Tarea Especifica",
            createdAtEpochMillis = 1000L,
            cost = 120.0
        )

        repository.saveTask(task)

        val fetched = repository.getTaskById("t2").first()
        assertNotNull(fetched)
        assertEquals("process_abc", fetched?.processId)
        assertEquals(120.0, fetched?.cost ?: 0.0, 0.001)

        val processTasks = repository.getTasksByProcessId("process_abc").first()
        assertEquals(1, processTasks.size)

        val deleted = repository.deleteTask("t2")
        assertTrue(deleted)

        val emptyTasks = repository.getTasksByProcessId("process_abc").first()
        assertTrue(emptyTasks.isEmpty())
    }
}
