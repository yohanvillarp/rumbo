package tech.nikelyh.rumbo.feature.processes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProcessDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val processRepository: ProcessRepository,
    private val taskRepository: TaskRepository,
    private val milestoneRepository: MilestoneRepository,
    private val weeklyGoalRepository: WeeklyGoalRepository,
    workSessionRepository: WorkSessionRepository,
    progressRepository: ProgressRepository
) : ViewModel() {

    val processId: String = savedStateHandle.get<String>("processId") ?: ""

    private val userMessageFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProcessDetailUiState> = combine(
        processRepository.getProcessById(processId),
        taskRepository.getTasksByProcessId(processId),
        milestoneRepository.getMilestonesByProcessId(processId),
        workSessionRepository.getWorkSessionsByProcessId(processId),
        progressRepository.getProgressEntriesByProcessId(processId),
        weeklyGoalRepository.getWeeklyGoalsByProcessId(processId),
        userMessageFlow
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
        @Suppress("UNCHECKED_CAST")
        val userMsg = flows[6] as String?

        if (process == null) {
            ProcessDetailUiState.Error("Proceso no encontrado")
        } else {
            val totalTimeInvested = sessions.sumOf { it.durationMillis }
            val pendingTasks = tasks.filter { !it.isCompleted }
            val completedTasks = tasks.filter { it.isCompleted }
            val currentGoal = goals.firstOrNull()

            ProcessDetailUiState.Content(
                process = process,
                pendingTasks = pendingTasks,
                completedTasks = completedTasks,
                milestones = milestones,
                workSessions = sessions,
                totalTimeInvestedMillis = totalTimeInvested,
                progressEntries = progressEntries,
                weeklyGoal = currentGoal,
                userMessage = userMsg
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
            ProcessDetailUiEvent.DismissUserMessage -> {
                userMessageFlow.value = null
            }
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
                if (currentState.pendingTasks.isNotEmpty()) {
                    userMessageFlow.value = "No se puede finalizar el proceso mientras existan tareas pendientes. Completa todas sus tareas asociadas primero."
                    return
                }
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
            is ProcessDetailUiEvent.SaveWeeklyGoal -> {
                viewModelScope.launch {
                    val currentGoal = currentState.weeklyGoal
                    val currentWeekId = getCurrentWeekIdentifier()
                    val newGoal = currentGoal?.edit(event.description) ?: WeeklyGoal(
                        id = UUID.randomUUID().toString(),
                        processId = processId,
                        weekIdentifier = currentWeekId,
                        description = event.description.trim()
                    )
                    weeklyGoalRepository.saveWeeklyGoal(newGoal)
                }
            }
            is ProcessDetailUiEvent.CompleteWeeklyGoal -> {
                viewModelScope.launch {
                    val currentGoal = currentState.weeklyGoal ?: return@launch
                    weeklyGoalRepository.saveWeeklyGoal(currentGoal.markAchieved())
                }
            }
            is ProcessDetailUiEvent.CarryOverWeeklyGoal -> {
                viewModelScope.launch {
                    val currentGoal = currentState.weeklyGoal ?: return@launch
                    val nextWeekId = getNextWeekIdentifier()
                    val carriedGoal = currentGoal.carryOverToWeek(
                        newGoalId = UUID.randomUUID().toString(),
                        nextWeekIdentifier = nextWeekId
                    )
                    weeklyGoalRepository.saveWeeklyGoal(carriedGoal)
                }
            }
            is ProcessDetailUiEvent.DiscardWeeklyGoal -> {
                viewModelScope.launch {
                    weeklyGoalRepository.deleteWeeklyGoal(event.goalId)
                }
            }
        }
    }

    private fun getCurrentWeekIdentifier(): String {
        return try {
            val now = LocalDate.now()
            val weekFields = WeekFields.of(Locale.getDefault())
            val weekNumber = now.get(weekFields.weekOfWeekBasedYear())
            "${now.year}-W$weekNumber"
        } catch (_: Throwable) {
            "2026-W40"
        }
    }

    private fun getNextWeekIdentifier(): String {
        return try {
            val nextWeek = LocalDate.now().plusWeeks(1)
            val weekFields = WeekFields.of(Locale.getDefault())
            val weekNumber = nextWeek.get(weekFields.weekOfWeekBasedYear())
            "${nextWeek.year}-W$weekNumber"
        } catch (_: Throwable) {
            "2026-W41"
        }
    }
}
