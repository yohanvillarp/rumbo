package tech.nikelyh.rumbo.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MilestoneTest {

    @Test
    fun `milestone belongs to process and can be completed and reopened`() {
        val milestone = Milestone(
            id = "m1",
            processId = "p1",
            title = "Arquitectura Inicial Completada"
        )

        assertEquals("p1", milestone.processId)
        assertFalse(milestone.isCompleted)

        val completed = milestone.complete(3000L)
        assertTrue(completed.isCompleted)
        assertEquals(3000L, completed.completedAtEpochMillis)

        val reopened = completed.reopen()
        assertFalse(reopened.isCompleted)
        assertEquals(null, reopened.completedAtEpochMillis)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `milestone with blank title throws exception`() {
        Milestone(
            id = "m2",
            processId = "p1",
            title = "   "
        )
    }
}
