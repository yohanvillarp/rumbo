package tech.nikelyh.rumbo.feature.processes

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
import androidx.lifecycle.SavedStateHandle
import org.junit.Test
import tech.nikelyh.rumbo.feature.processes.fakes.FakeProcessRepository

class CreateProcessViewModelTest {

    private lateinit var processRepository: FakeProcessRepository
    private lateinit var viewModel: CreateProcessViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        processRepository = FakeProcessRepository()
        viewModel = CreateProcessViewModel(SavedStateHandle(), processRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `blank name produces validation error`() {
        viewModel.onEvent(CreateProcessUiEvent.NameChanged("   "))
        viewModel.onEvent(CreateProcessUiEvent.SubmitProcess)

        val state = viewModel.uiState.value
        assertEquals("El nombre del proceso es obligatorio", state.nameError)
    }

    @Test
    fun `valid process submission saves process into repository with initial zero cost`() = runBlocking {
        viewModel.onEvent(CreateProcessUiEvent.NameChanged("  Aprender Android  "))
        viewModel.onEvent(CreateProcessUiEvent.DescriptionChanged("  Notas  "))
        viewModel.onEvent(CreateProcessUiEvent.SubmitProcess)

        val state = viewModel.uiState.value
        assertNull(state.nameError)
        assertTrue(state.isSuccess)

        val savedProcesses = processRepository.getProcesses().first()
        assertEquals(1, savedProcesses.size)
        val process = savedProcesses.first()
        assertEquals("Aprender Android", process.name)
        assertEquals("Notas", process.description)
        assertEquals(0.0, process.accumulatedDirectCost, 0.01)
    }

    @Test
    fun `selecting parent process associates subprocess`() = runBlocking {
        viewModel.onEvent(CreateProcessUiEvent.NameChanged("Subproceso 1"))
        viewModel.onEvent(CreateProcessUiEvent.ParentProcessSelected("parent-123"))
        viewModel.onEvent(CreateProcessUiEvent.SubmitProcess)

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)

        val savedProcesses = processRepository.getProcesses().first()
        val process = savedProcesses.first()
        assertEquals("Subproceso 1", process.name)
        assertEquals("parent-123", process.parentProcessId)
        assertTrue(process.isSubProcess)
    }
}
