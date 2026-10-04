package tech.nikelyh.rumbo.feature.progress

import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProgressLevel
import tech.nikelyh.rumbo.core.model.Task

data class StartSessionUiState(
    val selectedProcessId: String = "general",
    val selectedTaskId: String? = null,
    val selectedTaskTitle: String = "",
    val availableProcesses: List<Process> = emptyList(),
    val availableTasks: List<Task> = emptyList(),
    val elapsedTimeMillis: Long = 0L,
    val isTimerRunning: Boolean = false,
    val isSessionFinished: Boolean = false,
    val sessionNote: String = "",
    val saveProgressEntry: Boolean = false,
    val progressLevel: ProgressLevel = ProgressLevel.MEDIUM,
    val showForgotTimerDialog: Boolean = false,
    val manualMinutesInput: String = "",
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false
)
