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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.nikelyh.rumbo.core.model.Priority
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProcessStatus
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus
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
            weeklyGoalRepository,
            workSessionRepository,
            progressRepository
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
    fun `saving weekly goal creates or updates goal for current week`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }
        viewModel.uiState.first { it is ProcessDetailUiState.Content }

        viewModel.onEvent(ProcessDetailUiEvent.SaveWeeklyGoal("Terminar dashboard OLAP"))

        val savedGoals = weeklyGoalRepository.getWeeklyGoalsByProcessId("p100").first()
        assertEquals(1, savedGoals.size)
        val goal = savedGoals.first()
        assertEquals("Terminar dashboard OLAP", goal.description)

        collectJob.cancel()
    }

    @Test
    fun `carrying over weekly goal creates goal for next week`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }
        viewModel.uiState.first { it is ProcessDetailUiState.Content }

        viewModel.onEvent(ProcessDetailUiEvent.SaveWeeklyGoal("Terminar dashboard OLAP"))
        val firstGoal = weeklyGoalRepository.getWeeklyGoalsByProcessId("p100").first().first()

        viewModel.onEvent(ProcessDetailUiEvent.CarryOverWeeklyGoal(firstGoal.id))

        val allGoals = weeklyGoalRepository.getWeeklyGoalsByProcessId("p100").first()
        assertEquals(2, allGoals.size)

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

    @Test
    fun `finishing process is blocked when pending tasks exist`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }
        viewModel.uiState.first { it is ProcessDetailUiState.Content }

        taskRepository.saveTask(
            Task(
                id = "t1",
                processId = "p100",
                title = "Tarea pendiente",
                status = TaskStatus.PENDING,
                priority = Priority.MEDIUM,
                createdAtEpochMillis = 1000L
            )
        )

        viewModel.onEvent(ProcessDetailUiEvent.FinishProcess)

        val process = processRepository.getProcessById("p100").first()
        assertEquals(ProcessStatus.ACTIVE, process?.status)

        val state = viewModel.uiState.value as ProcessDetailUiState.Content
        assertNotNull(state.userMessage)
        assertTrue(state.userMessage!!.contains("tareas pendientes"))

        collectJob.cancel()
    }

    @Test
    fun `finishing process is blocked when active subprocesses exist`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }
        viewModel.uiState.first { it is ProcessDetailUiState.Content }

        processRepository.saveProcess(
            Process(
                id = "sub-1",
                name = "Subproceso activo",
                parentProcessId = "p100",
                status = ProcessStatus.ACTIVE,
                createdAtEpochMillis = 1000L,
                colorOrVisualId = "blue"
            )
        )

        viewModel.onEvent(ProcessDetailUiEvent.FinishProcess)

        val process = processRepository.getProcessById("p100").first()
        assertEquals(ProcessStatus.ACTIVE, process?.status)

        val state = viewModel.uiState.value as ProcessDetailUiState.Content
        assertNotNull(state.userMessage)
        assertTrue(state.userMessage!!.contains("subprocesos activos"))

        collectJob.cancel()
    }

    @Test
    fun `finishing process succeeds when all tasks and subprocesses are completed`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }
        viewModel.uiState.first { it is ProcessDetailUiState.Content }

        taskRepository.saveTask(
            Task(
                id = "t1",
                processId = "p100",
                title = "Tarea completada",
                status = TaskStatus.COMPLETED,
                priority = Priority.MEDIUM,
                createdAtEpochMillis = 1000L,
                finishedAtEpochMillis = 2000L
            )
        )
        processRepository.saveProcess(
            Process(
                id = "sub-1",
                name = "Subproceso completado",
                parentProcessId = "p100",
                status = ProcessStatus.COMPLETED,
                createdAtEpochMillis = 1000L,
                colorOrVisualId = "blue",
                finishedAtEpochMillis = 2000L
            )
        )

        viewModel.onEvent(ProcessDetailUiEvent.FinishProcess)

        val updatedProcess = processRepository.getProcessById("p100").first()
        assertNotNull(updatedProcess)
        assertEquals(ProcessStatus.COMPLETED, updatedProcess?.status)
        assertNotNull(updatedProcess?.finishedAtEpochMillis)

        collectJob.cancel()
    }
}
