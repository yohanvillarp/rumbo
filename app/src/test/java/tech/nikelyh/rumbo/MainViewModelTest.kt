package tech.nikelyh.rumbo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.nikelyh.rumbo.core.data.repository.SettingsRepository
import tech.nikelyh.rumbo.core.data.repository.WorkSessionRepository
import tech.nikelyh.rumbo.core.model.ActiveSessionState
import tech.nikelyh.rumbo.core.model.UserProfile
import tech.nikelyh.rumbo.core.model.UserSettings
import tech.nikelyh.rumbo.core.model.WorkSession

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var settingsRepository: TestSettingsRepository
    private lateinit var workSessionRepository: TestWorkSessionRepository
    private lateinit var viewModel: MainViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = TestSettingsRepository()
        workSessionRepository = TestWorkSessionRepository()
        viewModel = MainViewModel(settingsRepository, workSessionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits Success with onboarding status and null activeSession by default`() = runTest {
        settingsRepository.setOnboardingCompleted(true)
        val state = viewModel.uiState.first { it is MainUiState.Success } as MainUiState.Success

        assertTrue(state.hasCompletedOnboarding)
        assertNull(state.activeSession)
    }

    @Test
    fun `uiState includes activeSession when a task session is running`() = runTest {
        settingsRepository.setOnboardingCompleted(true)
        val runningSession = ActiveSessionState(
            processId = "proc-1",
            taskId = "task-100",
            startTimeEpochMillis = 10_000L,
            lastResumeEpochMillis = 10_000L,
            isRunning = true
        )
        workSessionRepository.saveActiveSessionState(runningSession)

        val state = viewModel.uiState.first {
            it is MainUiState.Success && it.activeSession != null
        } as MainUiState.Success

        assertNotNull(state.activeSession)
        assertEquals("task-100", state.activeSession?.taskId)
        assertEquals("proc-1", state.activeSession?.processId)
        assertTrue(state.activeSession?.isRunning == true)
    }

    @Test
    fun `uiState activeSession is null when session is not running`() = runTest {
        settingsRepository.setOnboardingCompleted(true)
        val pausedSession = ActiveSessionState(
            processId = "proc-1",
            taskId = "task-100",
            startTimeEpochMillis = 10_000L,
            lastResumeEpochMillis = 10_000L,
            isRunning = false
        )
        workSessionRepository.saveActiveSessionState(pausedSession)

        val state = viewModel.uiState.first { it is MainUiState.Success } as MainUiState.Success
        assertNull(state.activeSession)
    }

    private class TestSettingsRepository : SettingsRepository {
        private val settingsFlow = MutableStateFlow(UserSettings())
        private val profileFlow = MutableStateFlow<UserProfile?>(null)

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

        override suspend fun resetApplicationData() {
            settingsFlow.value = UserSettings()
            profileFlow.value = null
        }
    }

    private class TestWorkSessionRepository : WorkSessionRepository {
        private val flow = MutableStateFlow<Map<String, WorkSession>>(emptyMap())
        private val activeSessionFlow = MutableStateFlow(ActiveSessionState())
        override val activeSessionState: Flow<ActiveSessionState> = activeSessionFlow

        override fun getAllWorkSessions(): Flow<List<WorkSession>> = flow.map { it.values.toList() }
        override fun getWorkSessionsByProcessId(processId: String): Flow<List<WorkSession>> = flow.map { map -> map.values.filter { it.processId == processId } }
        override fun getWorkSessionsByTaskId(taskId: String): Flow<List<WorkSession>> = flow.map { map -> map.values.filter { it.taskId == taskId } }
        override fun getWorkSessionById(id: String): Flow<WorkSession?> = flow.map { it[id] }
        override suspend fun saveWorkSession(session: WorkSession) { flow.value = flow.value + (session.id to session) }
        override suspend fun deleteWorkSession(id: String): Boolean = flow.value.containsKey(id).also { if (it) flow.value = flow.value - id }
        override suspend fun saveActiveSessionState(state: ActiveSessionState) { activeSessionFlow.value = state }
        override suspend fun clearActiveSessionState() { activeSessionFlow.value = ActiveSessionState() }
    }
}
