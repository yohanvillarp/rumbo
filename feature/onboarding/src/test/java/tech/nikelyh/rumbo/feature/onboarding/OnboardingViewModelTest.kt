package tech.nikelyh.rumbo.feature.onboarding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.nikelyh.rumbo.feature.onboarding.fakes.FakeSettingsRepository

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var viewModel: OnboardingViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = FakeSettingsRepository()
        viewModel = OnboardingViewModel(settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is empty name with no errors`() {
        val state = viewModel.uiState.value
        assertEquals("", state.name)
        assertNull(state.nameError)
        assertFalse(state.isCompleted)
    }

    @Test
    fun `submit blank name sets validation error`() {
        viewModel.onEvent(OnboardingUiEvent.NameChanged("   "))
        viewModel.onEvent(OnboardingUiEvent.SubmitName)

        val state = viewModel.uiState.value
        assertEquals("El nombre es obligatorio", state.nameError)
        assertFalse(state.isCompleted)
    }

    @Test
    fun `submit too long name sets validation error`() {
        val longName = "a".repeat(51)
        viewModel.onEvent(OnboardingUiEvent.NameChanged(longName))
        viewModel.onEvent(OnboardingUiEvent.SubmitName)

        val state = viewModel.uiState.value
        assertEquals("El nombre no puede superar los 50 caracteres", state.nameError)
        assertFalse(state.isCompleted)
    }

    @Test
    fun `valid name submission trims whitespace saves settings and completes onboarding`() = runBlocking {
        viewModel.onEvent(OnboardingUiEvent.NameChanged("   Yohan   "))
        viewModel.onEvent(OnboardingUiEvent.SubmitName)

        val state = viewModel.uiState.value
        assertNull(state.nameError)
        assertTrue(state.isCompleted)

        val savedProfile = settingsRepository.userProfile.first()
        assertNotNull(savedProfile)
        assertEquals("Yohan", savedProfile?.name)

        val savedSettings = settingsRepository.userSettings.first()
        assertTrue(savedSettings.hasCompletedOnboarding)
    }

    @Test
    fun `change language updates selected language and repository`() = runBlocking {
        viewModel.onEvent(OnboardingUiEvent.ChangeLanguage(tech.nikelyh.rumbo.core.model.AppLanguage.ENGLISH))

        val state = viewModel.uiState.value
        assertEquals("en", state.selectedLanguageCode)

        val savedSettings = settingsRepository.userSettings.first()
        assertEquals("en", savedSettings.languageCode)

        viewModel.onEvent(OnboardingUiEvent.ChangeLanguage(tech.nikelyh.rumbo.core.model.AppLanguage.SYSTEM))
        assertEquals(null, viewModel.uiState.value.selectedLanguageCode)
        assertEquals(null, settingsRepository.userSettings.first().languageCode)
    }

    @Test
    fun `change language to portuguese updates selected language and repository to pt`() = runBlocking {
        viewModel.onEvent(OnboardingUiEvent.ChangeLanguage(tech.nikelyh.rumbo.core.model.AppLanguage.PORTUGUESE))

        val state = viewModel.uiState.value
        assertEquals("pt", state.selectedLanguageCode)

        val savedSettings = settingsRepository.userSettings.first()
        assertEquals("pt", savedSettings.languageCode)
    }
}
