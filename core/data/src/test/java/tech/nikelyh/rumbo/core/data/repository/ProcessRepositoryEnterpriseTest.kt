package tech.nikelyh.rumbo.core.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import tech.nikelyh.rumbo.core.data.fakes.FakeProcessDao
import tech.nikelyh.rumbo.core.data.fakes.FakeTaskDao
import tech.nikelyh.rumbo.core.data.model.asEntity
import tech.nikelyh.rumbo.core.data.testing.MainDispatcherRule
import tech.nikelyh.rumbo.core.data.testing.aProcess
import tech.nikelyh.rumbo.core.data.testing.aTask
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProcessStatus

/**
 * Enterprise-grade testing suite for [ProcessRepositoryImpl].
 * Utilizes:
 * - Kotlin Coroutines [runTest] for structured virtual time testing
 * - Cash App Turbine for deterministic reactive [kotlinx.coroutines.flow.Flow] assertions
 * - Google Truth for readable, diagnostic-rich fluent assertions
 * - Domain Fixture Builders for resilient test decoupling
 */
class ProcessRepositoryEnterpriseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var processDao: FakeProcessDao
    private lateinit var taskDao: FakeTaskDao
    private lateinit var repository: ProcessRepositoryImpl

    @Before
    fun setUp() {
        processDao = FakeProcessDao()
        taskDao = FakeTaskDao()
        repository = ProcessRepositoryImpl(processDao, taskDao)
    }

    @Test
    fun `getProcesses emits updated process list reactively using Turbine`() = runTest {
        repository.getProcesses().test {
            // Initially empty
            assertThat(awaitItem()).isEmpty()

            // When a new process is saved
            val newProcess = aProcess {
                withId("process-alpha")
                withName("Enterprise Architecture")
            }
            repository.saveProcess(newProcess)

            // Flow emits the newly added item
            val emittedList = awaitItem()
            assertThat(emittedList).hasSize(1)
            assertThat(emittedList.first().name).isEqualTo("Enterprise Architecture")
            assertThat(emittedList.first().id).isEqualTo("process-alpha")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleProcessStarred enforces enterprise maximum 3 starred limit`() = runTest {
        // Prepare 3 starred processes
        val p1 = aProcess { withId("p1"); withName("Process 1"); withStarred(true) }
        val p2 = aProcess { withId("p2"); withName("Process 2"); withStarred(true) }
        val p3 = aProcess { withId("p3"); withName("Process 3"); withStarred(true) }
        val p4 = aProcess { withId("p4"); withName("Process 4"); withStarred(false) }

        repository.saveProcess(p1)
        repository.saveProcess(p2)
        repository.saveProcess(p3)
        repository.saveProcess(p4)

        // Attempting to star the 4th process should fail with MaxLimitReached
        val result = repository.toggleProcessStarred("p4")
        assertThat(result).isInstanceOf(StarProcessResult.MaxLimitReached::class.java)

        // Verify with Truth that p4 remains unstarred
        repository.getProcessById("p4").test {
            val process = awaitItem()
            assertThat(process?.isStarred).isFalse()
            cancelAndIgnoreRemainingEvents()
        }

        // Unstarring one should succeed
        val unstarResult = repository.toggleProcessStarred("p1")
        assertThat(unstarResult).isEqualTo(StarProcessResult.Success)

        repository.getProcessById("p1").test {
            val process = awaitItem()
            assertThat(process?.isStarred).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ensureGeneralProcessExists guarantees singleton General process`() = runTest {
        repository.ensureGeneralProcessExists()

        repository.getProcessById(Process.GENERAL_PROCESS_ID).test {
            val general = awaitItem()
            assertThat(general).isNotNull()
            assertThat(general!!.name).isEqualTo("General")
            assertThat(general.isSystem).isTrue()
            assertThat(general.isActive).isTrue()
            cancelAndIgnoreRemainingEvents()
        }

        // Calling again is idempotent
        repository.ensureGeneralProcessExists()
        repository.getProcesses().test {
            val processes = awaitItem()
            assertThat(processes.filter { it.id == Process.GENERAL_PROCESS_ID }).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `general process deletion is blocked to preserve system integrity`() = runTest {
        repository.ensureGeneralProcessExists()

        val deleteSuccess = repository.deleteProcess(Process.GENERAL_PROCESS_ID)
        assertThat(deleteSuccess).isFalse()

        val archiveSuccess = repository.archiveProcess(Process.GENERAL_PROCESS_ID)
        assertThat(archiveSuccess).isFalse()
    }

    @Test
    fun `cost aggregation rolls up recursively across process hierarchy`() = runTest {
        val root = aProcess { withId("root"); withName("Root") }
        val sub = aProcess { withId("sub"); withName("Sub"); withParent("root") }
        val grand = aProcess { withId("grand"); withName("Grand"); withParent("sub") }

        repository.saveProcess(root)
        repository.saveProcess(sub)
        repository.saveProcess(grand)

        // Insert tasks with costs: root=100.0, sub=50.0, grand=25.0
        taskDao.insertOrUpdate(aTask { withId("t1"); withProcessId("root"); withCost(100.0) }.asEntity())
        taskDao.insertOrUpdate(aTask { withId("t2"); withProcessId("sub"); withCost(50.0) }.asEntity())
        taskDao.insertOrUpdate(aTask { withId("t3"); withProcessId("grand"); withCost(25.0) }.asEntity())

        repository.getProcesses().test {
            val processes = awaitItem()
            val rootItem = processes.first { it.id == "root" }
            val subItem = processes.first { it.id == "sub" }
            val grandItem = processes.first { it.id == "grand" }

            // Root must accumulate 100 + 50 + 25 = 175.0
            assertThat(rootItem.accumulatedDirectCost).isEqualTo(175.0)

            // Sub must accumulate 50 + 25 = 75.0
            assertThat(subItem.accumulatedDirectCost).isEqualTo(75.0)

            // Grand must have only its direct cost = 25.0
            assertThat(grandItem.accumulatedDirectCost).isEqualTo(25.0)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
