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
    fun `submitting task assigns General process if unselected and saves to repository`() = runBlocking {
        viewModel.onEvent(CreateTaskUiEvent.TitleChanged("  Configurar Gradle  "))
        viewModel.onEvent(CreateTaskUiEvent.CostChanged("0"))
        viewModel.onEvent(CreateTaskUiEvent.SubmitTask)

        val state = viewModel.uiState.value
        assertNull(state.titleError)
        assertNull(state.costError)
        assertTrue(state.isSuccess)

        val savedTasks = taskRepository.getAllTasks().first()
        assertEquals(1, savedTasks.size)
        val task = savedTasks.first()
        assertEquals("Configurar Gradle", task.title)
        assertEquals("general", task.processId)
        assertEquals(0.0, task.cost, 0.01)
    }
}
