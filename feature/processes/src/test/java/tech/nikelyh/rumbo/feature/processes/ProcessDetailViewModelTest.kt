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

        viewModel.onEvent(ProcessDetailUiEvent.SaveWeeklyGoal("Completar las tareas clave de la semana"))

        val savedGoals = weeklyGoalRepository.getWeeklyGoalsByProcessId("p100").first()
        assertEquals(1, savedGoals.size)
        val goal = savedGoals.first()
        assertEquals("Completar las tareas clave de la semana", goal.description)

        collectJob.cancel()
    }

    @Test
    fun `carrying over weekly goal creates goal for next week`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }
        viewModel.uiState.first { it is ProcessDetailUiState.Content }

        viewModel.onEvent(ProcessDetailUiEvent.SaveWeeklyGoal("Completar las tareas clave de la semana"))
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
    fun `finishing process is blocked when process has no tasks and no subprocesses`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }
        viewModel.uiState.first { it is ProcessDetailUiState.Content }

        viewModel.onEvent(ProcessDetailUiEvent.FinishProcess)

        val process = processRepository.getProcessById("p100").first()
        assertEquals(ProcessStatus.ACTIVE, process?.status)

        val state = viewModel.uiState.value as ProcessDetailUiState.Content
        assertNotNull(state.userMessage)
        assertTrue(state.userMessage!!.contains("sin tareas ni subprocesos"))

        collectJob.cancel()
    }

    @Test
    fun `finishing process on explicit user action updates status to COMPLETED when it has completed tasks`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }
        viewModel.uiState.first { it is ProcessDetailUiState.Content }

        taskRepository.saveTask(
            Task(
                id = "t-comp",
                processId = "p100",
                title = "Tarea culminada",
                status = TaskStatus.COMPLETED,
                priority = Priority.MEDIUM,
                createdAtEpochMillis = 1000L,
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

        viewModel.onEvent(ProcessDetailUiEvent.ReopenProcess)
        val reopenedProcess = processRepository.getProcessById("p100").first()
        assertNotNull(reopenedProcess)
        assertEquals(ProcessStatus.ACTIVE, reopenedProcess?.status)
        assertNull(reopenedProcess?.finishedAtEpochMillis)

        collectJob.cancel()
    }

    @Test
    fun `sorting pending tasks by due date, recent, and priority orders correctly`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }
        viewModel.uiState.first { it is ProcessDetailUiState.Content }

        val t1 = Task(id = "t1", processId = "p100", title = "T1", status = TaskStatus.PENDING, priority = Priority.LOW, createdAtEpochMillis = 1000L, dueDateEpochMillis = 5000L)
        val t2 = Task(id = "t2", processId = "p100", title = "T2", status = TaskStatus.PENDING, priority = Priority.HIGH, createdAtEpochMillis = 2000L, dueDateEpochMillis = 3000L)
        val t3 = Task(id = "t3", processId = "p100", title = "T3", status = TaskStatus.PENDING, priority = Priority.MEDIUM, createdAtEpochMillis = 3000L, dueDateEpochMillis = null)

        taskRepository.saveTask(t1)
        taskRepository.saveTask(t2)
        taskRepository.saveTask(t3)

        // Default: DUE_DATE (t2 [3000L] -> t1 [5000L] -> t3 [null])
        var state = viewModel.uiState.value as ProcessDetailUiState.Content
        assertEquals(listOf("t2", "t1", "t3"), state.pendingTasks.map { it.id })

        // Sort by RECENT (t3 [3000L] -> t2 [2000L] -> t1 [1000L])
        viewModel.onEvent(ProcessDetailUiEvent.ChangeTaskSortOrder(tech.nikelyh.rumbo.core.model.TaskSortOrder.RECENT))
        state = viewModel.uiState.value as ProcessDetailUiState.Content
        assertEquals(listOf("t3", "t2", "t1"), state.pendingTasks.map { it.id })

        // Sort by PRIORITY (t2 [HIGH] -> t3 [MEDIUM] -> t1 [LOW])
        viewModel.onEvent(ProcessDetailUiEvent.ChangeTaskSortOrder(tech.nikelyh.rumbo.core.model.TaskSortOrder.PRIORITY))
        state = viewModel.uiState.value as ProcessDetailUiState.Content
        assertEquals(listOf("t2", "t3", "t1"), state.pendingTasks.map { it.id })

        collectJob.cancel()
    }

    @Test
    fun `toggling star toggles starred status and warns on 4th star`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }
        viewModel.uiState.first { it is ProcessDetailUiState.Content }

        // Star the testProcess (first starred process)
        viewModel.onEvent(ProcessDetailUiEvent.ToggleStar)
        val pStarred = processRepository.getProcessById("p100").first()
        assertTrue(pStarred?.isStarred == true)

        // Now add 2 more starred processes to reach 3 total
        processRepository.saveProcess(Process("p2", "P2", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 2000L, colorOrVisualId = "blue", isStarred = true))
        processRepository.saveProcess(Process("p3", "P3", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 3000L, colorOrVisualId = "blue", isStarred = true))

        // Create a 4th unstarred process and set as target
        processRepository.saveProcess(Process("p4", "P4", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 4000L, colorOrVisualId = "blue", isStarred = false))
        val vm4 = ProcessDetailViewModel(
            SavedStateHandle(mapOf("processId" to "p4")),
            processRepository,
            taskRepository,
            milestoneRepository,
            weeklyGoalRepository,
            workSessionRepository,
            progressRepository
        )
        val vm4Job = launch(testDispatcher) { vm4.uiState.collect {} }
        vm4.uiState.first { it is ProcessDetailUiState.Content }

        vm4.onEvent(ProcessDetailUiEvent.ToggleStar)
        val state4 = vm4.uiState.first { (it as? ProcessDetailUiState.Content)?.userMessage != null }
        val content4 = state4 as ProcessDetailUiState.Content
        assertEquals("Solo es posible destacar hasta 3 procesos", content4.userMessage)

        vm4Job.cancel()
        collectJob.cancel()
    }
}

