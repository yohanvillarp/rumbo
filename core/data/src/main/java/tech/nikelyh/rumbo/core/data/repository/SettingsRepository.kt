package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import tech.nikelyh.rumbo.core.model.UserSettings

interface SettingsRepository {
    val userSettings: Flow<UserSettings>
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun setNotifications(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
}
