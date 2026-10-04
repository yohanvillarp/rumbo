package tech.nikelyh.rumbo.core.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.nikelyh.rumbo.core.model.ActiveSessionState
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
        val ACTIVE_SESSION_PROCESS_ID = stringPreferencesKey("active_session_process_id")
        val ACTIVE_SESSION_TASK_ID = stringPreferencesKey("active_session_task_id")
        val ACTIVE_SESSION_START_TIME = longPreferencesKey("active_session_start_time")
        val ACTIVE_SESSION_LAST_RESUME = longPreferencesKey("active_session_last_resume")
        val ACTIVE_SESSION_ACCUMULATED_TIME = longPreferencesKey("active_session_accumulated_time")
        val ACTIVE_SESSION_IS_RUNNING = booleanPreferencesKey("active_session_is_running")
    }

    val activeSessionState: Flow<ActiveSessionState> = context.dataStore.data.map { preferences ->
        ActiveSessionState(
            processId = preferences[PreferencesKeys.ACTIVE_SESSION_PROCESS_ID],
            taskId = preferences[PreferencesKeys.ACTIVE_SESSION_TASK_ID],
            startTimeEpochMillis = preferences[PreferencesKeys.ACTIVE_SESSION_START_TIME] ?: 0L,
            lastResumeEpochMillis = preferences[PreferencesKeys.ACTIVE_SESSION_LAST_RESUME] ?: 0L,
            accumulatedTimeMillis = preferences[PreferencesKeys.ACTIVE_SESSION_ACCUMULATED_TIME] ?: 0L,
            isRunning = preferences[PreferencesKeys.ACTIVE_SESSION_IS_RUNNING] ?: false
        )
    }

    suspend fun saveActiveSessionState(state: ActiveSessionState) {
        val processId = state.processId
        val taskId = state.taskId
        context.dataStore.edit { preferences ->
            if (processId != null) {
                preferences[PreferencesKeys.ACTIVE_SESSION_PROCESS_ID] = processId
            } else {
                preferences.remove(PreferencesKeys.ACTIVE_SESSION_PROCESS_ID)
            }
            if (taskId != null) {
                preferences[PreferencesKeys.ACTIVE_SESSION_TASK_ID] = taskId
            } else {
                preferences.remove(PreferencesKeys.ACTIVE_SESSION_TASK_ID)
            }
            preferences[PreferencesKeys.ACTIVE_SESSION_START_TIME] = state.startTimeEpochMillis
            preferences[PreferencesKeys.ACTIVE_SESSION_LAST_RESUME] = state.lastResumeEpochMillis
            preferences[PreferencesKeys.ACTIVE_SESSION_ACCUMULATED_TIME] = state.accumulatedTimeMillis
            preferences[PreferencesKeys.ACTIVE_SESSION_IS_RUNNING] = state.isRunning
        }
    }

    suspend fun clearActiveSessionState() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.ACTIVE_SESSION_PROCESS_ID)
            preferences.remove(PreferencesKeys.ACTIVE_SESSION_TASK_ID)
            preferences.remove(PreferencesKeys.ACTIVE_SESSION_START_TIME)
            preferences.remove(PreferencesKeys.ACTIVE_SESSION_LAST_RESUME)
            preferences.remove(PreferencesKeys.ACTIVE_SESSION_ACCUMULATED_TIME)
            preferences.remove(PreferencesKeys.ACTIVE_SESSION_IS_RUNNING)
        }
    }

    val userSettings: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            isDarkModeEnabled = preferences[PreferencesKeys.DARK_MODE],
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
