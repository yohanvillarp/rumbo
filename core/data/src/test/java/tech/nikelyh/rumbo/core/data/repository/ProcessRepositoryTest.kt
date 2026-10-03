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
import tech.nikelyh.rumbo.core.model.Process

class ProcessRepositoryTest {

    private lateinit var processDao: FakeProcessDao
    private lateinit var repository: ProcessRepositoryImpl

    @Before
    fun setUp() {
        processDao = FakeProcessDao()
        repository = ProcessRepositoryImpl(processDao)
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
}
