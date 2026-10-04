package tech.nikelyh.rumbo.feature.processes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import tech.nikelyh.rumbo.core.data.repository.ProcessRepository
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProcessStatus
import javax.inject.Inject

@HiltViewModel
class ProcessesViewModel @Inject constructor(
    processRepository: ProcessRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow(ProcessStatus.ACTIVE)

    val uiState: StateFlow<ProcessesUiState> = combine(
        processRepository.getProcesses(),
        searchQuery,
        selectedFilter
    ) { processes, query, filter ->
        val active = processes.filter { it.status == ProcessStatus.ACTIVE }
        val paused = processes.filter { it.status == ProcessStatus.PAUSED }
        val completed = processes.filter { it.status == ProcessStatus.COMPLETED || it.status == ProcessStatus.ARCHIVED }

        val filteredActive = filterByQuery(active, query)
        val filteredPaused = filterByQuery(paused, query)
        val filteredCompleted = filterByQuery(completed, query)

        if (processes.isEmpty()) {
            ProcessesUiState.Empty
        } else {
            ProcessesUiState.Content(
                activeProcesses = filteredActive,
                pausedProcesses = filteredPaused,
                completedProcesses = filteredCompleted,
                selectedFilter = filter,
                searchQuery = query
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProcessesUiState.Loading
    )

    fun onEvent(event: ProcessesUiEvent) {
        when (event) {
            is ProcessesUiEvent.SearchQueryChanged -> searchQuery.value = event.query
            is ProcessesUiEvent.FilterChanged -> selectedFilter.value = event.status
            else -> { /* Navigation handled at Screen route level */ }
        }
    }

    private fun filterByQuery(list: List<Process>, query: String): List<Process> {
        if (query.isBlank()) return list
        return list.filter {
            it.name.contains(query, ignoreCase = true) ||
                (it.description?.contains(query, ignoreCase = true) == true)
        }
    }
}
