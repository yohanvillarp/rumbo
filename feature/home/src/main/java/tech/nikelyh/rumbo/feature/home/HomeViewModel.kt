package tech.nikelyh.rumbo.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.nikelyh.rumbo.core.data.repository.ProcessRepository
import tech.nikelyh.rumbo.core.data.repository.SettingsRepository
import tech.nikelyh.rumbo.core.data.repository.TaskRepository
import tech.nikelyh.rumbo.core.data.repository.WorkSessionRepository
import tech.nikelyh.rumbo.core.model.TaskStatus
import tech.nikelyh.rumbo.core.model.WorkSession
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    processRepository: ProcessRepository,
    private val taskRepository: TaskRepository,
    private val workSessionRepository: WorkSessionRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        processRepository.getProcesses(),
        taskRepository.getAllTasks(),
        settingsRepository.userProfile
    ) { processes, tasks, userProfile ->
        val name = userProfile?.name ?: "Explorador"
        val hour = try {
            LocalTime.now().hour
        } catch (_: Throwable) {
            12
        }

        val greetingPrefix = getGreetingForHour(hour)
        val fullGreeting = "$greetingPrefix, $name"

        val activeProcesses = processes.filter { it.isActive }
        val featuredProcess = activeProcesses.firstOrNull { !it.nextAction.isNullOrBlank() }
            ?: activeProcesses.firstOrNull()

        val pendingTasks = tasks.filter { !it.isCompleted }

        if (processes.isEmpty() && tasks.isEmpty()) {
            HomeUiState.Empty
        } else {
            HomeUiState.Content(
                greeting = fullGreeting,
                userName = name,
                continueProcess = featuredProcess,
                activeProcesses = activeProcesses.take(3),
                todayTasks = pendingTasks.take(5)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading
    )

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnToggleTaskStatus -> {
                viewModelScope.launch {
                    val newStatus = if (event.task.isCompleted) TaskStatus.PENDING else TaskStatus.COMPLETED
                    val finishedAt = if (newStatus == TaskStatus.COMPLETED) System.currentTimeMillis() else null
                    val updatedTask = event.task.updateStatus(newStatus, finishedAt = finishedAt)
                    taskRepository.saveTask(updatedTask)
                }
            }
            is HomeUiEvent.CompleteTaskWithDuration -> {
                viewModelScope.launch {
                    val durationMillis = event.durationMinutes * 60 * 1000L
                    val now = System.currentTimeMillis()
                    val session = WorkSession(
                        id = UUID.randomUUID().toString(),
                        processId = event.task.processId,
                        taskId = event.task.id,
                        startTimeEpochMillis = now - durationMillis,
                        endTimeEpochMillis = now,
                        durationMillis = durationMillis,
                        note = "Duración registrada al culminar tarea"
                    )
                    workSessionRepository.saveWorkSession(session)
                    val updatedTask = event.task.addWorkedTime(durationMillis).updateStatus(TaskStatus.COMPLETED, finishedAt = now)
                    taskRepository.saveTask(updatedTask)
                }
            }
            else -> { /* Navigation events handled at UI Route level */ }
        }
    }

    internal fun getGreetingForHour(hour: Int): String {
        return when (hour) {
            in 5..11 -> "Buenos días"
            in 12..18 -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }
}
