package tech.nikelyh.rumbo.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessTest {

    @Test
    fun `process creation default status is ACTIVE`() {
        val process = Process(
            id = "p1",
            name = "Aprender Kotlin",
            createdAtEpochMillis = 1000L,
            colorOrVisualId = "blue"
        )

        assertEquals(ProcessStatus.ACTIVE, process.status)
        assertTrue(process.isActive)
        assertFalse(process.isPaused)
        assertFalse(process.isFinished)
        assertEquals(0.0, process.accumulatedDirectCost, 0.001)
    }

    @Test
    fun `addDirectCost accumulates costs correctly`() {
        val initialProcess = Process(
            id = "p1",
            name = "Proyecto Rumbo",
            createdAtEpochMillis = 1000L,
            colorOrVisualId = "blue",
            accumulatedDirectCost = 50.0
        )

        val updatedProcess = initialProcess.addDirectCost(25.5)

        assertEquals(75.5, updatedProcess.accumulatedDirectCost, 0.001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `addDirectCost throws exception for negative amount`() {
        val process = Process(
            id = "p1",
            name = "Proyecto",
            createdAtEpochMillis = 1000L,
            colorOrVisualId = "blue"
        )

        process.addDirectCost(-10.0)
    }

    @Test
    fun `process state transitions pause resume finish archive`() {
        val process = Process(
            id = "p1",
            name = "Proceso Larga Duracion",
            createdAtEpochMillis = 1000L,
            colorOrVisualId = "green"
        )

        val paused = process.pause()
        assertEquals(ProcessStatus.PAUSED, paused.status)
        assertTrue(paused.isPaused)

        val resumed = paused.resume()
        assertEquals(ProcessStatus.ACTIVE, resumed.status)
        assertTrue(resumed.isActive)

        val finished = resumed.finish(2000L)
        assertEquals(ProcessStatus.COMPLETED, finished.status)
        assertTrue(finished.isFinished)
        assertEquals(2000L, finished.finishedAtEpochMillis)

        val archived = finished.archive()
        assertEquals(ProcessStatus.ARCHIVED, archived.status)
        assertTrue(archived.isFinished)

        val reopened = finished.reopen()
        assertEquals(ProcessStatus.ACTIVE, reopened.status)
        assertTrue(reopened.isActive)
        assertFalse(reopened.isFinished)
        assertNull(reopened.finishedAtEpochMillis)
    }

    @Test
    fun `general process system creation`() {
        val general = Process.createGeneralProcess(1000L)

        assertEquals(Process.GENERAL_PROCESS_ID, general.id)
        assertEquals("General", general.name)
        assertTrue(general.isActive)
    }
}
