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
import tech.nikelyh.rumbo.core.model.ProcessSortOrder
import tech.nikelyh.rumbo.core.model.ProcessStatus
import tech.nikelyh.rumbo.core.model.ProcessTypeFilter
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
    fun `filtering by status separates active and completed processes`() = runBlocking {
        val pActive = Process(id = "p1", name = "Activo", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 1000L, colorOrVisualId = "teal")
        val pCompleted = Process(id = "p3", name = "Completado", status = ProcessStatus.COMPLETED, createdAtEpochMillis = 3000L, colorOrVisualId = "green", finishedAtEpochMillis = 4000L)

        processRepository.saveProcess(pActive)
        processRepository.saveProcess(pCompleted)

        val state = viewModel.uiState.first()
        assertTrue(state is ProcessesUiState.Content)

        val content = state as ProcessesUiState.Content
        assertEquals(1, content.activeProcesses.size)
        assertEquals(1, content.completedProcesses.size)
        assertEquals("p1", content.activeProcesses.first().id)
        assertEquals("p3", content.completedProcesses.first().id)
    }

    @Test
    fun `filtering by type separates main and subprocesses`() = runBlocking {
        val mainProcess = Process(id = "p_main", name = "Principal", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 1000L, colorOrVisualId = "teal")
        val subProcess = Process(id = "p_sub", name = "Subproceso", parentProcessId = "p_main", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 2000L, colorOrVisualId = "blue")

        processRepository.saveProcess(mainProcess)
        processRepository.saveProcess(subProcess)

        // Filter MAIN only
        viewModel.onEvent(ProcessesUiEvent.TypeFilterChanged(ProcessTypeFilter.MAIN))
        val mainState = viewModel.uiState.first { (it as? ProcessesUiState.Content)?.selectedTypeFilter == ProcessTypeFilter.MAIN }
        val mainContent = mainState as ProcessesUiState.Content
        assertEquals(1, mainContent.activeProcesses.size)
        assertEquals("p_main", mainContent.activeProcesses.first().id)

        // Filter SUBPROCESS only
        viewModel.onEvent(ProcessesUiEvent.TypeFilterChanged(ProcessTypeFilter.SUBPROCESS))
        val subState = viewModel.uiState.first { (it as? ProcessesUiState.Content)?.selectedTypeFilter == ProcessTypeFilter.SUBPROCESS }
        val subContent = subState as ProcessesUiState.Content
        assertEquals(1, subContent.activeProcesses.size)
        assertEquals("p_sub", subContent.activeProcesses.first().id)
    }

    @Test
    fun `sorting processes by name and accumulated cost`() = runBlocking {
        val pBeta = Process(id = "p_beta", name = "Beta", status = ProcessStatus.ACTIVE, accumulatedDirectCost = 100.0, createdAtEpochMillis = 1000L, colorOrVisualId = "teal")
        val pAlpha = Process(id = "p_alpha", name = "Alpha", status = ProcessStatus.ACTIVE, accumulatedDirectCost = 500.0, createdAtEpochMillis = 2000L, colorOrVisualId = "blue")

        processRepository.saveProcess(pBeta)
        processRepository.saveProcess(pAlpha)

        // Sort by NAME (A-Z)
        viewModel.onEvent(ProcessesUiEvent.SortOrderChanged(ProcessSortOrder.NAME))
        val nameState = viewModel.uiState.first { (it as? ProcessesUiState.Content)?.sortOrder == ProcessSortOrder.NAME }
        val nameProcesses = (nameState as ProcessesUiState.Content).activeProcesses
        assertEquals("p_alpha", nameProcesses[0].id)
        assertEquals("p_beta", nameProcesses[1].id)

        // Sort by ACCUMULATED_COST descending
        viewModel.onEvent(ProcessesUiEvent.SortOrderChanged(ProcessSortOrder.ACCUMULATED_COST))
        val costState = viewModel.uiState.first { (it as? ProcessesUiState.Content)?.sortOrder == ProcessSortOrder.ACCUMULATED_COST }
        val costProcesses = (costState as ProcessesUiState.Content).activeProcesses
        assertEquals("p_alpha", costProcesses[0].id)
        assertEquals("p_beta", costProcesses[1].id)
    }

    @Test
    fun `toggling star toggles starred status and prioritizes starred in active processes`() = runBlocking {
        val p1 = Process(id = "p1", name = "P1", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 1000L, colorOrVisualId = "teal")
        val p2 = Process(id = "p2", name = "P2", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 2000L, colorOrVisualId = "blue")
        processRepository.saveProcess(p1)
        processRepository.saveProcess(p2)

        viewModel.onEvent(ProcessesUiEvent.ToggleStar("p1"))
        val state = viewModel.uiState.first { (it as? ProcessesUiState.Content)?.activeProcesses?.any { p -> p.id == "p1" && p.isStarred } == true }
        val content = state as ProcessesUiState.Content
        assertEquals("p1", content.activeProcesses.first().id)
        assertTrue(content.activeProcesses.first().isStarred)
    }

    @Test
    fun `toggling star warns user when attempting to star 4 processes`() = runBlocking {
        val p1 = Process(id = "p1", name = "P1", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 1000L, colorOrVisualId = "teal", isStarred = true)
        val p2 = Process(id = "p2", name = "P2", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 2000L, colorOrVisualId = "teal", isStarred = true)
        val p3 = Process(id = "p3", name = "P3", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 3000L, colorOrVisualId = "teal", isStarred = true)
        val p4 = Process(id = "p4", name = "P4", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 4000L, colorOrVisualId = "teal", isStarred = false)
        processRepository.saveProcess(p1)
        processRepository.saveProcess(p2)
        processRepository.saveProcess(p3)
        processRepository.saveProcess(p4)

        viewModel.onEvent(ProcessesUiEvent.ToggleStar("p4"))
        val state = viewModel.uiState.first { (it as? ProcessesUiState.Content)?.userMessage != null }
        val content = state as ProcessesUiState.Content
        assertEquals("Solo es posible destacar hasta 3 procesos", content.userMessage)

        viewModel.onEvent(ProcessesUiEvent.DismissUserMessage)
        val stateAfterDismiss = viewModel.uiState.first { (it as? ProcessesUiState.Content)?.userMessage == null }
        org.junit.Assert.assertNull((stateAfterDismiss as ProcessesUiState.Content).userMessage)
    }
}

