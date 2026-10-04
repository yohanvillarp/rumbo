package tech.nikelyh.rumbo.feature.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus
import tech.nikelyh.rumbo.feature.home.fakes.FakeProcessRepository
import tech.nikelyh.rumbo.feature.home.fakes.FakeSettingsRepository
import tech.nikelyh.rumbo.feature.home.fakes.FakeTaskRepository
import tech.nikelyh.rumbo.feature.home.fakes.FakeWorkSessionRepository

class HomeViewModelTest {

    private lateinit var processRepository: FakeProcessRepository
    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var workSessionRepository: FakeWorkSessionRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        processRepository = FakeProcessRepository()
        taskRepository = FakeTaskRepository()
        workSessionRepository = FakeWorkSessionRepository()
        settingsRepository = FakeSettingsRepository()
        viewModel = HomeViewModel(processRepository, taskRepository, workSessionRepository, settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `greeting selects correct prefix for morning afternoon and night hours`() {
        assertEquals("Buenos días", viewModel.getGreetingForHour(8))
        assertEquals("Buenas tardes", viewModel.getGreetingForHour(14))
        assertEquals("Buenas noches", viewModel.getGreetingForHour(22))
    }

    @Test
    fun `uiState presents empty state when no processes or tasks exist`() = runBlocking {
        val state = viewModel.uiState.first()
        assertTrue(state is HomeUiState.Empty)
    }

    @Test
    fun `uiState ignores General system process and remains Empty when no user processes or tasks exist`() = runBlocking {
        processRepository.ensureGeneralProcessExists()
        val state = viewModel.uiState.first()
        assertTrue(state is HomeUiState.Empty)
    }

    @Test
    fun `uiState presents content with active processes max 3 and continue process selection`() = runBlocking {
        val p1 = Process(
            id = "p1",
            name = "Proceso 1",
            status = ProcessStatus.ACTIVE,
            createdAtEpochMillis = 1000L,
            colorOrVisualId = "blue",
            nextAction = "Siguiente paso 1"
        )
        val p2 = Process(
            id = "p2",
            name = "Proceso 2",
            status = ProcessStatus.ACTIVE,
            createdAtEpochMillis = 2000L,
            colorOrVisualId = "green"
        )

        processRepository.saveProcess(p1)
        processRepository.saveProcess(p2)

        val t1 = Task(
            id = "t1",
            processId = "p1",
            title = "Tarea Pendiente",
            status = TaskStatus.PENDING,
            createdAtEpochMillis = 1000L
        )
        taskRepository.saveTask(t1)

        val state = viewModel.uiState.first()
        assertTrue(state is HomeUiState.Content)

        val content = state as HomeUiState.Content
        assertEquals("Yohan", content.userName)
        assertNotNull(content.continueProcess)
        assertEquals("p1", content.continueProcess?.id)
        assertEquals(2, content.activeProcesses.size)
        assertEquals(1, content.todayTasks.size)
    }

    @Test
    fun `toggling task status updates task in repository`() = runBlocking {
        val t1 = Task(
            id = "t1",
            processId = "p1",
            title = "Tarea Pendiente",
            status = TaskStatus.PENDING,
            createdAtEpochMillis = 1000L
        )
        taskRepository.saveTask(t1)

        viewModel.onEvent(HomeUiEvent.OnToggleTaskStatus(t1))

        val updatedTask = taskRepository.getTaskById("t1").first()
        assertNotNull(updatedTask)
        assertEquals(TaskStatus.COMPLETED, updatedTask?.status)
    }

    @Test
    fun `completing task with duration creates work session and marks task completed`() = runBlocking {
        val t1 = Task(
            id = "t1",
            processId = "p1",
            title = "Tarea Pendiente",
            status = TaskStatus.PENDING,
            createdAtEpochMillis = 1000L
        )
        taskRepository.saveTask(t1)

        viewModel.onEvent(HomeUiEvent.CompleteTaskWithDuration(t1, 30))

        val updatedTask = taskRepository.getTaskById("t1").first()
        assertNotNull(updatedTask)
        assertEquals(TaskStatus.COMPLETED, updatedTask?.status)
        assertEquals(30 * 60 * 1000L, updatedTask?.timeWorkedMillis)

        val sessions = workSessionRepository.getWorkSessionsByTaskId("t1").first()
        assertEquals(1, sessions.size)
        assertEquals(30 * 60 * 1000L, sessions.first().durationMillis)
    }

    @Test
    fun `completing task with prior worked time only logs delta session when duration is increased`() = runBlocking {
        val t1 = Task(
            id = "t_delta",
            processId = "p1",
            title = "Tarea con Sesión Previa",
            status = TaskStatus.PENDING,
            createdAtEpochMillis = 1000L,
            timeWorkedMillis = 15 * 60 * 1000L // 15 min already worked
        )
        taskRepository.saveTask(t1)

        // User enters 25 minutes total
        viewModel.onEvent(HomeUiEvent.CompleteTaskWithDuration(t1, 25))

        val updatedTask = taskRepository.getTaskById("t_delta").first()
        assertNotNull(updatedTask)
        assertEquals(TaskStatus.COMPLETED, updatedTask?.status)
        assertEquals(25 * 60 * 1000L, updatedTask?.timeWorkedMillis)

        val sessions = workSessionRepository.getWorkSessionsByTaskId("t_delta").first()
        assertEquals(1, sessions.size)
        // Delta should be 10 minutes (25 - 15)
        assertEquals(10 * 60 * 1000L, sessions.first().durationMillis)
    }
}
