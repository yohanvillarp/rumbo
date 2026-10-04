package tech.nikelyh.rumbo.feature.tasks

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
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus
import tech.nikelyh.rumbo.feature.tasks.fakes.FakeProcessRepository
import tech.nikelyh.rumbo.feature.tasks.fakes.FakeTaskRepository
import tech.nikelyh.rumbo.feature.tasks.fakes.FakeWorkSessionRepository

class TasksViewModelTest {

    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var workSessionRepository: FakeWorkSessionRepository
    private lateinit var processRepository: FakeProcessRepository
    private lateinit var viewModel: TasksViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        taskRepository = FakeTaskRepository()
        workSessionRepository = FakeWorkSessionRepository()
        processRepository = FakeProcessRepository()
        viewModel = TasksViewModel(taskRepository, workSessionRepository, processRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is Empty when no tasks exist`() = runBlocking {
        val state = viewModel.uiState.first()
        assertTrue(state is TasksUiState.Empty)
    }

    @Test
    fun `filters tasks by pending and completed tabs`() = runBlocking {
        val t1 = Task(id = "t1", processId = "p1", title = "Tarea Pendiente", status = TaskStatus.PENDING, createdAtEpochMillis = 1000L)
        val t2 = Task(id = "t2", processId = "p1", title = "Tarea Completada", status = TaskStatus.COMPLETED, createdAtEpochMillis = 2000L)

        taskRepository.saveTask(t1)
        taskRepository.saveTask(t2)

        val state = viewModel.uiState.first()
        assertTrue(state is TasksUiState.Content)

        val content = state as TasksUiState.Content
        assertEquals(1, content.tasks.size)
        assertEquals("t1", content.tasks.first().id)

        // Switch filter to Completed
        viewModel.onEvent(TasksUiEvent.FilterChanged(TaskFilter.COMPLETED))

        val updatedState = viewModel.uiState.first { (it as? TasksUiState.Content)?.selectedFilter == TaskFilter.COMPLETED }
        val updatedContent = updatedState as TasksUiState.Content
        assertEquals(1, updatedContent.tasks.size)
        assertEquals("t2", updatedContent.tasks.first().id)
    }
}
