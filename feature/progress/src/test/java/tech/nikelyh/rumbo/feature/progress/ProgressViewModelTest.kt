package tech.nikelyh.rumbo.feature.progress

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
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProcessStatus
import tech.nikelyh.rumbo.core.model.ProgressEntry
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskStatus
import tech.nikelyh.rumbo.core.model.WorkSession
import tech.nikelyh.rumbo.feature.progress.fakes.FakeProcessRepository
import tech.nikelyh.rumbo.feature.progress.fakes.FakeProgressRepository
import tech.nikelyh.rumbo.feature.progress.fakes.FakeTaskRepository
import tech.nikelyh.rumbo.feature.progress.fakes.FakeWorkSessionRepository

class ProgressViewModelTest {

    private lateinit var processRepository: FakeProcessRepository
    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var workSessionRepository: FakeWorkSessionRepository
    private lateinit var progressRepository: FakeProgressRepository
    private lateinit var viewModel: ProgressViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        processRepository = FakeProcessRepository()
        taskRepository = FakeTaskRepository()
        workSessionRepository = FakeWorkSessionRepository()
        progressRepository = FakeProgressRepository()

        viewModel = ProgressViewModel(
            processRepository,
            taskRepository,
            workSessionRepository,
            progressRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is Empty when no user processes or tasks exist`() = runBlocking {
        val state = viewModel.uiState.first()
        assertTrue(state is ProgressUiState.Empty)
    }

    @Test
    fun `computes task analytics and process analytics strictly separated`() = runBlocking {
        val p1 = Process(
            id = "p1",
            name = "Aprender Architecture",
            status = ProcessStatus.ACTIVE,
            createdAtEpochMillis = System.currentTimeMillis() - 86400000L,
            colorOrVisualId = "teal",
            accumulatedDirectCost = 150.0
        )
        processRepository.saveProcess(p1)

        val t1 = Task(
            id = "t1",
            processId = "p1",
            title = "Tarea Completa",
            status = TaskStatus.COMPLETED,
            createdAtEpochMillis = System.currentTimeMillis()
        )
        val t2 = Task(
            id = "t2",
            processId = "p1",
            title = "Tarea Pendiente",
            status = TaskStatus.PENDING,
            createdAtEpochMillis = System.currentTimeMillis()
        )
        taskRepository.saveTask(t1)
        taskRepository.saveTask(t2)

        val now = System.currentTimeMillis()
        val session = WorkSession(
            id = "s1",
            processId = "p1",
            startTimeEpochMillis = now - 3600000L,
            endTimeEpochMillis = now,
            durationMillis = 3600000L
        )
        workSessionRepository.saveWorkSession(session)

        val progressEntry = ProgressEntry(
            id = "pr1",
            processId = "p1",
            dateEpochMillis = now,
            progressLevel = 100,
            note = "Gran avance"
        )
        progressRepository.saveProgressEntry(progressEntry)

        val state = viewModel.uiState.first { it is ProgressUiState.Content }
        assertTrue(state is ProgressUiState.Content)

        val content = state as ProgressUiState.Content
        assertEquals(1, content.taskAnalytics.completedTasksCount)
        assertEquals(1, content.taskAnalytics.pendingTasksCount)

        assertEquals(3600000L, content.processAnalytics.totalTimeInvestedMillis)
        assertEquals(1, content.processAnalytics.totalSessionsCount)
        assertEquals(150.0, content.processAnalytics.totalAccumulatedCost, 0.01)
        assertEquals("Alto", content.processAnalytics.comparisons.first().declaredProgressLevelLabel)
    }

    @Test
    fun `switches timeframe filter between 7 30 and 90 days`() = runBlocking {
        val p1 = Process(id = "p1", name = "P1", status = ProcessStatus.ACTIVE, createdAtEpochMillis = 1000L, colorOrVisualId = "teal")
        processRepository.saveProcess(p1)

        viewModel.onEvent(ProgressUiEvent.TimeframeSelected(TimeframeFilter.DAYS_7))

        val state = viewModel.uiState.first { (it as? ProgressUiState.Content)?.selectedTimeframe == TimeframeFilter.DAYS_7 }
        val content = state as ProgressUiState.Content
        assertEquals(TimeframeFilter.DAYS_7, content.selectedTimeframe)
    }
}
