package tech.nikelyh.rumbo.core.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.model.UserSettings
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
class RumboPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val userSettings: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            isDarkModeEnabled = preferences[PreferencesKeys.DARK_MODE] ?: false,
            isNotificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS] ?: true,
            hasCompletedOnboarding = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS] = enabled
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }
}
