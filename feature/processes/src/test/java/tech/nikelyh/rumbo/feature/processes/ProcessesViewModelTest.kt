package tech.nikelyh.rumbo.feature.processes

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
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProcessStatus
import tech.nikelyh.rumbo.feature.processes.fakes.FakeProcessRepository

class ProcessesViewModelTest {

    private lateinit var processRepository: FakeProcessRepository
    private lateinit var viewModel: ProcessesViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        processRepository = FakeProcessRepository()
        viewModel = ProcessesViewModel(processRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is Empty when repository has no processes`() = runBlocking {
        val state = viewModel.uiState.first()
        assertTrue(state is ProcessesUiState.Empty)
    }

    @Test
    fun `includes General system process in active processes list on processes screen`() = runBlocking {
        processRepository.ensureGeneralProcessExists()
        val p1 = Process(id = "p1", name = "Aprender Kotlin", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 1000L, colorOrVisualId = "teal")
        processRepository.saveProcess(p1)

        val state = viewModel.uiState.first()
        assertTrue(state is ProcessesUiState.Content)

        val content = state as ProcessesUiState.Content
        assertEquals(2, content.activeProcesses.size)
        assertTrue(content.activeProcesses.any { it.id == Process.GENERAL_PROCESS_ID })
        assertTrue(content.activeProcesses.any { it.id == "p1" })
    }

    @Test
    fun `filtering by status separates active, paused, and completed processes`() = runBlocking {
        val pActive = Process(id = "p1", name = "Activo", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 1000L, colorOrVisualId = "teal")
        val pPaused = Process(id = "p2", name = "Pausado", status = ProcessStatus.PAUSED, createdAtEpochMillis = 2000L, colorOrVisualId = "blue")
        val pCompleted = Process(id = "p3", name = "Completado", status = ProcessStatus.COMPLETED, createdAtEpochMillis = 3000L, colorOrVisualId = "green", finishedAtEpochMillis = 4000L)

        processRepository.saveProcess(pActive)
        processRepository.saveProcess(pPaused)
        processRepository.saveProcess(pCompleted)

        val state = viewModel.uiState.first()
        assertTrue(state is ProcessesUiState.Content)

        val content = state as ProcessesUiState.Content
        assertEquals(1, content.activeProcesses.size)
        assertEquals(1, content.pausedProcesses.size)
        assertEquals(1, content.completedProcesses.size)
        assertEquals("p1", content.activeProcesses.first().id)
        assertEquals("p2", content.pausedProcesses.first().id)
        assertEquals("p3", content.completedProcesses.first().id)
    }
}
