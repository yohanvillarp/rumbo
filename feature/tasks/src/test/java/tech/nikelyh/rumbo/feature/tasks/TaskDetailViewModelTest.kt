package tech.nikelyh.rumbo.feature.tasks

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
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus
import tech.nikelyh.rumbo.feature.tasks.fakes.FakeProcessRepository
import tech.nikelyh.rumbo.feature.tasks.fakes.FakeTaskRepository
import tech.nikelyh.rumbo.feature.tasks.fakes.FakeWorkSessionRepository

@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailViewModelTest {

    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var workSessionRepository: FakeWorkSessionRepository
    private lateinit var processRepository: FakeProcessRepository
    private lateinit var viewModel: TaskDetailViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    private val testTask = Task(
        id = "t100",
        processId = "p1",
        title = "Tarea Detalle",
        status = TaskStatus.PENDING,
        createdAtEpochMillis = 1000L
    )

    @Before
    fun setUp() = runBlocking {
        Dispatchers.setMain(testDispatcher)
        taskRepository = FakeTaskRepository()
        workSessionRepository = FakeWorkSessionRepository()
        processRepository = FakeProcessRepository()

        taskRepository.saveTask(testTask)

        val savedStateHandle = SavedStateHandle(mapOf("taskId" to "t100"))
        viewModel = TaskDetailViewModel(savedStateHandle, taskRepository, workSessionRepository, processRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads task detail correctly`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }

        val state = viewModel.uiState.first { it is TaskDetailUiState.Content }
        assertTrue(state is TaskDetailUiState.Content)

        val content = state as TaskDetailUiState.Content
        assertEquals("t100", content.task.id)
        assertEquals("Tarea Detalle", content.task.title)

        collectJob.cancel()
    }

    @Test
    fun `toggling status updates status to COMPLETED`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }
        viewModel.uiState.first { it is TaskDetailUiState.Content }

        viewModel.onEvent(TaskDetailUiEvent.ToggleStatus)

        val updated = taskRepository.getTaskById("t100").first()
        assertNotNull(updated)
        assertEquals(TaskStatus.COMPLETED, updated?.status)

        collectJob.cancel()
    }

    @Test
    fun `deleting task removes task from repository`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }
        viewModel.uiState.first { it is TaskDetailUiState.Content }

        viewModel.onEvent(TaskDetailUiEvent.DeleteTask)

        val deleted = taskRepository.getTaskById("t100").first()
        assertNull(deleted)

        collectJob.cancel()
    }

    @Test
    fun `completing task with duration creates work session and updates task`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }
        viewModel.uiState.first { it is TaskDetailUiState.Content }

        viewModel.onEvent(TaskDetailUiEvent.CompleteWithDuration(45))

        val updated = taskRepository.getTaskById("t100").first()
        assertNotNull(updated)
        assertEquals(TaskStatus.COMPLETED, updated?.status)
        assertEquals(45 * 60 * 1000L, updated?.timeWorkedMillis)

        val sessions = workSessionRepository.getWorkSessionsByTaskId("t100").first()
        assertEquals(1, sessions.size)
        assertEquals(45 * 60 * 1000L, sessions.first().durationMillis)

        collectJob.cancel()
    }

    @Test
    fun `hasStartedSession is true when an active session for the task is running`() = runBlocking {
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }
        viewModel.uiState.first { it is TaskDetailUiState.Content }

        workSessionRepository.saveActiveSessionState(
            tech.nikelyh.rumbo.core.model.ActiveSessionState(
                taskId = "t100",
                processId = "p1",
                startTimeEpochMillis = 1000L,
                isRunning = true
            )
        )

        val state = viewModel.uiState.first {
            it is TaskDetailUiState.Content && it.hasStartedSession
        } as TaskDetailUiState.Content
        assertTrue(state.hasStartedSession)

        collectJob.cancel()
    }
}
