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

class LogProgressViewModelTest {

    private lateinit var progressRepository: FakeProgressRepository
    private lateinit var processRepository: FakeProcessRepository
    private lateinit var viewModel: LogProgressViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        progressRepository = FakeProgressRepository()
        processRepository = FakeProcessRepository()

        val savedStateHandle = SavedStateHandle(mapOf("processId" to "p2"))
        viewModel = LogProgressViewModel(savedStateHandle, progressRepository, processRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `logging progress manually creates entry independently without session`() = runBlocking {
        viewModel.onEvent(LogProgressUiEvent.LevelSelected(ProgressLevel.HIGH))
        viewModel.onEvent(LogProgressUiEvent.NoteChanged("Completado hito clave"))
        viewModel.onEvent(LogProgressUiEvent.SubmitProgress)

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)

        val savedEntries = progressRepository.getProgressEntriesByProcessId("p2").first()
        assertEquals(1, savedEntries.size)
        val entry = savedEntries.first()
        assertEquals("p2", entry.processId)
        assertEquals(100, entry.progressLevel)
        assertEquals("Completado hito clave", entry.note)
    }
}
