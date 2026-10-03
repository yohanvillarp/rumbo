package tech.nikelyh.rumbo.feature.processes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.nikelyh.rumbo.core.data.repository.ProcessRepository
import javax.inject.Inject

@HiltViewModel
class ProcessesViewModel @Inject constructor(
    private val processRepository: ProcessRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<ProcessesUiState> = combine(
        processRepository.getProcesses(),
        searchQuery
    ) { processes, query ->
        val filtered = if (query.isBlank()) {
            processes
        } else {
            processes.filter {
                it.name.contains(query, ignoreCase = true) ||
                    (it.description?.contains(query, ignoreCase = true) == true)
            }
        }
        ProcessesUiState.Success(
            processes = filtered,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProcessesUiState.Loading
    )

    fun onEvent(event: ProcessesUiEvent) {
        when (event) {
            is ProcessesUiEvent.SearchQueryChanged -> searchQuery.value = event.query
            is ProcessesUiEvent.CreateProcess -> {
                viewModelScope.launch {
                    processRepository.saveProcess(event.process)
                }
            }
            is ProcessesUiEvent.DeleteProcess -> {
                viewModelScope.launch {
                    processRepository.deleteProcess(event.processId)
                }
            }
            is ProcessesUiEvent.ProcessSelected -> { /* Handle process navigation */ }
        }
    }
}
