package tech.nikelyh.rumbo.feature.processes

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProcessStatus
import tech.nikelyh.rumbo.feature.processes.fakes.FakeMilestoneRepository
import tech.nikelyh.rumbo.feature.processes.fakes.FakeProcessRepository
import tech.nikelyh.rumbo.feature.processes.fakes.FakeProgressRepository
import tech.nikelyh.rumbo.feature.processes.fakes.FakeTaskRepository
import tech.nikelyh.rumbo.feature.processes.fakes.FakeWeeklyGoalRepository
import tech.nikelyh.rumbo.feature.processes.fakes.FakeWorkSessionRepository

@OptIn(ExperimentalCoroutinesApi::class)
class ProcessDetailViewModelTest {

    private lateinit var processRepository: FakeProcessRepository
    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var milestoneRepository: FakeMilestoneRepository
    private lateinit var workSessionRepository: FakeWorkSessionRepository
    private lateinit var progressRepository: FakeProgressRepository
    private lateinit var weeklyGoalRepository: FakeWeeklyGoalRepository
    private lateinit var viewModel: ProcessDetailViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    private val testProcess = Process(
        id = "p100",
        name = "Proceso Test",
        status = ProcessStatus.ACTIVE,
        createdAtEpochMillis = 1000L,
        colorOrVisualId = "teal"
    )

    @Before
    fun setUp() = runBlocking {
        Dispatchers.setMain(testDispatcher)
        processRepository = FakeProcessRepository()
        taskRepository = FakeTaskRepository()
        milestoneRepository = FakeMilestoneRepository()
        workSessionRepository = FakeWorkSessionRepository()
        progressRepository = FakeProgressRepository()
        weeklyGoalRepository = FakeWeeklyGoalRepository()

        processRepository.saveProcess(testProcess)

        val savedStateHandle = SavedStateHandle(mapOf("processId" to "p100"))
        viewModel = ProcessDetailViewModel(
            savedStateHandle,
            processRepository,
            taskRepository,
            milestoneRepository,
            workSessionRepository,
            progressRepository,
            weeklyGoalRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads process detail content correctly`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }

        val state = viewModel.uiState.first { it is ProcessDetailUiState.Content }
        assertTrue(state is ProcessDetailUiState.Content)

        val content = state as ProcessDetailUiState.Content
        assertEquals("p100", content.process.id)
        assertEquals("Proceso Test", content.process.name)

        collectJob.cancel()
    }

    @Test
    fun `pausing process updates status in repository`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }
        viewModel.uiState.first { it is ProcessDetailUiState.Content }

        viewModel.onEvent(ProcessDetailUiEvent.PauseProcess)

        val updatedProcess = processRepository.getProcessById("p100").first()
        assertNotNull(updatedProcess)
        assertEquals(ProcessStatus.PAUSED, updatedProcess?.status)

        collectJob.cancel()
    }

    @Test
    fun `finishing process on explicit user action updates status to COMPLETED`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }
        viewModel.uiState.first { it is ProcessDetailUiState.Content }

        viewModel.onEvent(ProcessDetailUiEvent.FinishProcess)

        val updatedProcess = processRepository.getProcessById("p100").first()
        assertNotNull(updatedProcess)
        assertEquals(ProcessStatus.COMPLETED, updatedProcess?.status)
        assertNotNull(updatedProcess?.finishedAtEpochMillis)

        collectJob.cancel()
    }
}
