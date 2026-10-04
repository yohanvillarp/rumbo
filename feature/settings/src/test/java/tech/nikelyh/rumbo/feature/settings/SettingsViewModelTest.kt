package tech.nikelyh.rumbo.feature.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.nikelyh.rumbo.feature.settings.fakes.FakeSettingsRepository

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = FakeSettingsRepository()
        viewModel = SettingsViewModel(settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState loads settings from repository`() = runTest {
        val state = viewModel.uiState.first { it is SettingsUiState.Success }
        assertTrue(state is SettingsUiState.Success)
        val success = state as SettingsUiState.Success
        assertFalse(success.isResetting)
    }

    @Test
    fun `toggle dark mode updates state`() = runTest {
        viewModel.onEvent(SettingsUiEvent.ToggleDarkMode(true))
        val state = viewModel.uiState.first { it is SettingsUiState.Success && (it as SettingsUiState.Success).userSettings.isDarkModeEnabled == true }
        val success = state as SettingsUiState.Success
        assertEquals(true, success.userSettings.isDarkModeEnabled)
    }

    @Test
    fun `toggle notifications updates state`() = runTest {
        viewModel.onEvent(SettingsUiEvent.ToggleNotifications(false))
        val state = viewModel.uiState.first { it is SettingsUiState.Success && !(it as SettingsUiState.Success).userSettings.isNotificationsEnabled }
        val success = state as SettingsUiState.Success
        assertFalse(success.userSettings.isNotificationsEnabled)
    }

    @Test
    fun `reset application data invokes repository reset and emits resetCompleted`() = runTest {
        var resetEventReceived = false
        val collectJob = launch(testDispatcher) {
            viewModel.resetCompleted.collect {
                resetEventReceived = true
            }
        }

        viewModel.onEvent(SettingsUiEvent.ResetApplicationData)

        assertTrue(settingsRepository.resetCalled)
        assertTrue(resetEventReceived)

        collectJob.cancel()
    }

    @Test
    fun `change language updates user settings language code`() = runTest {
        viewModel.onEvent(SettingsUiEvent.ChangeLanguage(tech.nikelyh.rumbo.core.model.AppLanguage.ENGLISH))
        val state = viewModel.uiState.first { 
            it is SettingsUiState.Success && (it as SettingsUiState.Success).userSettings.languageCode == "en" 
        } as SettingsUiState.Success

        assertEquals("en", state.userSettings.languageCode)

        viewModel.onEvent(SettingsUiEvent.ChangeLanguage(tech.nikelyh.rumbo.core.model.AppLanguage.SYSTEM))
        val stateSystem = viewModel.uiState.first { 
            it is SettingsUiState.Success && (it as SettingsUiState.Success).userSettings.languageCode == null 
        } as SettingsUiState.Success

        assertEquals(null, stateSystem.userSettings.languageCode)
    }

    @Test
    fun `change language to portuguese updates user settings language code to pt`() = runTest {
        viewModel.onEvent(SettingsUiEvent.ChangeLanguage(tech.nikelyh.rumbo.core.model.AppLanguage.PORTUGUESE))
        val state = viewModel.uiState.first { 
            it is SettingsUiState.Success && (it as SettingsUiState.Success).userSettings.languageCode == "pt" 
        } as SettingsUiState.Success

        assertEquals("pt", state.userSettings.languageCode)
    }
}
