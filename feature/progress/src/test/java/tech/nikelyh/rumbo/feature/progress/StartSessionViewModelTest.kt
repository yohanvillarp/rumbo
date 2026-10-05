package tech.nikelyh.rumbo.feature.progress

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.nikelyh.rumbo.core.model.ProgressLevel
import tech.nikelyh.rumbo.feature.progress.fakes.FakeProcessRepository
import tech.nikelyh.rumbo.feature.progress.fakes.FakeProgressRepository
import tech.nikelyh.rumbo.feature.progress.fakes.FakeTaskRepository
import tech.nikelyh.rumbo.feature.progress.fakes.FakeWorkSessionRepository

class StartSessionViewModelTest {

    private lateinit var workSessionRepository: FakeWorkSessionRepository
    private lateinit var progressRepository: FakeProgressRepository
    private lateinit var processRepository: FakeProcessRepository
    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var viewModel: StartSessionViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        workSessionRepository = FakeWorkSessionRepository()
        progressRepository = FakeProgressRepository()
        processRepository = FakeProcessRepository()
        taskRepository = FakeTaskRepository()

        val savedStateHandle = SavedStateHandle(mapOf("processId" to "p1"))
        viewModel = StartSessionViewModel(
            savedStateHandle,
            workSessionRepository,
            progressRepository,
            taskRepository,
            processRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `finishing and submitting work session saves session with duration and note`() = runBlocking {
        viewModel.onEvent(StartSessionUiEvent.FinishTimer)
        viewModel.onEvent(StartSessionUiEvent.NoteChanged("Planificación y organización de la semana"))
        viewModel.onEvent(StartSessionUiEvent.SubmitSession)

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)

        val savedSessions = workSessionRepository.getWorkSessionsByProcessId("p1").first()
        assertEquals(1, savedSessions.size)
        val session = savedSessions.first()
        assertEquals("p1", session.processId)
        assertEquals("Planificación y organización de la semana", session.note)
    }

    @Test
    fun `submitting session with progress toggle creates explicit progress entry`() = runBlocking {
        viewModel.onEvent(StartSessionUiEvent.FinishTimer)
        viewModel.onEvent(StartSessionUiEvent.NoteChanged("Buena sesión"))
        viewModel.onEvent(StartSessionUiEvent.ToggleSaveProgress(true))
        viewModel.onEvent(StartSessionUiEvent.ProgressLevelSelected(ProgressLevel.HIGH))
        viewModel.onEvent(StartSessionUiEvent.SubmitSession)

        val savedEntries = progressRepository.getProgressEntriesByProcessId("p1").first()
        assertEquals(1, savedEntries.size)
        val entry = savedEntries.first()
        assertEquals(100, entry.progressLevel)
        assertEquals("Buena sesión", entry.note)
    }

    @Test
    fun `forgot timer with manual minutes sets duration and transitions to finished`() = runBlocking {
        viewModel.onEvent(StartSessionUiEvent.ForgotTimerClicked)
        val stateWithDialog = viewModel.uiState.value
        assertTrue(stateWithDialog.showForgotTimerDialog)

        viewModel.onEvent(StartSessionUiEvent.ConfirmManualMinutes("40"))

        val stateAfterConfirm = viewModel.uiState.value
        assertTrue(stateAfterConfirm.isSessionFinished)
        assertEquals(40 * 60 * 1000L, stateAfterConfirm.elapsedTimeMillis)
    }

    @Test
    fun `restores active timer state from repository when reopening session`() = runBlocking {
        val now = System.currentTimeMillis()
        val activeState = tech.nikelyh.rumbo.core.model.ActiveSessionState(
            processId = "p1",
            taskId = "t1",
            startTimeEpochMillis = now - 600000L,
            lastResumeEpochMillis = now - 300000L,
            accumulatedTimeMillis = 300000L,
            isRunning = true
        )
        workSessionRepository.saveActiveSessionState(activeState)

        val savedHandle = SavedStateHandle(mapOf("processId" to "p1", "taskId" to "t1"))
        val restoredViewModel = StartSessionViewModel(
            savedHandle,
            workSessionRepository,
            progressRepository,
            taskRepository,
            processRepository
        )

        val state = restoredViewModel.uiState.first { it.elapsedTimeMillis >= 600000L }
        assertTrue(state.isTimerRunning)
        assertTrue(state.elapsedTimeMillis >= 600000L)
    }

    @Test
    fun `canceling active session clears active state and exits without saving session`() = runBlocking {
        viewModel.onEvent(StartSessionUiEvent.CancelSession)

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertTrue(state.isSessionFinished)

        val activeState = workSessionRepository.activeSessionState.first()
        assertTrue(!activeState.hasActiveSession)

        val savedSessions = workSessionRepository.getWorkSessionsByProcessId("p1").first()
        assertTrue(savedSessions.isEmpty())
    }

    @Test
    fun `adjust minutes and resume updates elapsed time and keeps timer running`() = runBlocking {
        viewModel.onEvent(StartSessionUiEvent.ForgotTimerClicked)
        viewModel.onEvent(StartSessionUiEvent.AdjustMinutesAndResume("35"))

        val state = viewModel.uiState.value
        assertTrue(!state.showForgotTimerDialog)
        assertTrue(!state.isSessionFinished)
        assertTrue(state.isTimerRunning)
        assertEquals(35 * 60 * 1000L, state.elapsedTimeMillis)
    }

    @Test
    fun `submitting session with markTaskAsCompleted updates task to completed`() = runBlocking {
        val testTask = tech.nikelyh.rumbo.core.model.Task(
            id = "t1",
            processId = "p1",
            title = "Test Task",
            createdAtEpochMillis = 1000L
        )
        taskRepository.saveTask(testTask)

        val handle = SavedStateHandle(mapOf("processId" to "p1", "taskId" to "t1"))
        val vm = StartSessionViewModel(
            handle,
            workSessionRepository,
            progressRepository,
            taskRepository,
            processRepository
        )

        vm.onEvent(StartSessionUiEvent.FinishTimer)
        vm.onEvent(StartSessionUiEvent.ToggleCompleteTask(true))
        vm.onEvent(StartSessionUiEvent.SubmitSession)

        val savedTask = taskRepository.getTaskById("t1").first()
        assertTrue(savedTask != null && savedTask.isCompleted)
    }
}
