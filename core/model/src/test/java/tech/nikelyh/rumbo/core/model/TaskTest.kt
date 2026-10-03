package tech.nikelyh.rumbo.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskTest {

    @Test
    fun `task default cost is zero and belongs to general process by default`() {
        val task = Task(
            id = "t1",
            title = "Tarea Rapida",
            createdAtEpochMillis = 1000L
        )

        assertEquals(0.0, task.cost, 0.001)
        assertEquals(Process.GENERAL_PROCESS_ID, task.processId)
        assertEquals(TaskStatus.PENDING, task.status)
        assertFalse(task.isCompleted)
    }

    @Test
    fun `task belongs to specified processId`() {
        val task = Task(
            id = "t2",
            processId = "process_123",
            title = "Escribir documentacion",
            createdAtEpochMillis = 1000L,
            cost = 15.0
        )

        assertEquals("process_123", task.processId)
        assertEquals(15.0, task.cost, 0.001)
    }

    @Test
    fun `completing task updates status and finished timestamp`() {
        val task = Task(
            id = "t3",
            processId = "p1",
            title = "Revisar codigo",
            createdAtEpochMillis = 1000L
        )

        val completedTask = task.complete(2000L)

        assertEquals(TaskStatus.COMPLETED, completedTask.status)
        assertTrue(completedTask.isCompleted)
        assertEquals(2000L, completedTask.finishedAtEpochMillis)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `task cost cannot be negative`() {
        Task(
            id = "t4",
            title = "Invalida",
            createdAtEpochMillis = 1000L,
            cost = -5.0
        )
    }
}
