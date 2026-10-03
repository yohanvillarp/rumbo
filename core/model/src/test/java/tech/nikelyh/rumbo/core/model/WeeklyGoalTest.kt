package tech.nikelyh.rumbo.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyGoalTest {

    @Test
    fun `weekly goal belongs to process and updates status`() {
        val goal = WeeklyGoal(
            id = "wg1",
            processId = "p1",
            weekIdentifier = "2026-W10",
            description = "Completar modulo core:model"
        )

        assertEquals(GoalStatus.PENDING, goal.status)

        val achieved = goal.markAchieved()
        assertEquals(GoalStatus.ACHIEVED, achieved.status)

        val cancelled = goal.markCancelled()
        assertEquals(GoalStatus.CANCELLED, cancelled.status)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank description throws exception`() {
        WeeklyGoal(
            id = "wg2",
            processId = "p1",
            weekIdentifier = "2026-W10",
            description = ""
        )
    }
}
