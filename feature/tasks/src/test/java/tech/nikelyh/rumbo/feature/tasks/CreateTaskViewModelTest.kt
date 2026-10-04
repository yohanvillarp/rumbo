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
import tech.nikelyh.rumbo.feature.tasks.fakes.FakeProcessRepository
import tech.nikelyh.rumbo.feature.tasks.fakes.FakeTaskRepository

class CreateTaskViewModelTest {

    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var processRepository: FakeProcessRepository
    private lateinit var viewModel: CreateTaskViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        taskRepository = FakeTaskRepository()
        processRepository = FakeProcessRepository()

        val savedStateHandle = SavedStateHandle()
        viewModel = CreateTaskViewModel(savedStateHandle, taskRepository, processRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `blank title produces validation error`() {
        viewModel.onEvent(CreateTaskUiEvent.TitleChanged("   "))
        viewModel.onEvent(CreateTaskUiEvent.SubmitTask)

        val state = viewModel.uiState.value
        assertEquals("El título de la tarea es obligatorio", state.titleError)
    }

    @Test
    fun `negative cost produces validation error`() {
        viewModel.onEvent(CreateTaskUiEvent.TitleChanged("Tarea Test"))
        viewModel.onEvent(CreateTaskUiEvent.CostChanged("-5.0"))
        viewModel.onEvent(CreateTaskUiEvent.SubmitTask)

        val state = viewModel.uiState.value
        assertEquals("El costo no puede ser negativo", state.costError)
    }

    @Test
    fun `missing due date produces validation error`() {
        viewModel.onEvent(CreateTaskUiEvent.TitleChanged("Tarea Test"))
        viewModel.onEvent(CreateTaskUiEvent.DueDateChanged(null))
        viewModel.onEvent(CreateTaskUiEvent.SubmitTask)

        val state = viewModel.uiState.value
        assertEquals("La fecha límite es obligatoria", state.dueDateError)
    }

    @Test
    fun `submitting task with valid due date saves to repository`() = runBlocking {
        val dueTime = System.currentTimeMillis() + 86400000L
        viewModel.onEvent(CreateTaskUiEvent.TitleChanged("  Configurar Gradle  "))
        viewModel.onEvent(CreateTaskUiEvent.DueDateChanged(dueTime))
        viewModel.onEvent(CreateTaskUiEvent.CostChanged("0"))
        viewModel.onEvent(CreateTaskUiEvent.SubmitTask)

        val state = viewModel.uiState.value
        assertNull(state.titleError)
        assertNull(state.costError)
        assertNull(state.dueDateError)
        assertTrue(state.isSuccess)

        val savedTasks = taskRepository.getAllTasks().first()
        assertEquals(1, savedTasks.size)
        val task = savedTasks.first()
        assertEquals("Configurar Gradle", task.title)
        assertEquals(dueTime, task.dueDateEpochMillis)
        assertEquals(0.0, task.cost, 0.01)
    }

    @Test
    fun `submitting task with default 23 59 due date saves accurate timestamp`() = runBlocking {
        val targetDate = java.time.LocalDate.of(2026, 10, 4)
        val endOfDayMillis = targetDate.atTime(23, 59)
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        viewModel.onEvent(CreateTaskUiEvent.TitleChanged("Tarea con hora por defecto"))
        viewModel.onEvent(CreateTaskUiEvent.DueDateChanged(endOfDayMillis))
        viewModel.onEvent(CreateTaskUiEvent.SubmitTask)

        val task = taskRepository.getAllTasks().first().first()
        assertEquals(endOfDayMillis, task.dueDateEpochMillis)
        val savedZdt = java.time.Instant.ofEpochMilli(task.dueDateEpochMillis!!)
            .atZone(java.time.ZoneId.systemDefault())
        assertEquals(23, savedZdt.hour)
        assertEquals(59, savedZdt.minute)
    }
}
