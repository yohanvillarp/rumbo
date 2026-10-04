package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import tech.nikelyh.rumbo.core.common.Dispatcher
import tech.nikelyh.rumbo.core.common.RumboDispatchers
import tech.nikelyh.rumbo.core.data.datasource.RumboPreferencesDataSource
import tech.nikelyh.rumbo.core.database.RumboDatabase
import tech.nikelyh.rumbo.core.model.UserProfile
import tech.nikelyh.rumbo.core.model.UserSettings
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val preferencesDataSource: RumboPreferencesDataSource,
    private val rumboDatabase: RumboDatabase,
    private val processRepository: ProcessRepository,
    @param:Dispatcher(RumboDispatchers.IO) private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
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

    override suspend fun resetApplicationData() = withContext(ioDispatcher) {
        rumboDatabase.clearAllTables()
        preferencesDataSource.clearAllData()
        processRepository.ensureGeneralProcessExists()
    }
}
