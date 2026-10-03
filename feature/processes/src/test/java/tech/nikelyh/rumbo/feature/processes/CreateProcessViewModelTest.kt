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
        viewModel = CreateProcessViewModel(processRepository)
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
    fun `negative cost produces validation error`() {
        viewModel.onEvent(CreateProcessUiEvent.NameChanged("Mi Proceso"))
        viewModel.onEvent(CreateProcessUiEvent.CostChanged("-10.0"))
        viewModel.onEvent(CreateProcessUiEvent.SubmitProcess)

        val state = viewModel.uiState.value
        assertEquals("El costo inicial no puede ser negativo", state.costError)
    }

    @Test
    fun `valid process submission saves process into repository`() = runBlocking {
        viewModel.onEvent(CreateProcessUiEvent.NameChanged("  Aprender Android  "))
        viewModel.onEvent(CreateProcessUiEvent.DescriptionChanged("  Notas  "))
        viewModel.onEvent(CreateProcessUiEvent.CostChanged("100.5"))
        viewModel.onEvent(CreateProcessUiEvent.NextActionChanged("  Leer docs  "))
        viewModel.onEvent(CreateProcessUiEvent.SubmitProcess)

        val state = viewModel.uiState.value
        assertNull(state.nameError)
        assertNull(state.costError)
        assertTrue(state.isSuccess)

        val savedProcesses = processRepository.getProcesses().first()
        assertEquals(1, savedProcesses.size)
        val process = savedProcesses.first()
        assertEquals("Aprender Android", process.name)
        assertEquals("Notas", process.description)
        assertEquals(100.5, process.accumulatedDirectCost, 0.01)
        assertEquals("Leer docs", process.nextAction)
    }
}
