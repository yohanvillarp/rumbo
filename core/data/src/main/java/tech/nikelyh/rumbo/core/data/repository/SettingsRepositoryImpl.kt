package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.Flow
import tech.nikelyh.rumbo.core.data.datasource.RumboPreferencesDataSource
import tech.nikelyh.rumbo.core.model.UserProfile
import tech.nikelyh.rumbo.core.model.UserSettings
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val preferencesDataSource: RumboPreferencesDataSource
) : SettingsRepository {

    override val userSettings: Flow<UserSettings> = preferencesDataSource.userSettings

    override val userProfile: Flow<UserProfile?> = preferencesDataSource.userProfile

    override suspend fun setDarkMode(enabled: Boolean) {
        preferencesDataSource.setDarkMode(enabled)
    }

    override suspend fun setNotifications(enabled: Boolean) {
        preferencesDataSource.setNotifications(enabled)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        preferencesDataSource.setOnboardingCompleted(completed)
    }

    override suspend fun setUserName(name: String) {
        preferencesDataSource.setUserName(name)
    }
}
