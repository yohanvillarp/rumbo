package tech.nikelyh.rumbo.feature.settings.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import tech.nikelyh.rumbo.core.data.repository.SettingsRepository
import tech.nikelyh.rumbo.core.model.UserProfile
import tech.nikelyh.rumbo.core.model.UserSettings

class FakeSettingsRepository : SettingsRepository {
    private val settingsFlow = MutableStateFlow(UserSettings())
    private val profileFlow = MutableStateFlow<UserProfile?>(null)

    var resetCalled: Boolean = false

    override val userSettings: Flow<UserSettings> = settingsFlow
    override val userProfile: Flow<UserProfile?> = profileFlow

    override suspend fun setDarkMode(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(isDarkModeEnabled = enabled)
    }

    override suspend fun setNotifications(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(isNotificationsEnabled = enabled)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(hasCompletedOnboarding = completed)
    }

    override suspend fun setUserName(name: String) {
        profileFlow.value = UserProfile(id = "user_me", name = name, createdAtEpochMillis = 1000L)
    }

    override suspend fun setLanguage(languageCode: String?) {
        settingsFlow.value = settingsFlow.value.copy(languageCode = languageCode)
    }

    override suspend fun resetApplicationData() {
        resetCalled = true
        settingsFlow.value = UserSettings()
        profileFlow.value = null
    }
}
