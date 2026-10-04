package tech.nikelyh.rumbo.core.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.model.UserProfile
import tech.nikelyh.rumbo.core.model.UserSettings
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class RumboPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_CREATED_AT = stringPreferencesKey("user_created_at")
    }

    val userSettings: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            isDarkModeEnabled = preferences[PreferencesKeys.DARK_MODE] ?: false,
            isNotificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS] ?: true,
            hasCompletedOnboarding = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        )
    }

    val userProfile: Flow<UserProfile?> = context.dataStore.data.map { preferences ->
        val name = preferences[PreferencesKeys.USER_NAME]
        val createdAt = preferences[PreferencesKeys.USER_CREATED_AT]?.toLongOrNull() ?: System.currentTimeMillis()
        if (name != null) {
            UserProfile(id = "user_me", name = name, createdAtEpochMillis = createdAt)
        } else {
            null
        }
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

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
            if (preferences[PreferencesKeys.USER_CREATED_AT] == null) {
                preferences[PreferencesKeys.USER_CREATED_AT] = System.currentTimeMillis().toString()
            }
        }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
