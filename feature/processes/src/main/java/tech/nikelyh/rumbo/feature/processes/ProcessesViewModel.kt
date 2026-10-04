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
import tech.nikelyh.rumbo.core.model.ProcessSortOrder
import tech.nikelyh.rumbo.core.model.ProcessStatus
import tech.nikelyh.rumbo.core.model.ProcessTypeFilter
import javax.inject.Inject

@HiltViewModel
class ProcessesViewModel @Inject constructor(
    processRepository: ProcessRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow(ProcessStatus.ACTIVE)
    private val selectedTypeFilter = MutableStateFlow(ProcessTypeFilter.ALL)
    private val sortOrder = MutableStateFlow(ProcessSortOrder.RECENT)

    val uiState: StateFlow<ProcessesUiState> = combine(
        processRepository.getProcesses(),
        searchQuery,
        selectedFilter,
        selectedTypeFilter,
        sortOrder
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val processes = flows[0] as List<Process>
        val query = flows[1] as String
        val filter = flows[2] as ProcessStatus
        val typeFilter = flows[3] as ProcessTypeFilter
        val order = flows[4] as ProcessSortOrder

        val active = processes.filter { it.status == ProcessStatus.ACTIVE }
        val paused = processes.filter { it.status == ProcessStatus.PAUSED }
        val completed = processes.filter { it.status == ProcessStatus.COMPLETED || it.status == ProcessStatus.ARCHIVED }

        fun processList(list: List<Process>): List<Process> {
            val typeFiltered = when (typeFilter) {
                ProcessTypeFilter.ALL -> list
                ProcessTypeFilter.MAIN -> list.filter { it.parentProcessId == null }
                ProcessTypeFilter.SUBPROCESS -> list.filter { it.parentProcessId != null }
            }
            val queryFiltered = if (query.isBlank()) typeFiltered else {
                typeFiltered.filter {
                    it.name.contains(query, ignoreCase = true) ||
                        (it.description?.contains(query, ignoreCase = true) == true)
                }
            }
            return when (order) {
                ProcessSortOrder.RECENT -> queryFiltered.sortedWith(
                    compareByDescending<Process> { it.isSystem }
                        .thenByDescending { it.createdAtEpochMillis }
                )
                ProcessSortOrder.NAME -> queryFiltered.sortedWith(
                    compareByDescending<Process> { it.isSystem }
                        .thenBy { it.name.lowercase() }
                )
                ProcessSortOrder.ACCUMULATED_COST -> queryFiltered.sortedWith(
                    compareByDescending<Process> { it.isSystem }
                        .thenByDescending { it.accumulatedDirectCost }
                )
            }
        }

        if (processes.isEmpty()) {
            ProcessesUiState.Empty
        } else {
            ProcessesUiState.Content(
                activeProcesses = processList(active),
                pausedProcesses = processList(paused),
                completedProcesses = processList(completed),
                selectedFilter = filter,
                selectedTypeFilter = typeFilter,
                sortOrder = order,
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
            is ProcessesUiEvent.TypeFilterChanged -> selectedTypeFilter.value = event.typeFilter
            is ProcessesUiEvent.SortOrderChanged -> sortOrder.value = event.sortOrder
            else -> { /* Navigation handled at Screen route level */ }
        }
    }
}
