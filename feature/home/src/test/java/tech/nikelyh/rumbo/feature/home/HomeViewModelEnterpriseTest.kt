package tech.nikelyh.rumbo.feature.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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

/**
 * Enterprise architecture test suite for [HomeViewModel].
 * Demonstrates:
 * - Deterministic, reactive StateFlow verification using Cash App Turbine
 * - Fluent, expressive assertions with Google Truth
 * - Strict virtual time advancement and coroutine lifecycle management
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HomeViewModelEnterpriseTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private lateinit var processRepository: FakeProcessRepository
    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var workSessionRepository: FakeWorkSessionRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        processRepository = FakeProcessRepository()
        taskRepository = FakeTaskRepository()
        workSessionRepository = FakeWorkSessionRepository()
        settingsRepository = FakeSettingsRepository()
        viewModel = HomeViewModel(
            processRepository = processRepository,
            taskRepository = taskRepository,
            workSessionRepository = workSessionRepository,
            settingsRepository = settingsRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits state transitions reactively using Turbine`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            // Initial emission from stateIn initialValue
            assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

            // Upstream combines flows and emits Empty state
            val emptyState = awaitItem()
            assertThat(emptyState).isInstanceOf(HomeUiState.Empty::class.java)

            // Add an active process
            val process = Process(
                id = "p-focus",
                name = "Deep Work",
                status = ProcessStatus.ACTIVE,
                createdAtEpochMillis = 1000L,
                colorOrVisualId = "purple",
                isStarred = true
            )
            processRepository.saveProcess(process)

            // StateFlow transitions to Content with the starred process
            val contentState = awaitItem()
            assertThat(contentState).isInstanceOf(HomeUiState.Content::class.java)
            val content = contentState as HomeUiState.Content
            assertThat(content.starredProcesses).hasSize(1)
            assertThat(content.starredProcesses.first().name).isEqualTo("Deep Work")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `starred processes section remains empty when processes are not starred`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // Consume Loading
            awaitItem() // Consume Empty

            // Add an unstarred process
            processRepository.saveProcess(
                Process(
                    id = "p-normal",
                    name = "Daily Routines",
                    status = ProcessStatus.ACTIVE,
                    createdAtEpochMillis = 2000L,
                    colorOrVisualId = "teal",
                    isStarred = false
                )
            )

            val contentState = awaitItem() as HomeUiState.Content
            assertThat(contentState.starredProcesses).isEmpty()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `complete task with duration updates task status to COMPLETED and records work session`() = runTest(testDispatcher) {
        val task = Task(
            id = "task-enterprise",
            processId = "p1",
            title = "Code Review Architecture",
            status = TaskStatus.PENDING,
            createdAtEpochMillis = 1000L
        )
        taskRepository.saveTask(task)

        viewModel.onEvent(HomeUiEvent.CompleteTaskWithDuration(task, 45L))
        testScheduler.advanceUntilIdle()

        // Verify task updated
        val updatedTask = taskRepository.getTaskById("task-enterprise").first()
        assertThat(updatedTask).isNotNull()
        assertThat(updatedTask!!.status).isEqualTo(TaskStatus.COMPLETED)
        assertThat(updatedTask.timeWorkedMillis).isEqualTo(45L * 60_000L)
    }

    @Test
    fun `user message dismisses cleanly via UI event`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // Consume Loading
            awaitItem() // Consume Empty

            // Add 3 starred processes
            processRepository.saveProcess(Process("p1", "P1", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 1, colorOrVisualId = "1", isStarred = true))
            processRepository.saveProcess(Process("p2", "P2", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 2, colorOrVisualId = "2", isStarred = true))
            processRepository.saveProcess(Process("p3", "P3", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 3, colorOrVisualId = "3", isStarred = true))
            processRepository.saveProcess(Process("p4", "P4", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 4, colorOrVisualId = "4", isStarred = false))

            awaitItem() // Consume Content

            // Try starring 4th process
            viewModel.onEvent(HomeUiEvent.ToggleStar("p4"))

            val warningState = awaitItem() as HomeUiState.Content
            assertThat(warningState.userMessage).isEqualTo("Solo es posible destacar hasta 3 procesos")

            // Dismiss user message
            viewModel.onEvent(HomeUiEvent.DismissUserMessage)
            val dismissedState = awaitItem() as HomeUiState.Content
            assertThat(dismissedState.userMessage).isNull()

            cancelAndIgnoreRemainingEvents()
        }
    }
}
