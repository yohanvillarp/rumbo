package tech.nikelyh.rumbo.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkSessionTest {

    @Test
    fun `work session records start finish and duration`() {
        val session = WorkSession(
            id = "ws1",
            processId = "p1",
            taskId = "t1",
            startTimeEpochMillis = 10000L
        )

        assertTrue(session.isActive)
        assertEquals(0L, session.durationMillis)

        val finishedSession = session.finishSession(endTime = 40000L, note = "Sesion de trabajo productiva")

        assertFalse(finishedSession.isActive)
        assertEquals(30000L, finishedSession.durationMillis)
        assertEquals("Sesion de trabajo productiva", finishedSession.note)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `work session throws exception if end time is before start time`() {
        WorkSession(
            id = "ws2",
            processId = "p1",
            startTimeEpochMillis = 20000L,
            endTimeEpochMillis = 10000L
        )
    }
}
