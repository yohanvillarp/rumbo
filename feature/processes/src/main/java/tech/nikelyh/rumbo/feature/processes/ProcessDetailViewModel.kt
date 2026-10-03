package tech.nikelyh.rumbo.feature.processes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.nikelyh.rumbo.core.data.repository.MilestoneRepository
import tech.nikelyh.rumbo.core.data.repository.ProcessRepository
import tech.nikelyh.rumbo.core.data.repository.ProgressRepository
import tech.nikelyh.rumbo.core.data.repository.TaskRepository
import tech.nikelyh.rumbo.core.data.repository.WeeklyGoalRepository
import tech.nikelyh.rumbo.core.data.repository.WorkSessionRepository
import tech.nikelyh.rumbo.core.model.Milestone
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProgressEntry
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.WeeklyGoal
import tech.nikelyh.rumbo.core.model.WorkSession
import javax.inject.Inject

@HiltViewModel
class ProcessDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val processRepository: ProcessRepository,
    private val taskRepository: TaskRepository,
    private val milestoneRepository: MilestoneRepository,
    workSessionRepository: WorkSessionRepository,
    progressRepository: ProgressRepository,
    weeklyGoalRepository: WeeklyGoalRepository
) : ViewModel() {

    val processId: String = savedStateHandle.get<String>("processId") ?: ""

    val uiState: StateFlow<ProcessDetailUiState> = combine(
        processRepository.getProcessById(processId),
        taskRepository.getTasksByProcessId(processId),
        milestoneRepository.getMilestonesByProcessId(processId),
        workSessionRepository.getWorkSessionsByProcessId(processId),
        progressRepository.getProgressEntriesByProcessId(processId),
        weeklyGoalRepository.getWeeklyGoalsByProcessId(processId)
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val process = flows[0] as Process?
        @Suppress("UNCHECKED_CAST")
        val tasks = flows[1] as List<Task>
        @Suppress("UNCHECKED_CAST")
        val milestones = flows[2] as List<Milestone>
        @Suppress("UNCHECKED_CAST")
        val sessions = flows[3] as List<WorkSession>
        @Suppress("UNCHECKED_CAST")
        val progressEntries = flows[4] as List<ProgressEntry>
        @Suppress("UNCHECKED_CAST")
        val goals = flows[5] as List<WeeklyGoal>

        if (process == null) {
            ProcessDetailUiState.Error("Proceso no encontrado")
        } else {
            val totalTimeInvested = sessions.sumOf { it.durationMillis }
            val pendingTasks = tasks.filter { !it.isCompleted }
            val currentGoal = goals.firstOrNull()

            ProcessDetailUiState.Content(
                process = process,
                pendingTasks = pendingTasks,
                milestones = milestones,
                workSessions = sessions,
                totalTimeInvestedMillis = totalTimeInvested,
                progressEntries = progressEntries,
                weeklyGoal = currentGoal
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProcessDetailUiState.Loading
    )

    fun onEvent(event: ProcessDetailUiEvent) {
        val currentState = uiState.value as? ProcessDetailUiState.Content ?: return
        val currentProcess = currentState.process

        when (event) {
            ProcessDetailUiEvent.PauseProcess -> {
                viewModelScope.launch {
                    processRepository.saveProcess(currentProcess.pause())
                }
            }
            ProcessDetailUiEvent.ResumeProcess -> {
                viewModelScope.launch {
                    processRepository.saveProcess(currentProcess.resume())
                }
            }
            ProcessDetailUiEvent.FinishProcess -> {
                viewModelScope.launch {
                    processRepository.saveProcess(currentProcess.finish(System.currentTimeMillis()))
                }
            }
            ProcessDetailUiEvent.ArchiveProcess -> {
                viewModelScope.launch {
                    processRepository.archiveProcess(currentProcess.id)
                }
            }
            is ProcessDetailUiEvent.ToggleTaskStatus -> {
                viewModelScope.launch {
                    val updatedTask = event.task.updateStatus(
                        if (event.task.isCompleted) tech.nikelyh.rumbo.core.model.TaskStatus.PENDING else tech.nikelyh.rumbo.core.model.TaskStatus.COMPLETED
                    )
                    taskRepository.saveTask(updatedTask)
                }
            }
            is ProcessDetailUiEvent.ToggleMilestoneStatus -> {
                viewModelScope.launch {
                    val updatedMilestone = if (event.milestone.isCompleted) {
                        event.milestone.reopen()
                    } else {
                        event.milestone.complete(System.currentTimeMillis())
                    }
                    milestoneRepository.saveMilestone(updatedMilestone)
                }
            }
        }
    }
}
