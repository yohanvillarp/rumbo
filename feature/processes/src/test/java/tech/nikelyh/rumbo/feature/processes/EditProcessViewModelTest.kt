package tech.nikelyh.rumbo.feature.processes

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
import org.junit.Assert.assertFalse
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
import tech.nikelyh.rumbo.feature.processes.fakes.FakeProcessRepository
import tech.nikelyh.rumbo.feature.processes.fakes.FakeTaskRepository

class EditProcessViewModelTest {

    private lateinit var processRepository: FakeProcessRepository
    private lateinit var taskRepository: FakeTaskRepository
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    private val processId = "proc-edit-1"
    private val initialProcess = Process(
        id = processId,
        name = "Proceso Original",
        description = "Descripción original",
        status = ProcessStatus.ACTIVE,
        createdAtEpochMillis = 1000L,
        colorOrVisualId = "teal",
        dueDateEpochMillis = 10000L
    )

    @Before
    fun setUp() = runBlocking {
        Dispatchers.setMain(testDispatcher)
        processRepository = FakeProcessRepository()
        taskRepository = FakeTaskRepository()

        processRepository.saveProcess(initialProcess)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): EditProcessViewModel {
        return EditProcessViewModel(
            savedStateHandle = SavedStateHandle(mapOf("processId" to processId)),
            processRepository = processRepository,
            taskRepository = taskRepository
        )
    }

    @Test
    fun `loads process details and existing due date successfully`() = runBlocking {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertEquals("Proceso Original", state.name)
        assertEquals("Descripción original", state.description)
        assertEquals(10000L, state.dueDateEpochMillis)
        assertFalse(state.isLoading)
        assertNull(state.dueDateError)
    }

    @Test
    fun `setting process due date earlier than task max due date triggers error and blocks submit`() = runBlocking {
        val maxTaskDue = 15000L
        taskRepository.saveTask(
            Task(
                id = "t1",
                processId = processId,
                title = "Tarea 1",
                status = TaskStatus.PENDING,
                priority = Priority.MEDIUM,
                createdAtEpochMillis = 1000L,
                dueDateEpochMillis = maxTaskDue
            )
        )

        val viewModel = createViewModel()
        assertEquals(maxTaskDue, viewModel.uiState.value.maxTaskDueDateEpochMillis)

        // Attempt to set process deadline earlier than task deadline
        viewModel.onEvent(EditProcessUiEvent.DueDateChanged(12000L))

        var state = viewModel.uiState.value
        assertNotNull(state.dueDateError)
        assertTrue(state.dueDateError!!.contains("no puede ser anterior"))

        viewModel.onEvent(EditProcessUiEvent.SubmitProcess)
        state = viewModel.uiState.value
        assertFalse(state.isSuccess)

        // Process in repository should retain original deadline
        val savedProc = processRepository.getProcessById(processId).first()
        assertEquals(10000L, savedProc?.dueDateEpochMillis)
    }

    @Test
    fun `setting process due date greater than or equal to task max due date succeeds`() = runBlocking {
        val maxTaskDue = 15000L
        taskRepository.saveTask(
            Task(
                id = "t1",
                processId = processId,
                title = "Tarea 1",
                status = TaskStatus.PENDING,
                priority = Priority.MEDIUM,
                createdAtEpochMillis = 1000L,
                dueDateEpochMillis = maxTaskDue
            )
        )

        val viewModel = createViewModel()

        // Set process deadline later than task deadline
        val validNewDeadline = 20000L
        viewModel.onEvent(EditProcessUiEvent.DueDateChanged(validNewDeadline))

        val state = viewModel.uiState.value
        assertNull(state.dueDateError)

        viewModel.onEvent(EditProcessUiEvent.SubmitProcess)
        assertTrue(viewModel.uiState.value.isSuccess)

        val updated = processRepository.getProcessById(processId).first()
        assertEquals(validNewDeadline, updated?.dueDateEpochMillis)
    }

    @Test
    fun `clearing process due date succeeds`() = runBlocking {
        val viewModel = createViewModel()

        viewModel.onEvent(EditProcessUiEvent.DueDateChanged(null))
        assertNull(viewModel.uiState.value.dueDateError)

        viewModel.onEvent(EditProcessUiEvent.SubmitProcess)
        assertTrue(viewModel.uiState.value.isSuccess)

        val updated = processRepository.getProcessById(processId).first()
        assertNull(updated?.dueDateEpochMillis)
    }
}
