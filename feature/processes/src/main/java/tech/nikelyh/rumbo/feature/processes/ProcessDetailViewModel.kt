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

import tech.nikelyh.rumbo.core.model.TaskSortOrder

@HiltViewModel
class ProcessDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val processRepository: ProcessRepository,
    private val taskRepository: TaskRepository,
    private val milestoneRepository: MilestoneRepository,
    private val weeklyGoalRepository: WeeklyGoalRepository,
    private val workSessionRepository: WorkSessionRepository,
    progressRepository: ProgressRepository
) : ViewModel() {

    val processId: String = savedStateHandle.get<String>("processId") ?: ""

    private val userMessageFlow = MutableStateFlow<String?>(null)
    private val taskSortOrderFlow = MutableStateFlow(TaskSortOrder.DUE_DATE)

    val uiState: StateFlow<ProcessDetailUiState> = combine(
        processRepository.getProcessById(processId),
        taskRepository.getTasksByProcessId(processId),
        milestoneRepository.getMilestonesByProcessId(processId),
        workSessionRepository.getWorkSessionsByProcessId(processId),
        progressRepository.getProgressEntriesByProcessId(processId),
        weeklyGoalRepository.getWeeklyGoalsByProcessId(processId),
        processRepository.getProcesses(),
        userMessageFlow,
        taskSortOrderFlow
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
        val allProcesses = flows[6] as List<Process>
        @Suppress("UNCHECKED_CAST")
        val userMsg = flows[7] as String?
        val sortOrder = flows[8] as TaskSortOrder

        if (process == null) {
            ProcessDetailUiState.Error("Proceso no encontrado")
        } else {
            val totalTimeInvested = sessions.sumOf { it.durationMillis }
            val rawPendingTasks = tasks.filter { !it.isCompleted }
            val pendingTasks = when (sortOrder) {
                TaskSortOrder.DUE_DATE -> rawPendingTasks.sortedWith(
                    compareBy<Task> { it.dueDateEpochMillis == null }
                        .thenBy { it.dueDateEpochMillis ?: Long.MAX_VALUE }
                        .thenByDescending { it.priority.ordinal }
                )
                TaskSortOrder.RECENT -> rawPendingTasks.sortedByDescending { it.createdAtEpochMillis }
                TaskSortOrder.PRIORITY -> rawPendingTasks.sortedWith(
                    compareByDescending<Task> { it.priority.ordinal }
                        .thenBy { it.dueDateEpochMillis ?: Long.MAX_VALUE }
                )
            }
            val completedTasks = tasks.filter { it.isCompleted }
            val currentGoal = goals.firstOrNull()

            val subProcesses = allProcesses.filter { it.parentProcessId == processId }
            val parentProcess = allProcesses.firstOrNull { it.id == process.parentProcessId }
            val activeSubProcesses = subProcesses.filter { !it.isFinished }

            val completionBlockedReason = when {
                pendingTasks.isNotEmpty() && activeSubProcesses.isNotEmpty() ->
                    "Para finalizar este proceso debes culminar sus ${pendingTasks.size} tareas pendientes y ${activeSubProcesses.size} subprocesos activos."
                pendingTasks.isNotEmpty() ->
                    "Para finalizar este proceso debes culminar sus ${pendingTasks.size} tareas pendientes."
                activeSubProcesses.isNotEmpty() ->
                    "Para finalizar este proceso debes culminar sus ${activeSubProcesses.size} subprocesos activos."
                else -> null
            }

            ProcessDetailUiState.Content(
                process = process,
                pendingTasks = pendingTasks,
                completedTasks = completedTasks,
                milestones = milestones,
                workSessions = sessions,
                totalTimeInvestedMillis = totalTimeInvested,
                progressEntries = progressEntries,
                weeklyGoal = currentGoal,
                subProcesses = subProcesses,
                parentProcess = parentProcess,
                completionBlockedReason = completionBlockedReason,
                userMessage = userMsg,
                taskSortOrder = sortOrder
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProcessDetailUiState.Loading
    )

    fun onEvent(event: ProcessDetailUiEvent) {
        if (event is ProcessDetailUiEvent.ChangeTaskSortOrder) {
            taskSortOrderFlow.value = event.order
            return
        }

        val currentState = uiState.value as? ProcessDetailUiState.Content ?: return
        val currentProcess = currentState.process

        when (event) {
            is ProcessDetailUiEvent.ChangeTaskSortOrder -> {
                taskSortOrderFlow.value = event.order
            }
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
                if (currentProcess.isSystem) {
                    userMessageFlow.value = "El proceso General permanece siempre activo para tus tareas cotidianas."
                    return
                }
                if (currentState.completionBlockedReason != null) {
                    userMessageFlow.value = currentState.completionBlockedReason
                    return
                }
                viewModelScope.launch {
                    processRepository.saveProcess(currentProcess.finish(System.currentTimeMillis()))
                }
            }
            ProcessDetailUiEvent.ReopenProcess -> {
                viewModelScope.launch {
                    processRepository.saveProcess(currentProcess.reopen())
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
            is ProcessDetailUiEvent.CompleteTaskWithDuration -> {
                viewModelScope.launch {
                    val targetTotalMillis = event.durationMinutes * 60 * 1000L
                    val additionalMillis = maxOf(0L, targetTotalMillis - event.task.timeWorkedMillis)
                    val now = System.currentTimeMillis()
                    if (additionalMillis > 0L) {
                        val session = WorkSession(
                            id = UUID.randomUUID().toString(),
                            processId = event.task.processId,
                            taskId = event.task.id,
                            startTimeEpochMillis = now - additionalMillis,
                            endTimeEpochMillis = now,
                            durationMillis = additionalMillis,
                            note = "Duración registrada al culminar tarea"
                        )
                        workSessionRepository.saveWorkSession(session)
                    }
                    val updatedTask = event.task.copy(timeWorkedMillis = maxOf(event.task.timeWorkedMillis, targetTotalMillis))
                        .updateStatus(tech.nikelyh.rumbo.core.model.TaskStatus.COMPLETED, finishedAt = now)
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
