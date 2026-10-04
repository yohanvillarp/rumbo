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
import tech.nikelyh.rumbo.core.model.Priority
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskSortOrder
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

    @Test
    fun `filters tasks by overdue and priority`() = runBlocking {
        val now = System.currentTimeMillis()
        val overdueTask = Task(
            id = "t_overdue",
            processId = "p1",
            title = "Tarea Vencida",
            priority = Priority.HIGH,
            dueDateEpochMillis = now - 100_000L,
            createdAtEpochMillis = now - 200_000L
        )
        val futureTask = Task(
            id = "t_future",
            processId = "p1",
            title = "Tarea Futura",
            priority = Priority.LOW,
            dueDateEpochMillis = now + 100_000L,
            createdAtEpochMillis = now - 150_000L
        )

        taskRepository.saveTask(overdueTask)
        taskRepository.saveTask(futureTask)

        // Filter by OVERDUE
        viewModel.onEvent(TasksUiEvent.FilterChanged(TaskFilter.OVERDUE))
        val overdueState = viewModel.uiState.first { (it as? TasksUiState.Content)?.selectedFilter == TaskFilter.OVERDUE }
        val overdueContent = overdueState as TasksUiState.Content
        assertEquals(1, overdueContent.tasks.size)
        assertEquals("t_overdue", overdueContent.tasks.first().id)

        // Reset to ALL and filter by HIGH priority
        viewModel.onEvent(TasksUiEvent.FilterChanged(TaskFilter.ALL))
        viewModel.onEvent(TasksUiEvent.PriorityFilterChanged(Priority.HIGH))
        val priorityState = viewModel.uiState.first { (it as? TasksUiState.Content)?.selectedPriority == Priority.HIGH }
        val priorityContent = priorityState as TasksUiState.Content
        assertEquals(1, priorityContent.tasks.size)
        assertEquals("t_overdue", priorityContent.tasks.first().id)
    }

    @Test
    fun `sorts tasks by priority and recent`() = runBlocking {
        val now = System.currentTimeMillis()
        val lowRecent = Task(
            id = "t_low_recent",
            processId = "p1",
            title = "Baja reciente",
            priority = Priority.LOW,
            createdAtEpochMillis = now
        )
        val highOld = Task(
            id = "t_high_old",
            processId = "p1",
            title = "Alta antigua",
            priority = Priority.HIGH,
            createdAtEpochMillis = now - 50_000L
        )

        taskRepository.saveTask(lowRecent)
        taskRepository.saveTask(highOld)

        // Default sort is DUE_DATE. Change to PRIORITY
        viewModel.onEvent(TasksUiEvent.SortOrderChanged(TaskSortOrder.PRIORITY))
        val prioState = viewModel.uiState.first { (it as? TasksUiState.Content)?.taskSortOrder == TaskSortOrder.PRIORITY }
        val prioTasks = (prioState as TasksUiState.Content).tasks
        assertEquals("t_high_old", prioTasks[0].id)
        assertEquals("t_low_recent", prioTasks[1].id)

        // Change to RECENT
        viewModel.onEvent(TasksUiEvent.SortOrderChanged(TaskSortOrder.RECENT))
        val recentState = viewModel.uiState.first { (it as? TasksUiState.Content)?.taskSortOrder == TaskSortOrder.RECENT }
        val recentTasks = (recentState as TasksUiState.Content).tasks
        assertEquals("t_low_recent", recentTasks[0].id)
        assertEquals("t_high_old", recentTasks[1].id)
    }
}

