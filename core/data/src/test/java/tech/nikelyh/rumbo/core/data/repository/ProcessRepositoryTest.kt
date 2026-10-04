package tech.nikelyh.rumbo.core.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.nikelyh.rumbo.core.data.fakes.FakeProcessDao
import tech.nikelyh.rumbo.core.data.fakes.FakeTaskDao
import tech.nikelyh.rumbo.core.data.model.asEntity
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.Task

class ProcessRepositoryTest {

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
    fun `ensureGeneralProcessExists creates General process`() = runBlocking {
        repository.ensureGeneralProcessExists()

        val generalProcess = repository.getProcessById(Process.GENERAL_PROCESS_ID).first()
        assertNotNull(generalProcess)
        assertEquals("General", generalProcess?.name)
        assertEquals(Process.GENERAL_PROCESS_ID, generalProcess?.id)
    }

    @Test
    fun `general process cannot be deleted`() = runBlocking {
        repository.ensureGeneralProcessExists()

        val deleted = repository.deleteProcess(Process.GENERAL_PROCESS_ID)
        assertFalse(deleted)

        val generalProcess = repository.getProcessById(Process.GENERAL_PROCESS_ID).first()
        assertNotNull(generalProcess)
    }

    @Test
    fun `general process cannot be archived`() = runBlocking {
        repository.ensureGeneralProcessExists()

        val archived = repository.archiveProcess(Process.GENERAL_PROCESS_ID)
        assertFalse(archived)

        val generalProcess = repository.getProcessById(Process.GENERAL_PROCESS_ID).first()
        assertTrue(generalProcess?.isActive == true)
    }

    @Test
    fun `user created process can be created updated and deleted`() = runBlocking {
        val process = Process(
            id = "user_p1",
            name = "Proyecto Personal",
            createdAtEpochMillis = 1000L,
            colorOrVisualId = "purple"
        )

        repository.saveProcess(process)

        val fetched = repository.getProcessById("user_p1").first()
        assertNotNull(fetched)
        assertEquals("Proyecto Personal", fetched?.name)

        val deleted = repository.deleteProcess("user_p1")
        assertTrue(deleted)

        val afterDelete = repository.getProcessById("user_p1").first()
        assertEquals(null, afterDelete)
    }

    @Test
    fun `process accumulated cost calculates sum of direct tasks`() = runBlocking {
        val process = Process(
            id = "p_finance",
            name = "Finanzas",
            createdAtEpochMillis = 1000L,
            colorOrVisualId = "green"
        )
        repository.saveProcess(process)

        taskDao.insertOrUpdate(
            Task(
                id = "t1",
                processId = "p_finance",
                title = "Auditoría",
                createdAtEpochMillis = 1000L,
                cost = 25.5
            ).asEntity()
        )
        taskDao.insertOrUpdate(
            Task(
                id = "t2",
                processId = "p_finance",
                title = "Software contable",
                createdAtEpochMillis = 2000L,
                cost = 50.0
            ).asEntity()
        )

        val fetched = repository.getProcessById("p_finance").first()
        assertNotNull(fetched)
        assertEquals(75.5, fetched?.accumulatedDirectCost ?: 0.0, 0.001)
    }

    @Test
    fun `process accumulated cost includes tasks from child and grandchild subprocesses`() = runBlocking {
        val mainProcess = Process(
            id = "main_corp",
            name = "Corporativo",
            createdAtEpochMillis = 1000L,
            colorOrVisualId = "blue"
        )
        val subProcess = Process(
            id = "sub_marketing",
            name = "Marketing",
            parentProcessId = "main_corp",
            createdAtEpochMillis = 1500L,
            colorOrVisualId = "purple"
        )
        val grandChildProcess = Process(
            id = "grand_campaign",
            name = "Campaña Digital",
            parentProcessId = "sub_marketing",
            createdAtEpochMillis = 2000L,
            colorOrVisualId = "amber"
        )

        repository.saveProcess(mainProcess)
        repository.saveProcess(subProcess)
        repository.saveProcess(grandChildProcess)

        // Task for Main: 100.0
        taskDao.insertOrUpdate(
            Task(
                id = "t_main",
                processId = "main_corp",
                title = "Estrategia general",
                createdAtEpochMillis = 1000L,
                cost = 100.0
            ).asEntity()
        )
        // Task for Sub: 50.0
        taskDao.insertOrUpdate(
            Task(
                id = "t_sub",
                processId = "sub_marketing",
                title = "Diseño de marca",
                createdAtEpochMillis = 1500L,
                cost = 50.0
            ).asEntity()
        )
        // Task for Grandchild: 25.0
        taskDao.insertOrUpdate(
            Task(
                id = "t_grand",
                processId = "grand_campaign",
                title = "Anuncios online",
                createdAtEpochMillis = 2000L,
                cost = 25.0
            ).asEntity()
        )

        val fetchedGrand = repository.getProcessById("grand_campaign").first()
        assertEquals(25.0, fetchedGrand?.accumulatedDirectCost ?: 0.0, 0.001)

        val fetchedSub = repository.getProcessById("sub_marketing").first()
        // Sub gets its own task (50.0) + grandchild task (25.0) = 75.0
        assertEquals(75.0, fetchedSub?.accumulatedDirectCost ?: 0.0, 0.001)

        val fetchedMain = repository.getProcessById("main_corp").first()
        // Main gets its own task (100.0) + sub (50.0) + grandchild (25.0) = 175.0
        assertEquals(175.0, fetchedMain?.accumulatedDirectCost ?: 0.0, 0.001)

        // Reactivity check: delete grand task
        taskDao.deleteById("t_grand")
        val mainAfterDelete = repository.getProcessById("main_corp").first()
        assertEquals(150.0, mainAfterDelete?.accumulatedDirectCost ?: 0.0, 0.001)

        val subAfterDelete = repository.getProcessById("sub_marketing").first()
        assertEquals(50.0, subAfterDelete?.accumulatedDirectCost ?: 0.0, 0.001)
    }
}
