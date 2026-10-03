package tech.nikelyh.rumbo.feature.progress

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.nikelyh.rumbo.core.model.ProgressLevel
import tech.nikelyh.rumbo.feature.progress.fakes.FakeProcessRepository
import tech.nikelyh.rumbo.feature.progress.fakes.FakeProgressRepository
import tech.nikelyh.rumbo.feature.progress.fakes.FakeTaskRepository
import tech.nikelyh.rumbo.feature.progress.fakes.FakeWorkSessionRepository

class StartSessionViewModelTest {

    private lateinit var workSessionRepository: FakeWorkSessionRepository
    private lateinit var progressRepository: FakeProgressRepository
    private lateinit var processRepository: FakeProcessRepository
    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var viewModel: StartSessionViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        workSessionRepository = FakeWorkSessionRepository()
        progressRepository = FakeProgressRepository()
        processRepository = FakeProcessRepository()
        taskRepository = FakeTaskRepository()

        val savedStateHandle = SavedStateHandle(mapOf("processId" to "p1"))
        viewModel = StartSessionViewModel(
            savedStateHandle,
            workSessionRepository,
            progressRepository,
            processRepository,
            taskRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `finishing and submitting work session saves session with duration and note`() = runBlocking {
        viewModel.onEvent(StartSessionUiEvent.FinishTimer)
        viewModel.onEvent(StartSessionUiEvent.NoteChanged("Concentración profunda en arquitectura"))
        viewModel.onEvent(StartSessionUiEvent.SubmitSession)

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)

        val savedSessions = workSessionRepository.getWorkSessionsByProcessId("p1").first()
        assertEquals(1, savedSessions.size)
        val session = savedSessions.first()
        assertEquals("p1", session.processId)
        assertEquals("Concentración profunda en arquitectura", session.note)
    }

    @Test
    fun `submitting session with progress toggle creates explicit progress entry`() = runBlocking {
        viewModel.onEvent(StartSessionUiEvent.FinishTimer)
        viewModel.onEvent(StartSessionUiEvent.NoteChanged("Buena sesión"))
        viewModel.onEvent(StartSessionUiEvent.ToggleSaveProgress(true))
        viewModel.onEvent(StartSessionUiEvent.ProgressLevelSelected(ProgressLevel.HIGH))
        viewModel.onEvent(StartSessionUiEvent.SubmitSession)

        val savedEntries = progressRepository.getProgressEntriesByProcessId("p1").first()
        assertEquals(1, savedEntries.size)
        val entry = savedEntries.first()
        assertEquals(100, entry.progressLevel)
        assertEquals("Buena sesión", entry.note)
    }
}
