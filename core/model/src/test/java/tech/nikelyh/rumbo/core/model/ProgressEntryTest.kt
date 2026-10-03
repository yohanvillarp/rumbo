package tech.nikelyh.rumbo.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressEntryTest {

    @Test
    fun `manual progress entry records evaluation independently of task percentage`() {
        val entry = ProgressEntry(
            id = "pe1",
            processId = "p1",
            dateEpochMillis = 50000L,
            progressLevel = 75,
            note = "Evaluación manual de la semana"
        )

        assertEquals("p1", entry.processId)
        assertEquals(75, entry.progressLevel)
        assertEquals("Evaluación manual de la semana", entry.note)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `progress entry level out of bounds throws exception`() {
        ProgressEntry(
            id = "pe2",
            processId = "p1",
            dateEpochMillis = 50000L,
            progressLevel = 150
        )
    }
}
