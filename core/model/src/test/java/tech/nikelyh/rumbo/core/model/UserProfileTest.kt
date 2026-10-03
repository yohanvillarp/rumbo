package tech.nikelyh.rumbo.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UserProfileTest {

    @Test
    fun `user profile creation with valid name and date`() {
        val profile = UserProfile(
            id = "u1",
            name = "Yohan",
            createdAtEpochMillis = 100000L
        )

        assertEquals("Yohan", profile.name)
        assertEquals(100000L, profile.createdAtEpochMillis)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `user profile with blank name throws exception`() {
        UserProfile(
            id = "u2",
            name = "   ",
            createdAtEpochMillis = 100000L
        )
    }
}
