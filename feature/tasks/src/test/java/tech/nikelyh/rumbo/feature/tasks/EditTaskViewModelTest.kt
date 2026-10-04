package tech.nikelyh.rumbo.feature.tasks

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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.nikelyh.rumbo.core.model.Priority
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus
import tech.nikelyh.rumbo.feature.tasks.fakes.FakeProcessRepository
import tech.nikelyh.rumbo.feature.tasks.fakes.FakeTaskRepository

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EditTaskViewModelTest {

    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var processRepository: FakeProcessRepository
    private lateinit var viewModel: EditTaskViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    private val sampleTask = Task(
        id = "t100",
        processId = "p1",
        title = "Organizar estante",
        description = "Revisar libros y carpetas",
        status = TaskStatus.IN_PROGRESS,
        priority = Priority.MEDIUM,
        createdAtEpochMillis = 1000L,
        dueDateEpochMillis = 2000L,
        timeWorkedMillis = 50000L,
        cost = 15.0,
        estimatedDurationMinutes = 45
    )

    @Before
    fun setUp() = runBlocking {
        Dispatchers.setMain(testDispatcher)
        taskRepository = FakeTaskRepository()
        processRepository = FakeProcessRepository()
        taskRepository.saveTask(sampleTask)

        val savedStateHandle = SavedStateHandle(mapOf("taskId" to "t100"))
        viewModel = EditTaskViewModel(savedStateHandle, taskRepository, processRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads existing task data into uiState`() {
        val state = viewModel.uiState.value
        assertEquals("Organizar estante", state.title)
        assertEquals("Revisar libros y carpetas", state.description)
        assertEquals("p1", state.selectedProcessId)
        assertEquals(Priority.MEDIUM, state.priority)
        assertEquals("45", state.estimatedDurationMinutesInput)
        assertEquals("15.0", state.costInput)
        assertEquals(2000L, state.dueDateEpochMillis)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `blank title produces validation error on submit`() {
        viewModel.onEvent(EditTaskUiEvent.TitleChanged("   "))
        viewModel.onEvent(EditTaskUiEvent.SubmitTask)

        val state = viewModel.uiState.value
        assertEquals("El título de la tarea es obligatorio", state.titleError)
    }

    @Test
    fun `negative cost produces validation error on submit`() {
        viewModel.onEvent(EditTaskUiEvent.CostChanged("-5.0"))
        viewModel.onEvent(EditTaskUiEvent.SubmitTask)

        val state = viewModel.uiState.value
        assertEquals("El costo no puede ser negativo", state.costError)
    }

    @Test
    fun `null due date produces validation error on submit`() {
        viewModel.onEvent(EditTaskUiEvent.DueDateChanged(null))
        viewModel.onEvent(EditTaskUiEvent.SubmitTask)

        val state = viewModel.uiState.value
        assertEquals("La fecha límite es obligatoria", state.dueDateError)
    }

    @Test
    fun `submitting valid changes updates repository preserving task status and time worked`() = runBlocking {
        viewModel.onEvent(EditTaskUiEvent.TitleChanged("Organizar estante y escritorio"))
        viewModel.onEvent(EditTaskUiEvent.PriorityChanged(Priority.HIGH))
        viewModel.onEvent(EditTaskUiEvent.CostChanged("25.5"))
        viewModel.onEvent(EditTaskUiEvent.DurationChanged("60"))
        viewModel.onEvent(EditTaskUiEvent.SubmitTask)

        val state = viewModel.uiState.value
        assertNull(state.titleError)
        assertNull(state.costError)
        assertNull(state.dueDateError)
        assertTrue(state.isSuccess)

        val updatedTask = taskRepository.getTaskById("t100").first()
        assertEquals("Organizar estante y escritorio", updatedTask?.title)
        assertEquals(Priority.HIGH, updatedTask?.priority)
        assertEquals(25.5, updatedTask?.cost ?: 0.0, 0.01)
        assertEquals(60, updatedTask?.estimatedDurationMinutes)
        // Preserved original task attributes
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask?.status)
        assertEquals(50000L, updatedTask?.timeWorkedMillis)
        assertEquals(1000L, updatedTask?.createdAtEpochMillis)
    }
}
