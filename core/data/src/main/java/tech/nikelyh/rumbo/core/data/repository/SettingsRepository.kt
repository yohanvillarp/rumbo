package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import tech.nikelyh.rumbo.core.model.UserProfile
import tech.nikelyh.rumbo.core.model.UserSettings

interface SettingsRepository {
    val userSettings: Flow<UserSettings>
    val userProfile: Flow<UserProfile?>
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun setNotifications(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setUserName(name: String)
    suspend fun resetApplicationData()
}
